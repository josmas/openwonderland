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

import com.jme.math.Vector2f;
import com.jme.math.Vector3f;
import com.jme.scene.Node;
import java.awt.event.MouseEvent;
import org.jdesktop.wonderland.client.input.Event;
import org.jdesktop.mtgame.PickDetails;
import org.jdesktop.wonderland.common.ExperimentalAPI;
import java.util.logging.Logger;
import org.jdesktop.mtgame.CollisionComponent;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.wonderland.client.input.InputPicker;
import org.jdesktop.wonderland.client.jme.ThirdPersonCameraProcessor;
import org.jdesktop.wonderland.common.InternalAPI;

/**
 * An event which indicates that a mouse drag action occurred. 
 *
 * @author deronj
 */

@ExperimentalAPI
public class MouseDraggedEvent3D extends MouseMovedEvent3D {
    
    private static final Logger logger = Logger.getLogger(MouseDraggedEvent3D.class.getName());

    static {
	/** Allocate this event type's class ID. */
	EVENT_CLASS_ID = Event.allocateEventClassID();
    }

    /** The raw pick details of the actual pick hit. */
    protected PickDetails hitPickDetails;

    /** Default constructor (for cloning) */
    protected MouseDraggedEvent3D () {}

    /**
     * Create a new MouseDraggedEvent3D with a null pickDetails from an AWT mouse event.
     * @param awtEvent The AWT event
     */
    MouseDraggedEvent3D (MouseEvent awtEvent) {
        this(awtEvent, null);
    }

    /**
     * Create a new MouseDraggedEvent3D from an AWT mouse event.
     * @param awtEvent The AWT event
     * @param pickDetails The pick data for the event.
     */
    MouseDraggedEvent3D (MouseEvent awtEvent, PickDetails pickDetails) {
        super(awtEvent, pickDetails);
    }

    /**
     * Used by InputPicker to specify the raw hit pick details of this drag event.
     * <br>
     * INTERNAL ONLY
     */
    @InternalAPI
    public void setHitPickDetails (PickDetails hitPickDetails) {
	this.hitPickDetails = hitPickDetails;
    }

    /**
     * Returns the raw hit pick details of this drag event.
     */
    public PickDetails getHitPickDetails () {
	return hitPickDetails;
    }

    /**
     * Returns the actually entity hit by the event.
     */
    public Entity getHitEntity () {
	return InputPicker.pickDetailsToEntity(hitPickDetails);
    }

    /**
     * Returns the distance from the eye to the intersection point, based on the actual hit pick details.
     * which were calculated by the input system. (This distance is in world coordinates). If the event has 
     * no hit pick details, 0 is returned. 
     */
   public float getHitDistance () {
	if (hitPickDetails == null) {
	    return 0f;
	} else {
	    return hitPickDetails.getDistance();
	}
    }

    /**
     * Returns the intersection point in world coordinates, based on the actual hit pick details.
     */
    public Vector3f getHitIntersectionPointWorld () {
	if (hitPickDetails == null) {
	    return null;
	} else {
	    return hitPickDetails.getPosition();
	}
    }

    /**
     * Returns the intersection point in object (node) local coordinates, based on the actual hit 
     * pick details.
     */
    public Vector3f getHitIntersectionPointLocal () {
	if (hitPickDetails == null) {
	    return null;
	} else {
	    Vector3f posWorld = hitPickDetails.getPosition();
	    if (posWorld == null) return null;
	    CollisionComponent cc = hitPickDetails.getCollisionComponent();
	    Node node = cc.getNode();
	    node.getLocalToWorldMatrix(world2Local);
	    world2Local.invert();
	    return world2Local.mult(hitPickDetails.getPosition(), new Vector3f());
	}
    }

    /**
     * Returns the drag vector in world coordinates relative to the last mouse button press point.
     * While dragging, the returned value is the pointer movement vector projected into the plane 
     * of the drag start (mouse button press) point.
     * @param ret An Vector3f in which to store the drag vector. If null a new vector is created.
     * @return The argument ret is returned. If it was null a new vector is returned.
     */
    public Vector3f getDragVectorWorld (Vector3f dragStartWorld, Vector3f ret) {
        if (ret == null) {
            ret = new Vector3f();
        }
	
	logger.fine("dragStartWorld rel = " + dragStartWorld);

	// The current world position of the eye
	Vector3f eyeWorld = InputPicker3D.getInputPicker().getCameraPosition(null);
	logger.fine("eyeWorld = " + eyeWorld);

	// The float movement vector in screen space
	Vector2f scrPos = new Vector2f(
				     (float)(((MouseEvent)awtEvent).getX() - MouseButtonEvent3D.xLastPress),
                                     (float)(((MouseEvent)awtEvent).getY() - MouseButtonEvent3D.yLastPress));
	logger.fine("scrPos = " + scrPos);

	Vector2f pressXY = new Vector2f((float)MouseButtonEvent3D.xLastPress, 
					(float)MouseButtonEvent3D.yLastPress);
	Vector3f pressWorld = ((InputManager3D)InputManager3D.getInputManager()).
	    getCamera().getWorldCoordinates(pressXY, 0f);

	Vector2f dragXY = new Vector2f((float)((MouseEvent)awtEvent).getX(),
				       (float)((MouseEvent)awtEvent).getY());
	Vector3f dragWorld = ((InputManager3D)InputManager3D.getInputManager()).
	    getCamera().getWorldCoordinates(dragXY, 0f);

	// The world position of this event (in the view plane)
	Vector3f thisWorld = ((InputManager3D)InputManager3D.getInputManager()).
	    getCamera().getWorldCoordinates(scrPos, 0f);
	logger.fine("thisWorld = " + thisWorld);

	// The displacement vector of this event from the center of the drag plane
	ret.x = (dragWorld.x - pressWorld.x) * (dragStartWorld.z - eyeWorld.z) / (thisWorld.z - eyeWorld.z);
	ret.y = (pressWorld.y - dragWorld.y) * (dragStartWorld.z - eyeWorld.z) / (thisWorld.z - eyeWorld.z);
	ret.z = 0f;
	logger.info("dragVector = " + ret);

	return ret;
    }

    /** {@inheritDoc} */
    @Override
    public String toString () {
	// TODO: add internal state when drag methods are added
	return "Mouse Drag";
    }

    /** 
     * {@inheritDoc}
     * <br>
     * If event is null, a new event of this class is created and returned.
     * <br>
     * NOTE: any state set by <code>setPressPointScreen</code> and <code>getDragVectorWorld</copy>
     * is not copied into the newly cloned object.
     */
    @Override
    public Event clone (Event event) {
	if (event == null) {
	    event = new MouseDraggedEvent3D();
	    ((MouseDraggedEvent3D)event).hitPickDetails = hitPickDetails;
	}
	return super.clone(event);
    }
}
