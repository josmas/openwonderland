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
package org.jdesktop.wonderland.client.contextmenu;

import java.awt.Color;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.wonderland.client.input.Event;
import org.jdesktop.wonderland.client.input.EventClassListener;
import org.jdesktop.wonderland.client.scenemanager.SceneManager;
import org.jdesktop.wonderland.client.scenemanager.event.ActivatedEvent;
import org.jdesktop.wonderland.client.scenemanager.event.ContextEvent;
import org.jdesktop.wonderland.client.scenemanager.event.SelectionEvent;
import javax.media.opengl.GLContext;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.cell.ContextMenuComponent;
import org.jdesktop.wonderland.client.jme.CellRefComponent;


/**
 * A very basic "context" menu that appears when a user selects one or more
 * Entities and typically right-clicks to display this menu. THIS IS A WORK
 * IN PROGRESS.
 * <p>
 * There are currently several issues:
 * 1. It uses Swing to display the meny until the HUD is ready
 * 2. It enforces no ordering of the menu items
 * 3. It uses an explicit registration mechanism (versus via Java service loader)
 * 
 * @author Jordan Slott <jslott@dev.java.net>
 */
public class ContextMenu implements ActionListener {
    /* The popup menu to attach in various locations */
    private JFrame contextMenu = null;

    /* The panel into which context menu items are placed */
    private JPanel contextPanel = null;
    /*
     * An ordered list of Entities associated with the context action, in the
     * order that they were selected. If the list is null, nothing is selected
     */
    private List<Entity> popupEntityList = null;
    
    /* A map of the context menu name and the listener */
    private Map<String, ContextMenuListener> listenerMap = new HashMap();

    private ContextMenuComponent contextMenuComponent = null;

    private HashMap<String, JLabel> menuItems = new HashMap();
    
    /** Constructor */
    public ContextMenu() {
        contextMenu = new JFrame();
        contextMenu.setResizable(false);
        contextMenu.setUndecorated(true);
        contextMenu.getContentPane().setLayout(new GridLayout(1, 1));
        contextMenu.getContentPane().setBackground(Color.LIGHT_GRAY);
        contextPanel = new JPanel();
        contextMenu.getContentPane().add(contextPanel);
        contextPanel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
        contextPanel.setLayout(new GridLayout());

        // Register a global listener for context and selection events
        SceneManager.getSceneManager().addSceneListener(new ContextSelectionListener());
        
//        addContextMenuItem("Edit", new ContextMenuListener() {
//            public void entityContextPerformed(ContextMenuEvent event) {
//                Logger.getLogger(ContextMenu.class.getName()).warning(event.getName());
//            }
//        });
//        addContextMenuItem("Move", new ContextMenuListener() {
//            public void entityContextPerformed(ContextMenuEvent event) {
//                Logger.getLogger(ContextMenu.class.getName()).warning(event.getName());
//            }
//        });
    }
    
    /**
     * Singleton to hold instance of ContextMenu. This holder class is
     * loader on the first execution of ContextMenu.getContextMenu().
     */
    private static class ContextMenuHolder {
        private final static ContextMenu manager = new ContextMenu();
    }

    /**
     * Returns a single instance of this class
     * <p>
     * @return Single instance of this class.
     */
    public static final ContextMenu getContextMenu() {
        return ContextMenuHolder.manager;
    }

    /**
     * Adds a context menu entry, along with a listener for events 
     */
    public void addContextMenuItem(String name, ContextMenuListener listener) {
        // Create a new popup menu entry for this
//        JButton item = new JButton(name);
//        item.addActionListener(this);
//        item.setActionCommand(name);

        JLabel item = new JLabel(name);
        item.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)));
        item.addMouseListener(new LabelListener(name));
        menuItems.put(name, item);

        int numChildren = contextPanel.getComponentCount();
        GridLayout layout = (GridLayout) contextPanel.getLayout();
        layout.setRows(numChildren+1);
        contextPanel.add(item);
        contextPanel.invalidate();
//        layout.layoutContainer(contextMenu);
        contextMenu.pack();
        
        // Add an entry to the map of listeners for each menu item, if not null
        if (listener != null) {
            listenerMap.put(name, listener);
        }
    }

    public void removeContextMenuItem(String name) {
        JLabel item = menuItems.get(name);
        if (item==null) {
            Logger.getLogger(ContextMenu.class.getName()).warning("Did not find menu item to remove "+name);
            return;
        }

        int numChildren = contextPanel.getComponentCount();
        GridLayout layout = (GridLayout) contextPanel.getLayout();
        layout.setRows(numChildren-1);
        contextPanel.remove(item);
        contextPanel.invalidate();
//        layout.layoutContainer(contextMenu);
        contextMenu.pack();

        listenerMap.remove(name);
    }
    
    /**
     * Shows the Entity context menu given the AWT MouseEvent and the Entity
     * associated with the mouse click
     */
    public void showContextMenu(MouseEvent event, List<Entity> entityList) {
        // Check if there is an existing popup menu visible and make
        // it invisible
        if (contextMenu.isVisible() == true) {
            contextMenu.setVisible(false);
        }

        if (entityList.size()>0) {
            Entity topEntity = entityList.get(0);
            if (topEntity!=null) {
                CellRefComponent refComp = (CellRefComponent)topEntity.getComponent(CellRefComponent.class);
                if (refComp==null) {
                    Logger.getLogger(ContextMenu.class.getName()).warning("No CellRefComponent in Entity "+topEntity);
                } else {
                    Cell cell = refComp.getCell();
                    contextMenuComponent = cell.getComponent(ContextMenuComponent.class);
                    if (contextMenuComponent!=null)
                        contextMenuComponent.showContextMenu(this);
                }
            }
        }

        // Make the popup menu visible in the new location
        popupEntityList = entityList;
        Component component = event.getComponent();
        Point parentPoint = new Point(component.getLocationOnScreen());
        parentPoint.translate(event.getX(), event.getY());
        contextMenu.toFront();
        contextMenu.setLocation(parentPoint);
        contextMenu.setVisible(true);
        contextMenu.repaint();
    }

    /**
     * Hides the context menu.
     */
    public void hideContextMenu() {
        contextMenu.setVisible(false);
        if (contextMenuComponent!=null)
            contextMenuComponent.hideContextMenu(this);
    }

    /**
     * Listeners for clicks on any contxt menu element
     */
    class LabelListener extends MouseAdapter {

        /* The original text of the string */
        public String text = null;

        /** Constructor, takes the index of the element */
        public LabelListener(String text) {
            this.text = text;
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            // Fetch the listener for this menu item and deliver the event
            JLabel label = (JLabel)e.getSource();
            label.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)));
            ContextMenuListener listener = listenerMap.get(text);
            hideContextMenu();
            if (listener != null) {
                ContextMenuEvent event = new ContextMenuEvent(text, popupEntityList);
                listener.entityContextPerformed(event);
            }
        }

        @Override
        public void mouseEntered(MouseEvent e) {
            JLabel label = (JLabel)e.getSource();
            label.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(Color.BLACK),
                    BorderFactory.createEmptyBorder(5, 10, 5, 10)));
            contextMenu.pack();
        }

        @Override
        public void mouseExited(MouseEvent e) {
            JLabel label = (JLabel) e.getSource();
            label.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(Color.LIGHT_GRAY),
                    BorderFactory.createEmptyBorder(5, 10, 5, 10)));
            contextMenu.pack();
        }
    }

    public void actionPerformed(ActionEvent e) {
        // Send an event to the listener for the menu item. If we don't find a
        // listener, then just ignore.
        String name = e.getActionCommand();
        ContextMenuListener listener = listenerMap.get(name);
        hideContextMenu();
        if (listener != null) {
            ContextMenuEvent event = new ContextMenuEvent(name, popupEntityList);
            listener.entityContextPerformed(event);
        }
    }

    // We need to call this method reflectively because it isn't available in Java 5
    // BTW: we don't support Java 5 on Linux, so this is okay.
    private static boolean isLinux = System.getProperty("os.name").equals("Linux");
    private static Method isAWTLockHeldByCurrentThreadMethod;
    static {
        if (isLinux) {
            try {
                Class awtToolkitClass = Class.forName("sun.awt.SunToolkit");
                isAWTLockHeldByCurrentThreadMethod =
                    awtToolkitClass.getMethod("isAWTLockHeldByCurrentThread");
            } catch (ClassNotFoundException ex) {
            } catch (NoSuchMethodException ex) {
            }
        }
    }


    /**
     * Inner class that listeners for context and selection events
     */
    class ContextSelectionListener extends EventClassListener {
        @Override
        public Class[] eventClassesToConsume() {
            return new Class[] {
                        ActivatedEvent.class, ContextEvent.class,
                        SelectionEvent.class
            };
        }

        // Note: we don't override computeEvent because we don't do any computation in this listener.
        
        @Override
        public void commitEvent(Event event) {
            // Linux-specific workaround: On Linux JOGL holds the SunToolkit AWT lock in mtgame commit methods.
            // In order to avoid deadlock with any threads which are already holding the AWT lock and which
            // want to acquire the lock on the dirty rectangle so they can draw (e.g Embedded Swing threads)
            // we need to temporarily release the AWT lock before we lock the dirty rectangle and then reacquire
            // the AWT lock afterward.
            GLContext glContext = null;
            if (isAWTLockHeldByCurrentThreadMethod != null) {
                try {
                    Boolean ret = (Boolean) isAWTLockHeldByCurrentThreadMethod.invoke(null);
                    if (ret.booleanValue()) {
                        glContext = GLContext.getCurrent();
                        glContext.release();
                    }
                } catch (Exception ex) {}
            }

            // Go ahead and either close the context menu or show the context
            // menu. We reacquire the lock afterwards
            try {
                if (event instanceof ActivatedEvent || event instanceof SelectionEvent) {
                    hideContextMenu();
                }
                else if (event instanceof ContextEvent) {
                    // Show the context menu
                    ContextEvent ce = (ContextEvent) event;
                    showContextMenu(ce.getMouseEvent(), ce.getEntityList());
                }
            }
            finally {
                // Linux-specific workaround: Reacquire the lock if necessary.
                if (glContext != null) {
                    glContext.makeCurrent();
                }
            }
        }
    }
}
