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

import java.awt.event.MouseEvent;
import org.jdesktop.wonderland.client.input.Event;
import org.jdesktop.mtgame.PickDetails;
import org.jdesktop.wonderland.common.ExperimentalAPI;

/**
 * An event which indicates that a mouse drag action occurred. 
 *
 * @author deronj
 */

@ExperimentalAPI
public class MouseDraggedEvent3D extends MouseMovedEvent3D {
    
    static {
	/** Allocate this event type's class ID. */
	EVENT_CLASS_ID = Event.allocateEventClassID();
    }

    /** The canvas of the Wonderland main window */
    // TODO:  private static Canvas3D canvas;
    
    /*
    ** Begin temporaries used by getDragPoint.
    */

    /** The previous cursor position in image plate coords */
    // TODO:     private static Point3d cursorPrevIP = new Point3d();

    /** The current cursor position in image plate coords */
    // TODO:     private Point3d cursorIP = new Point3d();

    /** The transformation from image plate coords to virtual world coords*/
    // TODO:     private Transform3D ip2vw = new Transform3D();

    /** The transformation from virtual world coords image plate coords */
    // TODO:     private Transform3D vw2ip = new Transform3D();

    /** The drag start point in image plate coords */
    // TODO:     private Point3d dragStartIP = new Point3d();

    /** The eye point in image plate coords */
    // TODO:     private Point3d eyeIP = new Point3d();

    /** The drag vector in image plate coords */
    // TODO:     private Vector3d dragVecIP = new Vector3d();

    /** A temporary used to save the image plate cursor position */
    // TODO:     private Point3d cursorSaveIP = new Point3d();

    /*
    ** End temporaries used by getDragPoint.
    */

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
     * Used by MouseButtonEvent to specify the screen coordinates of the last mouse
     * button press.
     */
    /* TODO
    public static void setPressPointScreen (int x, int y) {
	if (canvas == null) {
	    FoundationWinSys fws = FoundationWinSys.getFoundationWinSys();
	    canvas = fws.getCanvas(0);
	}

	// Compute the cursor position of the press in image plate coords
	//System.err.println("pressPointScreen = " + x + " " + y);
	canvas.getPixelLocationInImagePlate(x, y, cursorPrevIP);
	//System.err.println("cursorPrevIP = " + cursorPrevIP);
    }
    */

    /**
     * Returns the drag vector in virtual world coordinates relative to the point at which
     * the drag started. While dragging, the returned value is the cursor movement vector
     * projected into the plane of the drag start point.
     *
     * @param dragStart The intersection point in virtual world coordinates of the 
     * mouse button press event which initiated the drag.
     * @param ret An Point3f object in which to store the drag point
     * @return The argument ret is returned.
     */
    /* TODO
    public Vector3f getDragVectorWorld (Point3f dragStart, Vector3f ret) {
        if (ret == null) {
            ret = new Vector3f();
        }

	// DJ Note: See Wonderland Graph Book #1 p.27 for a diagram
        synchronized (cursorIP) {

	    // Get the transformation from image plate coords to virtual world coords
	    canvas.getImagePlateToVworld(ip2vw);

	    // Get the transformation from virtual world coords to image plate coords
	    vw2ip.set(ip2vw);
	    vw2ip.invert();
        
	    // Get the cursor position in Image Plate coordinates
	    //System.err.println("drag event xy = " + ((MouseEvent)awtEvent).getX() + " " + ((MouseEvent)awtEvent).getY());
	    canvas.getPixelLocationInImagePlate(((MouseEvent)awtEvent).getX(), ((MouseEvent)awtEvent).getY(), cursorIP);
	    //System.err.println("cursorIP = " + cursorIP);

	    // Compute the cursor movement delta in image plate coords
	    // (Note: this is a vector but it is stored in a point).
	    cursorSaveIP.set(cursorIP);
	    cursorIP.sub(cursorPrevIP);
	    //System.err.println("cursorIP vector = " + cursorIP);
        
	    // Compute the drag start point in image plate coords
	    dragStartIP.set(dragStart);
	    vw2ip.transform(dragStartIP);
	    //System.err.println("dragStartIP = " + dragStartIP);
        
	    // Get the eye position in image plate coords
	    canvas.getCenterEyeInImagePlate(eyeIP);
	    //System.err.println("eyeIP = " + eyeIP);

	    // Use proportional triangles to project the cursor movement vector
	    // (which is in the plane of the image plate) onto the plane of the 
	    // drag start point. Note that the result is still in image plate coordinates
	    double zEyeToDragStart = dragStartIP.z - eyeIP.z;
	    double zEyeToImagePlate = eyeIP.z;
	    //System.err.println("zEyeToDragStart = " + zEyeToDragStart);
	    //System.err.println("zEyeToImagePlate = " + zEyeToImagePlate);
	    dragVecIP.x = (cursorIP.x * zEyeToDragStart) / zEyeToImagePlate;
	    dragVecIP.y = (cursorIP.y * zEyeToDragStart) / zEyeToImagePlate;
	    dragVecIP.z = 0;
	    //System.err.println("dragVecIP = " + dragVecIP);

	    // Compute the drag vector in virtual world coords
	    ip2vw.transform(dragVecIP);

            ret.x = (float) -dragVecIP.x;
	    ret.y = (float) -dragVecIP.y;
	    ret.z = (float) -dragVecIP.z;

	    cursorPrevIP = cursorSaveIP;
	    //System.err.println("cursorPrevIP = " + cursorPrevIP);
        }

        return ret;
    }
    */

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
	}
	return super.clone(event);
    }
}
