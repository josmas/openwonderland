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
package org.jdesktop.wonderland.client.jme.input;

/**
 * An event which indicates that a mouse wheel rotation occurred. 
 *
 * @author deronj
 */

@ExperimentalAPI
public class MouseWheelEvent3D extends MouseEvent3D {

    static {
	/** Allocate this event type's ID. */
	EVENT_ID = Event.allocateEventID();
    }

    /**
     * Create a new instance of MouseWheelEvent3D will a null pickData.
     * @param event The AWT event.
     */
    public MouseWheelEvent3D (MouseWheelEvent awtEvent) {
        this(awtEvent, null);
    }
    
    /**
     * Create a new instance of MouseWheelEvent3D.
     * @param event The AWT event.
     * @param pickData The pick data for the event.
     */
    public MouseWheelEvent3D (MouseWheelEvent awtEvent, PickData pickData) {
        super(awtEvent, pickData);
    }
    
    /**
     * Returns the number of "clicks" the mouse wheel was rotated.
     *
     * @return negative values if the mouse wheel was rotated up/away from the 
     * user, and positive values if the mouse wheel was rotated down/ 
     * towards the user
     */
    public int getWheelRotation() {
        return ((MouseWheelEvent)awtEvent).getWheelRotation();
    }
}
