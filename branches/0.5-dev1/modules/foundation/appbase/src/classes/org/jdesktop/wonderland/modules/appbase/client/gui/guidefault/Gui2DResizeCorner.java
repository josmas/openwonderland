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
package org.jdesktop.wonderland.modules.appbase.client.gui.guidefault;

import java.awt.event.MouseEvent;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.wonderland.client.input.Event;
import org.jdesktop.wonderland.client.jme.input.MouseEnterExitEvent3D;
import org.jdesktop.wonderland.client.jme.input.MouseEvent3D;
import org.jdesktop.wonderland.common.ExperimentalAPI;
import org.jdesktop.wonderland.modules.appbase.client.Window2D;
import org.jdesktop.wonderland.modules.appbase.client.Window2DView;

/**
 * The GUI code for the frame resize corner.
 *
 * @author deronj
 */

@ExperimentalAPI
class Gui2DResizeCorner extends Gui2DSide { 

    /** The associated resize corner component */
    protected FrameResizeCorner resizeCorner;

    /** 
     * Create a new instance of Gui2DResizeCorner.
     *
     * @param view The view associated with the component that uses this Gui.
     */
    public Gui2DResizeCorner (Window2DView view) {
	super(view);
    }

    /**
     * {@inheritDoc}
     */
    public void cleanup () {
	super.cleanup();
	resizeCorner = null;
    }

    /**
     * Specify the resize corner component for which this Gui provides behavior.
     *
     * @param resizeCorner The resize corner component.
     */
    public void setComponent (FrameResizeCorner resizeCorner) {
	this.resizeCorner = resizeCorner;
    }

    /**
     * {@inheritDoc}
     */
    protected void attachMouseListener (Entity entity) {
	mouseListener = new ResizeCornerMouseListener();
	mouseListener.addToEntity(entity);
    }

    /**
     * The mouse listener for this GUI.
     */
    protected class ResizeCornerMouseListener extends Gui2DSide.SideMouseListener {

	/**
	 * Called when a 3D event has occurred.
	 */
	public void commitEvent (Event event) {
	    Action action;

	    MouseEvent3D me3d = (MouseEvent3D) event;

	    if (me3d instanceof MouseEnterExitEvent3D &&
		controlArb.hasControl()) {
		resizeCorner.setMouseInside(((MouseEnterExitEvent3D)me3d).isEnter());
	    }

	    super.commitEvent(event);
	}
    }

    /**
     * Determine if this is a window configuration action.
     * are only recognized when the user has control of the window.
     *
     * @param me The AWT event for this 3D mouse event.
     * @param me3d The 3D mouse event.
     */
    protected Action determineIfConfigAction (MouseEvent me, MouseEvent3D me3d) {
	Action action = determineIfToFrontAction (me);
	if (action != null) {
	    return action;
	}

	return super.determineIfConfigAction(me, me3d);
    }

    /**
     * Perform the window configuration action.
     *
     * @param action The configuration action the given event provokes.
     * @param me The AWT event for this 3D mouse event.
     * @param me3d The 3D mouse event.
     */
    protected void performConfigAction (Action action, MouseEvent me, MouseEvent3D me3d) {
	if (action.type == ActionType.TO_FRONT) {
	    ((Window2D)window).toFront();
	    return;
	}

	super.performConfigAction(action, me, me3d);
    }
}
