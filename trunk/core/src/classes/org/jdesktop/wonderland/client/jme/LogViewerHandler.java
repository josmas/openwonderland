/**
 * Project Wonderland
 *
 * Copyright (c) 2004-2009, Sun Microsystems, Inc., All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * Sun designates this particular file as subject to the "Classpath"
 * exception as provided by Sun in the License file that accompanied
 * this code.
 */
package org.jdesktop.wonderland.client.jme;

import java.util.logging.Handler;
import java.util.logging.LogRecord;
import javax.swing.SwingUtilities;

/**
 * Handler that forwards log records to the log viewer
 * @author Jonathan Kaplan <kaplanj@dev.java.net>
 */
public class LogViewerHandler extends Handler {

    public LogViewerHandler() {
        super();

        LogViewerFrame.getInstance().setHandler(this);
    }

    @Override
    public void publish(final LogRecord record) {
        // call getSourceMethodName() here to force the record to fill in
        // the method name.  This is necessary because records use the
        // stack to find the method, so if you pass them to another thread
        // they won't be able to find it.
        record.getSourceMethodName();

        // make sure to update on the AWT event thread
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                LogViewerFrame.getInstance().addRecord(record);
            }
        });
    }

    @Override
    public void flush() {
    }

    @Override
    public void close() throws SecurityException {
    }
}
