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

package org.jdesktop.wonderland.client.selection.event;

import java.util.List;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.wonderland.client.input.Event;

/**
 * Event when a context action has been taken for an Entity or a set of Entities.
 * If there is more than one Entity (that is, if multiple Entities have been
 * selected before the context event), then the first Entity selected is found
 * in the getEntity() method (from the Event superclass) and the ordered list
 * of all selected Entities is obtained via the getEntities() method.
 * 
 * @author Jordan Slott <jslott@dev.java.net>
 */
public class ContextEvent extends Event {
    /* The list of Entities associated with the context event */
    private List<Entity> entities = null;
    
    /** Default constructor */
    public ContextEvent() {
    }
    
    /** Constructor, takes the list of Enitities of the context event. */
    public ContextEvent(List<Entity> entities) {
        if (entities != null && entities.size() > 0) {
            setEntity(entities.get(0));
        }
        this.entities = entities;
    }
    
    /**
     * Returns an ordered list of Entities associated with the context event.
     * The first Entity in the list represents the first Entity selected.
     * 
     * @return A list of selected Entities
     */
    public List<Entity> getEntities() {
        return entities;
    }
    
    /** 
     * {@inheritDoc}
     * <br>
     * If event is null, a new event of this class is created and returned.
     */
    @Override
    public Event clone (Event event) {
	if (event == null) {
	    event = new ContextEvent();
	}
        ((ContextEvent)event).entities = entities;
	return super.clone(event);
    }
}
