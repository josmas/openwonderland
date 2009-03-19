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

import java.util.List;
import org.jdesktop.mtgame.Entity;

/**
 * An event when an item on the context menu for Entities is selected.
 * 
 * @author Jordan Slott <jslott@dev.java.net>
 */
public class ContextMenuEvent {

    private String name = null;
    private List<Entity> entityList = null;

    /** Constructor, takes the entity */
    public ContextMenuEvent(String name, List<Entity> entityList) {
        this.name = name;
        this.entityList = entityList;
    }

    /**
     * Returns the Entity list associated with the context menu.
     * 
     * @return A list of Entity objects
     */
    public List<Entity> getEntityList() {
        return entityList;
    }

    /**
     * Returns the name of the context menu item selected.
     * @return The name of the context menu item selected
     */
    public String getName() {
        return name;
    }
}
