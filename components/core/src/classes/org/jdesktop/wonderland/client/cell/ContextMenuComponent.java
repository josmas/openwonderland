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
package org.jdesktop.wonderland.client.cell;

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;
import org.jdesktop.wonderland.client.contextmenu.ContextMenu;
import org.jdesktop.wonderland.client.contextmenu.ContextMenuListener;

/**
 *
 * A cell component which provides cell specific ContextMenu items to the
 * ContextMenu system.
 *
 * The general use case is that other components in the cell (and/or the cell itself)
 * adds menu items and listeners to this component using addMenuItem(....)
 *
 * @author paulby
 */
public class ContextMenuComponent extends CellComponent {

    private HashMap<ContextMenuItem, ContextMenuListener> menus = new HashMap();

    public ContextMenuComponent(Cell cell) {
        super(cell);
    }

    /**
     * Called by the ContextMenu system when the menu is hidden. This
     * method should remove anything added by showContextMenu
     * @param contextMenu
     */
    public void hideContextMenu(ContextMenu contextMenu) {
        synchronized(menus) {
            Set<Entry<ContextMenuItem, ContextMenuListener>> items = menus.entrySet();
            if (items==null)
                return;

            for(Entry<ContextMenuItem, ContextMenuListener> entry : items) {
                contextMenu.removeContextMenuItem(entry.getKey().getName());
            }
        }
    }

    /**
     * Called by the ContextMenu system before the menu is displayed. This method
     * should add any cell specific items to the menu
     * 
     * @param contextMenu
     */
    public void showContextMenu(ContextMenu contextMenu) {
        synchronized(menus) {
            Set<Entry<ContextMenuItem, ContextMenuListener>> items = menus.entrySet();
            if (items==null)
                return;
            
            for(Entry<ContextMenuItem, ContextMenuListener> entry : items) {
                contextMenu.addContextMenuItem(entry.getKey().getName(), entry.getValue());
            }
        }
    }

    /**
     * Add a menu item to the context menu for this cell. The String[] consists
     * of the menu hierarchy for the menu item. With the last String being the
     * name of the menu item, and all other strings being the names of submenus.
     * 
     * At the moment submenus are not supported (and will be ignored).
     *
     * @param menuItem
     * @param listener
     * @return true if the item was addedd successfully, false if there was a name collision and the item was not added
     */
    public boolean addMenuItem(String[] menuItem, ContextMenuListener listener) {

        ContextMenuItem item = new ContextMenuItem(menuItem);
        synchronized(menus) {
            if (menus.containsKey(item))
                return false;

            menus.put(item, listener);
        }
        return true;
    }

    /**
     * Remove the indicated menuItem from the context menu for this cell.
     * This change will not effect a menu that is currently being displayed,
     * but will be applied next time the menu is displayed
     * 
     * @param menuItem
     */
    public void removeMenuItem(String[] menuItem) {
        ContextMenuItem item = new ContextMenuItem(menuItem);
        synchronized(menus) {
            menus.remove(item);
        }
    }

    class ContextMenuItem {

        private String[] menuItem;

        public ContextMenuItem(String[] menuItem) {
            this.menuItem = menuItem;
        }

        public String getName() {
            return menuItem[menuItem.length-1];
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof ContextMenuItem))
                return false;

            String[] compare = ((ContextMenuItem)o).menuItem;
            if (compare.length!=menuItem.length)
                return false;

            boolean ret = true;
            for(int i=0; i<compare.length && ret; i++) {
                ret = compare[i].equals(menuItem[i]);
            }

            return ret;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 97 * hash + (this.menuItem != null ? this.menuItem[menuItem.length-1].hashCode() : 0);
            return hash;
        }
    }
}
