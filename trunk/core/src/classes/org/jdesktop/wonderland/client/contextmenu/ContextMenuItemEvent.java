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
 * An event when an item on the context menu for Cell is selected.
 * 
 * @author Jordan Slott <jslott@dev.java.net>
 */
public class ContextMenuItemEvent {

    private String name = null;

    private Cell cell = null;

    /** Constructor, takes the entity */
    public ContextMenuItemEvent(String name, Cell cell) {
        this.name = name;
        this.cell = cell;
    }

    /**
     * Returns the Cell associated with the context menu.
     *
     * @return A Cell object
     */
    public Cell getCell() {
        return cell;
    }

    /**
     * Returns the name of the context menu item selected.
     *
     * @return The name of the context menu item selected
     */
    public String getName() {
        return name;
    }
}
