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

@InternalAPI
public class InputPicker3D extends InputPicker {

    /**
     * Returns the entity resolver singleton.
     */
    static InputPicker getInputPicker () {
	if (inputPicker == null) {
	    inputPicker = new InputPicker3D();
	}
	return inputPicker;
    }

    /**
     * {@inheritDoc}
     */
    protected abstract Event createWonderlandEvent (AWTEvent awtEvent) {
	Event event;

	if (awtEvent instanceof KeyEvent) {
	    event = new KeyEvent3D((KeyEvent)awtEvent);
	} else if (awtEvent instanceof MouseWheelEvent) {
	    event = new MouseWheelEvent3D((MouseWheelEvent)awtEvent);
	} else if (awtEvent instanceof MouseEvent) {
	    switch (awtEvent.getID()) {
	    case MOUSE_CLICKED:
	    case MOUSE_RELEASED:
	    case MOUSE_PRESSED:
		event = new MouseButtonEvent3D((MouseEvent)awtEvent);
		break;
	    case MOUSE_ENTERED:
	    case MOUSE_EXITED:
		event = new MouseEnterExitEvent3D((MouseEvent)awtEvent);
		break;
	    case MOUSE_MOVED:
		event = new MouseMovedEvent3D((MouseEvent)awtEvent);
		break;
	    case MOUSE_DRAGGED:
		event = new MouseDraggedEvent3D((MouseEvent)awtEvent);
		break;
	    }
	}

	return event;
    }
}