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

    /** 
     * Initialize the input manager to receive input events from the given AWT canvas
     * and start the input manager running. This method does not define a camera
     * component, so picking on events will not start occuring until a camera component
     * is specified with a subsequent call to <code>setCameraComponent</code>.
     *
     * @param canvas The AWT canvas which generates AWT user events.
     */
    public void initialize (Canvas canvas) {
	initialize(canvas, null);
    }

    /** 
     * Initialize the input manager to receive input events from the given AWT canvas
     * and start the input manager running. The input manager will perform picks with the
     * given camera. This routine can only be called once. To subsequently change the 
     * camera, use <code>setCameraComponent</code>.
     *
     * @param canvas The AWT canvas which generates AWT user events.
     * @param cameraComp The mtgame camera component to use for picking operations.
     */
    public void initialize (Canvas canvas, CameraComponent cameraComp) {
	setCameraComponent(cameraComp);
	super.initialize(canvas);
    }

    /** 
     * Specify the camera component to be used for picking.
     *
     * @param cameraComp The mtgame camera component to use for picking operations.
     */
    public void setCameraComponent (CameraComponent cameraComp) {
	inputPicker.setCameraComponent(cameraComp);
    }

    /** 
     * Returns the camera component that is used for picking.
     */
    public CameraComponent getCameraComponent () {
	return inputPicker.getCameraComponent();
    }
}
