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
package org.jdesktop.wonderland.modules.contextmenu.client.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.contextmenu.ContextMenuItemEvent;
import org.jdesktop.wonderland.client.contextmenu.ContextMenuItem;
import org.jdesktop.wonderland.client.contextmenu.ContextMenuItemListener;
import org.jdesktop.wonderland.client.contextmenu.ContextMenuItemListener.MenuItemState;
import org.jdesktop.wonderland.client.contextmenu.ContextMenuManager;
import org.jdesktop.wonderland.client.contextmenu.cell.ContextMenuComponent;
import org.jdesktop.wonderland.client.contextmenu.spi.ContextMenuFactorySPI;
import org.jdesktop.wonderland.client.input.Event;
import org.jdesktop.wonderland.client.input.EventClassListener;
import org.jdesktop.wonderland.client.scenemanager.SceneManager;
import org.jdesktop.wonderland.client.scenemanager.event.ActivatedEvent;
import org.jdesktop.wonderland.client.scenemanager.event.ContextEvent;
import org.jdesktop.wonderland.client.scenemanager.event.SelectionEvent;

/**
 * A Swing-based implementation of the system context menu.
 * 
 * @author Jordan Slott <jslott@dev.java.net>
 */
public class SwingContextMenu {
    private JFrame contextMenu = null;
    private JPanel contextPanel = null;
    private Cell popupCell = null;
    private Map<String, ContextMenuItemListener> listenerMap = new HashMap();

    // Colors matching the Wonderland logo color scheme
    private Color WL_LIGHT_BLUE = new Color(12, 104, 234);
    private Color WL_BLUE = new Color(7, 73, 165);
    private Color WL_LIGHT_GREEN = new Color(61, 207, 60);
    private Color WL_GREEN = new Color(45, 164, 48);

    /** Constructor */
    public SwingContextMenu() {
        // Initialize the GUI.
        contextMenu = new JFrame();
        contextMenu.setResizable(false);
        contextMenu.setUndecorated(true);
        contextMenu.getContentPane().setLayout(new GridLayout(1, 1));
        contextPanel = new JPanel();
        contextPanel.setBackground(WL_LIGHT_BLUE);
        contextPanel.setOpaque(true);
        contextMenu.getContentPane().add(contextPanel);
        contextPanel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
        contextPanel.setLayout(new BoxLayout(contextPanel, BoxLayout.Y_AXIS));
        
        // Register a global listener for context and selection events
        SceneManager.getSceneManager().addSceneListener(new ContextSelectionListener());
    }

    /**
     * Initialize the context menu items.
     */
    private synchronized void initializeMenu(Cell cell) {
        // Clear out any existing entries in the context menu
        contextPanel.removeAll();
        listenerMap.clear();

        // Add the name of the cell as the header of the menu
        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
        titlePanel.setBackground(WL_BLUE);
        JLabel title = new JLabel("<html><b>" + cell.getName() + "</b></html>");
        title.setForeground(Color.WHITE);
        title.setBackground(Color.GRAY);
        titlePanel.add(title);
        contextPanel.add(titlePanel);
        contextPanel.invalidate();

        // Fetch the manager of the context menu and all of the factories
        // that generate menu items.
        ContextMenuManager cmm = ContextMenuManager.getContextMenuManager();
        List<ContextMenuFactorySPI> factoryList = cmm.getContextMenuFactoryList();

        // For each of the factories, loop through each of its items and
        // add to the menu
        for (ContextMenuFactorySPI factory : factoryList) {
            ContextMenuItem items[] = factory.getContextMenuItems();
            for (ContextMenuItem item : items) {
                addContextMenuItem(item, cell);
            }
        }

        // Look for the context component and add its items
        ContextMenuComponent cmc = cell.getComponent(ContextMenuComponent.class);
        if (cmc != null) {
            ContextMenuItem items[] = cmc.getContextMenuItems();
            for (ContextMenuItem item : items) {
                addContextMenuItem(item, cell);
            }
        }
    }

    /**
     * Adds a context menu entry.
     *
     * @param menuItem The new context menu item
     */
    private void addContextMenuItem(ContextMenuItem menuItem, Cell cell) {
        // First ask the listener (if it exists) the current state of the
        // menu item. If it is INACTIVE, then simply return.
        ContextMenuItemListener listener = menuItem.getContextMenuItemListener();
        MenuItemState state = MenuItemState.ENABLED;
        if (listener != null) {
            state = listener.getMenuItemState(menuItem, cell);
        }

        if (state == MenuItemState.INACTIVE) {
            return;
        }

        // Creates the context menu item, using the image as an icon if it
        // exists.
        String name = menuItem.getLabel();
        Image image = menuItem.getImage();
        JMenuItem item = null;
        if (image == null) {
            item = new JMenuItem(name);
        }
        else {
            item = new JMenuItem(name, new ImageIcon(image));
        }

        // Try to make the menu item look a bit nicer
        item.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 7));
        item.addMouseListener(new LabelListener(name));

        // If the state of the menu item is "DISABLED" then grey out the item
        if (state == MenuItemState.DISABLED) {
            item.setEnabled(false);
        }

        // Add the item to the menu
        contextPanel.add(item);
        contextPanel.invalidate();
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
    private void showContextMenu(MouseEvent event, Cell cell) {
        // Check if there is an existing popup menu visible and make
        // it invisible
        if (contextMenu.isVisible() == true) {
            contextMenu.setVisible(false);
        }

        // Make the popup menu visible in the new locatio
        popupCell = cell;
        Component component = event.getComponent();
        Point parentPoint = new Point(component.getLocationOnScreen());
        parentPoint.translate(event.getX(), event.getY());
        contextMenu.setLocation(parentPoint);
        contextMenu.setVisible(true);
        contextMenu.toFront();
        contextMenu.repaint();
    }

    /**
     * Hides the context menu.
     */
    private void hideContextMenu() {
        contextMenu.setVisible(false);
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
            // Fetch the source for this event. Check to see if it is enabled,
            // if not then just return
            JMenuItem item = (JMenuItem)e.getSource();
            if (item.isEnabled() == false) {
                return;
            }

            // Otherwise, highlight the menu item and fetch its listener. If
            // there is one, then dispatch the event to the listener.
            item.setBackground(WL_GREEN);
            ContextMenuItemListener listener = listenerMap.get(text);
            hideContextMenu();
            if (listener != null) {
                ContextMenuItemEvent event = new ContextMenuItemEvent(text, popupCell);
                listener.actionPerformed(event);
            }
        }

        @Override
        public void mouseEntered(MouseEvent e) {
            // Highlight the menu item with a color, but only if it is enabled
            JMenuItem item = (JMenuItem)e.getSource();
            if (item.isEnabled() == true) {
                item.setBackground(WL_LIGHT_GREEN);
                contextMenu.pack();
            }
        }

        @Override
        public void mouseExited(MouseEvent e) {
            // De-highlight the menu item with a color, but only if it is enabled
            JMenuItem item = (JMenuItem) e.getSource();
            if (item.isEnabled() == true) {
                item.setBackground(Color.white);
                contextMenu.pack();
            }
        }
    }

    /**
     * Inner class that listeners for context and selection events
     */
    class ContextSelectionListener extends EventClassListener {

        public ContextSelectionListener() {
            setSwingSafe(true);
        }

        @Override
        public Class[] eventClassesToConsume() {
            return new Class[] {
                ActivatedEvent.class, ContextEvent.class, SelectionEvent.class
            };
        }

        @Override
        public void commitEvent(Event event) {

            // Go ahead and either close the context menu or show the context
            // menu. We reacquire the lock afterwards
            if (event instanceof ActivatedEvent || event instanceof SelectionEvent) {
                hideContextMenu();
            }
            else if (event instanceof ContextEvent) {
                // Show the context menu, initialize the menu if this is the
                // first time
                Entity entity = null;
                List<Entity> entityList = ((ContextEvent)event).getEntityList();
                if (entityList != null && entityList.isEmpty() == false) {
                    entity = entityList.get(0);
                }
                
                Cell cell = null;
                if (entity != null) {
                    cell = SceneManager.getCellForEntity(entity);
                }
                if (cell == null) {
                    return;
                }

                initializeMenu(cell);
                ContextEvent ce = (ContextEvent) event;
                showContextMenu(ce.getMouseEvent(), cell);
            }
        }
    }
}
