/**
 * Open Wonderland
 *
 * Copyright (c) 2011 - 2012, Open Wonderland Foundation, All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * The Open Wonderland Foundation designates this particular file as
 * subject to the "Classpath" exception as provided by the Open Wonderland
 * Foundation in the License file that accompanied this code.
 */
package org.jdesktop.wonderland.client.jme;

import java.awt.event.ActionEvent;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.swing.SwingUtilities;

/**
 * Singleton data store for viewing logs
 * @author Jonathan Kaplan <jonathankap@gmail.com>
 */
public enum LogViewer {
    INSTANCE;
    
    /** the log handler */
    private LogViewerHandler handler;

    /** the queue of work to process */
    private List<LogRecord> workQueue = new LinkedList<LogRecord>();

    /** the list of currently displayed records */
    private final List<LogEntry> entries = Collections.synchronizedList(new LinkedList<LogEntry>());
    
    /** the maximum number of entries */
    private int maxEntries = 1000;

    /** whether the viewer is visible on startup */
    private boolean visibleOnStartup = false;
    
    /** any additional buttons */
    private final List<LogViewerButton> buttons = new LinkedList<LogViewerButton>();
    
    /** the actual log viewer frame */
    private LogViewerFrame frame;
    
    LogViewer() {
        // restore default values
        restore();
    }
    
    private void restore() {
        Preferences prefs = Preferences.userNodeForPackage(LogViewer.class);

        maxEntries = prefs.getInt("maxEntries", maxEntries);

        Logger root = LogManager.getLogManager().getLogger("");
        Level levelVal = Level.parse(prefs.get("rootLevel", root.getLevel().getName()));
        root.setLevel(levelVal);

        visibleOnStartup = prefs.getBoolean("visibleOnStartup", visibleOnStartup);
    }
    
    public void setVisible(final boolean visible) {
        if (SwingUtilities.isEventDispatchThread()) {
            doSetVisible(visible);
        } else {
            SwingUtilities.invokeLater(new Runnable() {
               public void run() {
                   doSetVisible(visible);
               } 
            });
        }
    }
    
    /**
     * Must be called on AWT event thread
     */
    private void doSetVisible(boolean visible) {
        LogViewerFrame lvf;
        
        if (visible) {
            lvf = getFrame(true);
            lvf.setVisible(true);
            lvf.toFront();
        } else {
            lvf = getFrame(false);
            if (lvf != null) {
                lvf.setVisible(false);
            }
        }
    }
    
    public LogViewerHandler getHandler() {
        return handler;
    }

    public void setHandler(LogViewerHandler handler) {
        this.handler = handler;
    }

    public int getMaxEntries() {
        return maxEntries;
    }

    public void setMaxEntries(int maxEntries) {
        if (maxEntries <= 0) {
            throw new IllegalArgumentException("Maximum entries must be " +
                                               "more than 0");
        }

        this.maxEntries = maxEntries;

        // save a preference
        Preferences prefs = Preferences.userNodeForPackage(LogViewer.class);
        prefs.putInt("maxEntries", maxEntries);

        // processing records now will correctly remove any records over
        // the new limit
        processRecords();
    }

    public Level getRootLogLevel() {
        return Logger.getLogger("").getLevel();
    }

    public void setRootLogLevel(Level rootLevel) {
        Logger.getLogger("").setLevel(rootLevel);

        // save a preference
        Preferences prefs = Preferences.userNodeForPackage(LogViewer.class);
        prefs.put("rootLevel", rootLevel.getName());
    }

    public boolean isVisibleOnStartup() {
        return visibleOnStartup;
    }

    public void setVisibleOnStartup(boolean visibleOnStartup) {
        this.visibleOnStartup = visibleOnStartup;

        // save a preference
        Preferences prefs = Preferences.userNodeForPackage(LogViewer.class);
        prefs.putBoolean("visibleOnStartup", visibleOnStartup);
    }
    
    /**
     * Add a new button to the log viewer
     */
    public void addButton(final LogViewerButton button) {
        buttons.add(button);
        
        // if the frame is already visible, add the button directly
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                if (frame != null) {
                    frame.addButton(button);
                }
            }
        });
    }
    
    /**
     * Remove a button from the log viewer
     */
    public void removeButton(final LogViewerButton button) {
        buttons.remove(button);
    
        // if the frame is already visible, remove the button directly
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                if (frame != null) {
                    frame.removeButton(button);
                }
            }
        });
    }
    
    /**
     * Get all buttons
     */
    protected List<LogViewerButton> getButtons() {
        return buttons;
    }
    
    /**
     * Get the frame associated with this viewer. Must be called on AWT
     * event thread, since the frame is created if it doesn't exist.
     * @param create if true, create the frame if it doesn't exist
     * @return the frame, or null if the frame doesn't exist and create is
     * false
     */
    protected LogViewerFrame getFrame(boolean create) {
        if (!SwingUtilities.isEventDispatchThread()) {
            throw new IllegalStateException("Must be on AWT event thread");
        }
        
        if (frame == null && create) {
            frame = new LogViewerFrame();
            
            // initialize with existing records
            synchronized (entries) {
                StringBuilder str = new StringBuilder();
                for (LogEntry entry : entries) {
                    format(entry.record, str);
                }
                frame.addRecord(str.toString(), 0);
            }
        }
        
        return frame;
    }

    /**
     * Called by the handler to add a new record to the log. This method queues
     * the record and schedules the actual update to happen on the AWT
     * event thread.
     * @param record the record to process
     */
    protected synchronized void addRecord(LogRecord record) {
        // OWL issue #160: queue up multiple events to improve performance

        // if the queue is empty, then we need to schedule a task
        // to clear the list. If the list is not empty, it means a task
        // has been scheduled but has not yet run. In that case, we can
        // just add our elements to the list and they will be processed
        // by the eventual task
        boolean schedule = workQueue.isEmpty();

        // add our record
        workQueue.add(record);

        // schedule a task if necessary
        if (schedule) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    processRecords();
                }
            });
        }
    }

    /**
     * Called on the AWT event thread to process all outstanding records
     */
    protected void processRecords() {
        // OWL issue #160: handle multiple events to improve performance

        // first, get the set of records to process, making sure we have the
        // lock to prevent additions
        Collection<LogRecord> records;
        synchronized (this) {
            // copy the records into a new collection
            records = new ArrayList<LogRecord>(workQueue);

            // clear the work queue. This ensures that the next time an element
            // is added, a new task will be scheduled
            workQueue.clear();
        }

        // process each record
        StringBuilder str = new StringBuilder();
        for (LogRecord record : records) {
            // format the record and add it to the strig builder
            int length = format(record, str);

            // add it to the list of records
            LogEntry entry = new LogEntry(record, length);
            entries.add(entries.size(), entry);
        }

        // if we are now over the maximum number of entries, remove as many
        // as we need
        int removeLen = 0;
        
        synchronized (entries) {
            while (entries.size() > getMaxEntries()) {
                LogEntry entry = entries.remove(0);
                removeLen += entry.length;
            }
        }

        // add data to the frame, if it exists
        LogViewerFrame lvf = getFrame(false);
        if (lvf != null) {
            lvf.addRecord(str.toString(), removeLen);
        }
    }
    
    /**
     * Get an unmodifiable copy of the list of entries
     * @return the list of log entries
     */
    public List<LogEntry> getEntries() {
        return entries;
    }
    
    /**
     * Format the given record, and add it to the given string builder. Return
     * the length of text added to the builder.
     */
    public static int format(LogRecord record, StringBuilder builder) {
        int startLen = builder.length();
        
        builder.append(record.getLevel());
        builder.append(" ");
        builder.append(DateFormat.getTimeInstance().format(new Date(record.getMillis())));
        builder.append(" ");
        builder.append(record.getSourceClassName());
        builder.append(" ");
        builder.append(record.getSourceMethodName());
        builder.append("\n");

        if (record.getMessage() != null) {
            // apply parameter substitutions
            builder.append(MessageFormat.format(record.getMessage(), record.getParameters()));
            builder.append("\n");
        }

        if (record.getThrown() != null) {
            builder.append(formatThrowable(record.getThrown()));
        }

        // return the difference in length from when we started
        return builder.length() - startLen;
    }

    public static String formatThrowable(Throwable t) {
        StringWriter out = new StringWriter();
        t.printStackTrace(new PrintWriter(out));
        return out.toString();
    }
    
    public class LogEntry {
        final LogRecord record;
        final int length;
    
        public LogEntry(LogRecord record, int length) {
            this.record = record;
            this.length = length;
        }
        
        public LogRecord getRecord() {
            return record;
        }
        
        public int getLength() {
            return length;
        }
    }
    
    
    /**
     * An extension of the log viewer that adds a button to the frame.
     * When the button is pressed, the activate() method is called with
     * the current formatted error report.
     */
    public interface LogViewerButton {
        /**
         * Get the text that should appear on the button
         * @return text that will appear on the button
         */
        String getButtonText();
        
        /**
         * Activate this button. The current set of log records and
         * event triggering the report are passed in.
         * @param report the formatted error report
         */
        void activate(List<LogEntry> entries, ActionEvent ae);
    }
}
