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

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.wonderland.client.input.Event;
import org.jdesktop.wonderland.client.input.EventClassListener;
import org.jdesktop.wonderland.client.jme.input.KeyEvent3D;
import org.jdesktop.wonderland.client.jme.input.MouseEvent3D;
import org.jdesktop.wonderland.common.ExperimentalAPI;
import org.jdesktop.wonderland.modules.appbase.client.Window2D;
import org.jdesktop.wonderland.modules.appbase.client.Window2DView;

/**
 * The GUI code for a side of a frame.
 *
 * @author deronj
 */

@ExperimentalAPI
class Gui2DSide extends Gui2D {

    /** For Debug: A listener for key events */
    protected SideKeyListener keyListener;

    /** 
     * Create a new instance of Gui2DSide.
     *
     * @param view The view for which the Gui provides behavior.
     */
    Gui2DSide (Window2DView view) {
	super(view);
    }

    /**
     * {@inheritDoc}
     */
    protected void attachMouseListener (Entity entity) {
	mouseListener = new SideMouseListener();
	mouseListener.addToEntity(entity);
    }

    /**
     * The mouse listener for this GUI.
     */
    protected class SideMouseListener extends Gui2D.MouseListener {

	/**
	 * Called when a 3D event has occurred.
	 */
	public void commitEvent (Event event) {
	    Action action;

	    MouseEvent3D me3d = (MouseEvent3D) event;
	    MouseEvent me = (MouseEvent) me3d.getAwtEvent();

	    // We only recognize config on the border when user has control
	    if (controlArb.hasControl()) {
		action = determineIfConfigAction(me, me3d);
		if (action != null) {
		    performConfigAction(action, me, me3d);
		    return;
		}
	    }

	    action = determineIfMiscAction(me, me3d);
	    if (action != null) {
		performMiscAction(action, me, me3d);
		return;
	    }
	}
    }


    /**
     * For Debug: Register this Gui's key listener on the given entity.
     */
    protected void attachKeyListener (Entity entity) {
	// For debug
	keyListener = new SideKeyListener();
	keyListener.addToEntity(entity);
    }

    /**
     * For Debug: Remove this Gui's key listener from its assigned entity.
     */
    protected void detachKeyListener (Entity entity) {
	// For debug
	if (keyListener != null && entity != null) {
	    keyListener.removeFromEntity(entity);
	}
    }

    /**
     * For Debug: The key listener listener for this GUI.
     */
    protected class SideKeyListener extends EventClassListener {

	/**
	 * {@inheritDoc}
	 */
	public Class[] eventClassesToConsume () {
	    return new Class[] { KeyEvent3D.class };
	}

	/**
	 * {@inheritDoc}
	 */
	public void commitEvent (Event event) {

	    KeyEvent3D ke3d = (KeyEvent3D) event;
	    KeyEvent ke = (KeyEvent) ke3d.getAwtEvent();

	    if (ke3d.isPressed() &&
		ke.getKeyCode() == KeyEvent.VK_W &&
		(ke.getModifiersEx() & KeyEvent.CTRL_DOWN_MASK) != 0) {
		// TODO: notyet window.getApp().printWindowInfosAll();
		return;
	    }
	}
    }

    /**
     * Determine if this is a window configuration action.
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
