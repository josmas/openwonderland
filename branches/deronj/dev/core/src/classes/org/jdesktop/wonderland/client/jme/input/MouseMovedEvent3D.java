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
 * An event which indicates that a mouse move action occurred. 
 *
 * @author deronj
 */

@ExperimentalAPI
class MouseMovedEvent3D extends MouseMotionEvent3D {

    static {
	/** Allocate this event type's ID. */
	EVENT_ID = Event.allocateEventID();
    }

    /**
     * Create a new instance of MouseMovedEvent3D with a null pickData.
     * @param event The AWT event.
     */
    MouseMovedEvent3D (MouseEvent awtEvent) {
        super(awtEvent, null);
    }
    /**
     * Create a new instance of MouseMovedEvent3D.
     * @param event The AWT event.
     * @param pickData The pick data for the event.
     */
    MouseMovedEvent3D (MouseEvent awtEvent, PickData pickData) {
        super(awtEvent, pickData);
    }
}
