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
package org.jdesktop.wonderland.client.input;

import java.util.logging.Logger;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.wonderland.client.utils.SmallIntegerAllocator;
import org.jdesktop.wonderland.common.ExperimentalAPI;
import org.jdesktop.wonderland.common.InternalAPI;

/**
 * The base class for all Wonderland events and actions.
 *
 * @author deronj
 */

@ExperimentalAPI
public abstract class Event implements java.io.Serializable {

    private static final Logger logger = Logger.getLogger(Event.class.getName());

    /** This event's ID. All concrete subclasses should call <code>Event.allocateEventID</code> to allocate this. */
    public static int EVENT_ID;

    private static SmallIntegerAllocator idAllocator = new SmallIntegerAllocator();

    /** The entity to which this event was distributed. */
    protected Entity entity;

    /** Whether the entity had focus at the time the event was distributed. */
    protected boolean isFocussed;

    /** Create a new instance of Event */
    protected Event () {}

    /**
     * Return the ID of this event type. 
     */
    public int getID () {
	return EVENT_ID;
    }

    /**
     * Allocate a unique event ID.
     */
    protected static int allocateEventID () {
	return idAllocator.allocate();
    }

    /**
     * Free an already allocated event ID.
     */
    protected static void free (int eventID) {
	idAllocator.free(eventID);
    }

    /**
     * Returns the entity to which this event was distributed.
     */
    public Entity getEntity () {
	return entity;
    }

    /**
     * Did the entity with which this event is associated have focus at the time the event was distributed?
     */
    public boolean isFocussed () {
	return isFocussed;
    }

    /**
     * INTERNAL ONLY.
     * <br>
     * Specify the entity with which this event is associated.
     */
    @InternalAPI
    public void setEntity (Entity entity) {
	this.entity = entity;
    }

    /**
     * INTERNAL ONLY.
     * <br>
     * Set whether event's entity had focus at the time the event was distributed.
     * <br><br>
     * NOTE: this must be called by the <code>EventDistributor</code> at event distribution time.
     */
    @InternalAPI
    public void setFocussed (boolean focussed) {
	isFocussed = focussed;
    }

    /** Copy the members of this class into the given event. */
    public Event clone (Event event) {
	event.entity = entity;
	event.isFocussed = isFocussed;
	return event;
    }
}



