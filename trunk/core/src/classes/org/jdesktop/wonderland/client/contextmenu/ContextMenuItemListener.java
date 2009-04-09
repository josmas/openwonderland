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

import org.jdesktop.wonderland.client.cell.Cell;

/**
 * A listener for selections on the context menu.
 * 
 * @author Jordan Slott <jslott@dev.java.net>
 */
public interface ContextMenuItemListener {

    /**
     * Enumeration of menu item states
     */
    public enum MenuItemState {
        ENABLED, DISABLED, INACTIVE
    };

    /**
     * Returns the "state" of the context menu item, called when the context
     * menu is about to be displayed. This method determines whether the menu
     * item is:
     * <p>
     * ENABLED: The menu item is present and enabled on the context menu
     * DISABLED: The mneu item is present but disabled on the context menu
     * INACTIVE: The menu item is not present on the context menu
     *
     * @param menuItem The menu item in question
     * @param cell The cell associated with the current context menu
     * @return The desired state of the menu item
     */
    public MenuItemState getMenuItemState(ContextMenuItem menuItem, Cell cell);

    /**
     * A context menu item has been selected.
     *
     * @param event The context item event
     */
    public void actionPerformed(ContextMenuItemEvent event);
}
