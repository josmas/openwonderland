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

import org.jdesktop.wonderland.common.ExperimentalAPI;

/**
 * An InputManager-internal event which is used to switch event modes in a safe manner.
 *
 * @author deronj
 */

@ExperimentalAPI
class EventModeEvent extends Event {

    /** The new event mode. */
    private InputManager.EventMode eventMode;

    /**
     * Create a new instance of EventModeEvent.
     * @param eventMode The new event mode.
     */
    EventModeEvent (InputManager.EventMode eventMode) {
	this.eventMode = eventMode;
    }

    /**
     * Returns the new event mode of this event.
     */
    InputManager.EventMode getEventMode () {
	return eventMode;
    }
}
