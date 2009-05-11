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
package org.jdesktop.wonderland.client.contextmenu.cell;

import java.util.HashSet;
import java.util.Set;
import org.jdesktop.wonderland.client.cell.*;
import org.jdesktop.wonderland.client.contextmenu.ContextMenuItem;

/**
 * A cell component which provides cell specific ContextMenu items to the
 * ContextMenu system. Users of this component can add and remove items.
 *
 * @author paulby
 * @author Jordan Slott <jslott@dev.java.net>
 */
public class ContextMenuComponent extends CellComponent {

    private Set<ContextMenuItem> menuItems;

    public ContextMenuComponent(Cell cell) {
        super(cell);
        menuItems = new HashSet();
    }

    /**
     * Add a menu item to the context menu for this cell component.
     *
     * @param menuItem The menu item to add
     * @return true if the item was addedd successfully, false if the menu item
     * already exists on the component
     */
    public boolean addMenuItem(ContextMenuItem menuItem) {
        synchronized (menuItems) {
            if (menuItems.contains(menuItem) == true) {
                return false;
            }
            menuItems.add(menuItem);
        }
        return true;
    }

    /**
     * Remove the indicated menu item from the context menu for this cell.
     * This change will not effect a menu that is currently being displayed,
     * but will be applied next time the menu is displayed
     * 
     * @param menuItem The menu item to remove
     */
    public void removeMenuItem(ContextMenuItem menuItem) {
        synchronized (menuItems) {
            menuItems.remove(menuItem);
        }
    }

    /**
     * Returns an array of context menu items.
     *
     * @return An array of context menu items
     */
    public ContextMenuItem[] getContextMenuItems() {
        synchronized (menuItems) {
            return menuItems.toArray(new ContextMenuItem[]{});
        }
    }
}
