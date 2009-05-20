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
package org.jdesktop.wonderland.modules.appbase.client.cell.view.viewdefault;

import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;

/**
 * A Swing-based implementation of the window menu menu.
 * Derived from org.jdesktop.wonderland.modules.contextmenu.client.ui.SwingContextMenu.
 *
 * @author deronj, Jordan Slott <jslott@dev.java.net>
 */
public class WindowMenuSwing {

    private static Logger logger = Logger.getLogger(WindowMenuSwing.class.getName());
    private Frame2DCell windowFrame;
    private JFrame menuFrame = null;
    private JPanel menuPanel = null;

    private HashMap<String,JMenuItem> menuItems = new HashMap<String,JMenuItem>();

    // Colors matching the Wonderland logo color scheme
    private Color WL_LIGHT_BLUE = new Color(12, 104, 234);
    private Color WL_BLUE = new Color(7, 73, 165);
    private Color WL_LIGHT_GREEN = new Color(61, 207, 60);
    private Color WL_GREEN = new Color(45, 164, 48);

    /** Create a new instance of WindowMenuSwing. */
    public WindowMenuSwing(Frame2DCell windowFrame) {
        this.windowFrame = windowFrame;

        // Initialize the GUI.
        menuFrame = new JFrame();
        menuFrame.setResizable(false);
        menuFrame.setUndecorated(true);
        menuFrame.getContentPane().setLayout(new GridLayout(1, 1));
        menuPanel = new JPanel();
        menuPanel.setBackground(WL_LIGHT_BLUE);
        menuPanel.setOpaque(true);
        menuFrame.getContentPane().add(menuPanel);
        menuPanel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
        menuPanel.setLayout(new BoxLayout(menuPanel, BoxLayout.Y_AXIS));

        initializeMenu();
    }

    /**
     * Initialize the window menu items. 
     */
    private void initializeMenu() {

        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
        titlePanel.setBackground(WL_BLUE);
        JLabel title = new JLabel("<html><b>Window Menu</b></html>");
        title.setForeground(Color.WHITE);
        title.setBackground(Color.GRAY);
        titlePanel.add(title);
        menuPanel.add(titlePanel);
        menuPanel.invalidate();

        // TODO: eventually, get the items from the frame
        addMenuItem("Control", "Take Control", null, true);
    }

    /**
     * Adds a menu entry.
     */
    private void addMenuItem(String itemName, String text, Image image, boolean isEnabled) {

        JMenuItem item;
        if (image == null) {
            item = new JMenuItem(text);
        } else {
            item = new JMenuItem(text, new ImageIcon(image));
        }

        // Try to make the menu item look a bit nicer
        item.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 7));
        item.addMouseListener(new ItemListener(text));

        // If the state of the menu item is "DISABLED" then grey out the item
        item.setEnabled(isEnabled);

        menuItems.put(itemName, item);

        // Add the item to the menu
        menuPanel.add(item);
        menuPanel.invalidate();
        menuFrame.pack();
    }

    /**
     * Cleanup resources.
     */
    public void cleanup () {
        // TODO
    }

    /**
     * Shows the menu located at the position of the given event.
     */
    public void showAt(MouseEvent event) {
        // If the window is already visible elsewhere, hide it
        if (menuFrame.isVisible() == true) {
            menuFrame.setVisible(false);
        }

        // Make the popup menu visible in the new locatio
        Component component = event.getComponent();
        Point parentPoint = new Point(component.getLocationOnScreen());
        parentPoint.translate(event.getX(), event.getY());
        menuFrame.setLocation(parentPoint);
        menuFrame.toFront();
        menuFrame.pack();
        menuFrame.repaint();
        menuFrame.setVisible(true);
    }

    /**
     * Hides the menu.
     */
    public void hide() {
        menuFrame.setVisible(false);
    }

    /**
     * Enable/disable the named menu item. Does nothing if the item doesn't exist in the menu.
     */
    public void itemSetEnabled (String itemName, boolean isEnabled) {
        JMenuItem item = menuItems.get(itemName);
        if (item == null) return;
        item.setEnabled(isEnabled);
    }

    /**
     * Set the text of the given menu item. Does nothing if the item doesn't exist in the menu.
     */
    public void itemSetText (String itemName, String text) {
        JMenuItem item = menuItems.get(itemName);
        if (item == null) return;
        item.setText(text);
    }

    /**
     * Listeners for clicks on any contxt menu element
     */
    class ItemListener extends MouseAdapter {

        /* The original text of the string */
        public String text = null;

        /** Constructor, takes the index of the element */
        public ItemListener(String text) {
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

            // Otherwise, highlight the menu item and hide the menu
            item.setBackground(WL_GREEN);
            hide();

            // TODO: perform action
            System.err.println("********* MOUSE CLICKED");
        }

        @Override
        public void mouseEntered(MouseEvent e) {
            // Highlight the menu item with a color, but only if it is enabled
            JMenuItem item = (JMenuItem)e.getSource();
            if (item.isEnabled() == true) {
                item.setBackground(WL_LIGHT_GREEN);
                menuFrame.pack();
            }
        }

        @Override
        public void mouseExited(MouseEvent e) {
            // De-highlight the menu item with a color, but only if it is enabled
            JMenuItem item = (JMenuItem) e.getSource();
            if (item.isEnabled() == true) {
                item.setBackground(Color.white);
                menuFrame.pack();
            }
        }
    }
}
