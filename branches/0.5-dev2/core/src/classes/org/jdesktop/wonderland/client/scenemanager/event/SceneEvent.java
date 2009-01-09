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

package org.jdesktop.wonderland.client.scenemanager.event;

import java.util.List;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.wonderland.client.input.Event;

/**
 * The base event class for all scene manager events. This base class manages
 * a list of Entities to which the scene event applies. In some cases (e.g.
 * hover event), there will be only one Entity, while there may be more than
 * one Entity for other events (e.g. selection event). The list of Entities is
 * ordered: the first Entity in the list was the first Entity selected.
 *
 * @author Jordan Slott <jslott@dev.java.net>
 */
public class SceneEvent extends Event {

    /* The list of Entities associated with the context event */
    private List<Entity> entityList = null;

    /** Default constructor */
    public SceneEvent() {
    }

    /** Constructor, takes the list of Enitities of the context event. */
    public SceneEvent(List<Entity> entities) {
        this.entityList = entities;
    }

    /**
     * Returns an ordered list of Entities associated with the context event.
     * The first Entity in the list represents the first Entity selected.
     *
     * @return A list of selected Entities
     */
    public List<Entity> getEntityList() {
        return entityList;
    }

    /**
     * {@inheritDoc}
     * <br>
     * If event is null, a new event of this class is created and returned.
     */
    @Override
    public Event clone (Event event) {
        if (event == null) {
            event = new SceneEvent();
        }
        ((SceneEvent)event).entityList = entityList;
        return super.clone(event);
    }
}
