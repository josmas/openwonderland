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

import com.jme.math.Matrix4f;
import com.jme.math.Vector3f;
import com.jme.scene.Node;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import org.jdesktop.mtgame.CollisionComponent;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.mtgame.PickDetails;
import org.jdesktop.wonderland.client.input.InputPicker;
import org.jdesktop.wonderland.common.ExperimentalAPI;
import org.jdesktop.wonderland.common.InternalAPI;

/**
 * The abstract super class for all Wonderland mouse events.
 *
 * @author deronj
 */

@ExperimentalAPI
public abstract class MouseEvent3D extends InputEvent3D {

    /** The supported button codes. */
    public enum ButtonId {NOBUTTON, BUTTON1, BUTTON2, BUTTON3};
    
    /** The originating AWT mouse event. */
    protected MouseEvent awtEvent;

    /** The pick details for the event */
    private PickDetails pickDetails;

    /** A temporary used for getIntersectionPointLocal */
    Matrix4f world2local = new Matrix4f();

    /**
     * Convert MouseEvent into a MouseEvent3D and change the event id to newID
     * @param awtEvent The originating AWT mouse event
     * @param pickDetails The pick details for the event.
     */
    public MouseEvent3D (MouseEvent awtEvent, PickDetails pickDetails) {
	this.awtEvent = awtEvent;
	this.pickDetails = pickDetails;
    }
    
    /**
     * {@inheritDoc}
     */
    public InputEvent getAwtEvent() {
        return awtEvent;
    }
    
    /**
     * Returns the pick details of the event.
     */
    public PickDetails getPickDetails () {
	return pickDetails;
    }

    /**
     * INTERNAL ONLY
     * <br>
     * Sets the pick details of the event.
     */
    @InternalAPI
    public void setPickDetails (PickDetails pickDetails) {
	this.pickDetails = pickDetails;
    }

    /**
     * Returns the entity hit by the event.
     */
    public Entity getEntity () {
	return InputPicker.pickDetailsToEntity(pickDetails);
    }

    /**
     * Returns which, if any, of the mouse buttons has changed state.
     * @return one of the following enums: NOBUTTON, BUTTON1, BUTTON2 or BUTTON3.
     */
    public ButtonId getButton () {
        ButtonId ret = ButtonId.NOBUTTON;
        
        int button = awtEvent.getButton();
        switch (button) {
            case MouseEvent.BUTTON1:
                ret = ButtonId.BUTTON1;
                break;
            case MouseEvent.BUTTON2:
                ret = ButtonId.BUTTON2;
                break;
            case MouseEvent.BUTTON3:
                ret = ButtonId.BUTTON3;
                break;
            default:
                assert(button == MouseEvent.NOBUTTON);
        }
        
	return ret;
    }

    /**
     * Returns the distance from the eye to the intersection point. (This distance is in world coordinates).
     * If event has no pick details, 0 is returned.
     */
    public float getDistance () {
	if (pickDetails == null) {
	    return 0f;
	} else {
	    return pickDetails.getDistance();
	}
    }

    /**
     * Returns the intersection point in world coordinates.
     */
    public Vector3f getIntersectionPointWorld () {
	if (pickDetails == null) {
	    return null;
	} else {
	    return pickDetails.getPosition();
	}
    }

    /**
     * Returns the intersection point in object (node) local coordinates.
     */
    public Vector3f getIntersectionPointLocal () {
	if (pickDetails == null) {
	    return null;
	} else {
	    Vector3f posWorld = pickDetails.getPosition();
	    if (posWorld == null) return null;
	    CollisionComponent cc = pickDetails.getCollisionComponent();
	    Node node = cc.getNode();
	    synchronized (world2local) {
		node.getLocalToWorldMatrix(null);
		world2local.invert();
		return world2local.mult(pickDetails.getPosition(), new Vector3f());
	    }
	}
    }
}
