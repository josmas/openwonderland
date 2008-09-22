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
import org.jdesktop.wonderland.client.utils.SmallIntegerAllocator;
import org.jdesktop.wonderland.common.ExperimentalAPI;

/**
 * The base class for all Wonderland events and actions.
 *
 * @author deronj
 */

@ExperimentalAPI
public abstract class Event implements java.io.Serializable {

    /** This event's ID. All concrete subclasses should call <code>Event.allocateEventID</code> to allocate this. */
    public static int EVENT_ID;

    private static final Logger logger = Logger.getLogger(Event.class.getName());

    private static SmallIntegerAllocator idAllocator = new SmallIntegerAllocator();

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
}



