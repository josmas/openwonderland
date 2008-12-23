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

import org.jdesktop.mtgame.Entity;
import org.jdesktop.wonderland.client.input.Event;

/**
 * Event when the user input (e.g. mouse pointer) starts and stops hovering
 * above an Entity.
 * 
 * @author Jordan Slott <jslott@dev.java.net>
 */
public class HoverEvent extends Event {
    /* True if the hovering has started, false if the hovering has stopped */
    private boolean isStart = false;
    
    /** Default constructor */
    public HoverEvent() {
    }
    
    /**
     * Constructor, takes the Enitity over which the hovering takes place and
     * whether hovering is starting or stopping
     */
    public HoverEvent(Entity entity, boolean isStart) {
        setEntity(entity);
        this.isStart = isStart;
    }
    
    /**
     * Returns true if this is a hover start event, false if it is a hover stop
     * event.
     * 
     * @return True for hover start, false for hover stop
     */
    public boolean isStart() {
        return isStart;
    }
    
    /** 
     * {@inheritDoc}
     * <br>
     * If event is null, a new event of this class is created and returned.
     */
    @Override
    public Event clone (Event event) {
	if (event == null) {
	    event = new HoverEvent();
	}
        ((HoverEvent)event).isStart = isStart;
	return super.clone(event);
    }
}
