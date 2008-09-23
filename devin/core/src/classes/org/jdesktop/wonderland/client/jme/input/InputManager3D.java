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

import java.awt.Canvas;
import org.jdesktop.mtgame.CameraComponent;
import org.jdesktop.wonderland.client.input.InputManager;
import org.jdesktop.wonderland.common.ExperimentalAPI;

/**
 * A singleton container for all of the processor objects in the Wonderland 3D event input subsystem.
 *
 * @author deronj
 */

@ExperimentalAPI
public class InputManager3D extends InputManager {

    /**
     * Return the input manager singleton.
     */
    public static InputManager getInputManager () {
	if (inputManager == null) {
	    inputManager = new InputManager3D();
	}
	return inputManager;
    }

    /**
     * Create a new instance of <code>InputManager3D</code>.
     */
    protected InputManager3D () {
	inputPicker = InputPicker3D.getInputPicker();
        eventDistributor = EventDistributor3D.getEventDistributor();
        inputPicker.setEventDistributor(eventDistributor);
    }
}
