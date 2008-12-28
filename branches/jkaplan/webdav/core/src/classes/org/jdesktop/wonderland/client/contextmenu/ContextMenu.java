/**
 * Project Wonderland
 *
 * Copyright (c) 2004-2008, Sun Microsystems, Inc., All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * $Revision$
 * $Date$
 * $State$
 */

package org.jdesktop.wonderland.client.contextmenu;

import java.awt.Component;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JFrame;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.wonderland.client.input.Event;
import org.jdesktop.wonderland.client.input.EventClassListener;
import org.jdesktop.wonderland.client.input.InputManager;
import org.jdesktop.wonderland.client.selection.event.ActivatedEvent;
import org.jdesktop.wonderland.client.selection.event.ContextEvent;
import org.jdesktop.wonderland.client.selection.event.SelectionEvent;


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

    /*
     * An ordered list of Entities associated with the context action, in the
     * order that they were selected. If the list is null, nothing is selected
     */
    private List<Entity> popupEntityList = null;
    
    /* A map of the context menu name and the listener */
    private Map<String, ContextMenuListener> listenerMap = new HashMap();
    
    /** Constructor */
    public ContextMenu() {
        contextMenu = new JFrame();
        contextMenu.setResizable(false);
        contextMenu.setUndecorated(true);
        contextMenu.getContentPane().setLayout(new GridLayout());
        
        // Register a global listener for context and selection events
        InputManager inputManager = InputManager.inputManager();
        inputManager.addGlobalEventListener(new ContextSelectionListener());
        
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
     * loader on the first execution of ContextMenu.getEntityContextMenu().
     */
    private static class ContextMenuHolder {
        private final static ContextMenu manager = new ContextMenu();
    }

    /**
     * Returns a single instance of this class
     * <p>
     * @return Single instance of this class.
     */
    public static final ContextMenu getEntityContextMenu() {
        return ContextMenuHolder.manager;
    }

    /**
     * Adds a context menu entry, along with a listener for events 
     */
    public void addContextMenuItem(String name, ContextMenuListener listener) {
        // Create a new popup menu entry for this
        JButton item = new JButton(name);
        item.addActionListener(this);
        item.setActionCommand(name);
        int numChildren = contextMenu.getContentPane().getComponentCount();
        contextMenu.getContentPane().setLayout(new GridLayout(numChildren + 1, 1));
        contextMenu.getContentPane().add(item);
        contextMenu.pack();
        
        // Add an entry to the map of listeners for each menu item, if not null
        if (listener != null) {
            listenerMap.put(name, listener);
        }
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

        // Make the popup menu visible in the new location
        Logger.getLogger(ContextMenu.class.getName()).warning("FRAME VISIBLE");
        popupEntityList = entityList;
        Component component = event.getComponent();
        Point parentPoint = new Point(component.getLocationOnScreen());
        parentPoint.translate(event.getX(), event.getY());
        contextMenu.setVisible(true);
        contextMenu.toFront();
        contextMenu.setLocation(parentPoint);
    }

    /**
     * Hides the context menu.
     */
    public void hideContextMenu() {
        contextMenu.setVisible(false);
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
            if (event instanceof ActivatedEvent || event instanceof SelectionEvent) {
                hideContextMenu();
            }
            else if (event instanceof ContextEvent) {
                // Show the context menu
                ContextEvent ce = (ContextEvent)event;
                Logger.getLogger(ContextMenu.class.getName()).warning("mouse event " + ce.getMouseEvent().getComponent());
                showContextMenu(ce.getMouseEvent(), popupEntityList);
            }
        }
    }
}
