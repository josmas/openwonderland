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
package org.jdesktop.wonderland.client.app.base.gui.guidefault;

import com.jme.math.Vector3f;
import com.jme.renderer.ColorRGBA;
import org.jdesktop.wonderland.client.app.base.Window2DView;

/**
 * One of the sides of the frame.
 *
 * @author deronj
 */ 

@ExperimentalAPI
public class FrameSide extends FrameComponent {

    /** The supported sides */
    public enum Side { TOP, LEFT, RIGHT, BOTTOM };

    /** Which side of the frame is this? */
    protected Side whichSide;

    /** The geometry of the side. */
    protected FrameRect rect;

    /** The x coordinate (relative to the view center) of the  upper left corner of the side */
    protected float x;

    /** The y coordinate (relative to the view center) of the upper left corner of the side */
    protected float y;

    /** The width of the side in world coordinates */
    protected float width;

    /** The height of the side in world coordinates */
    protected float height;

    /** 
     * Create a new instance of FrameSide with a default name.
     *
     * @param view The view the frame encloses.
     * @param whichSide The side of the frame.
     */
    public FrameSide (Window2DView view, Side whichSide, /*TODO Gui2D*/ Object gui) {
        this("FrameSide", view, whichSide, gui);
    }

    /** 
     * Create a new instance of FrameSide.
     *
     * @param name The name of the node.
     * @param view The view the frame encloses.
     * @param whichSide The side of the frame.
     */
    public FrameSide (String name, Window2DView view, Side whichSide, /*TODO Gui2D*/ Object gui) {
        super(name, view, gui);
	this.whichSide = whichSide;        
    }


    /**
     * {@inheritDoc}
     */
    public void cleanup () {
        super.cleanup();
	if (rect != null) {
	    removeChild(rect);
	    rect.cleanup();
	    rect = null;
	}
    }

    /**
     * {@inheritDoc}
     */
    public void update () throws InstantiationException {
        updateLayout();

        if (rect == null) {
	
	    // First time creation
	    rect = new FrameRect(view, gui, width, height);
	    attachChild(rect);

	    /* TODO: debug
	    if (!getName().equals("FrameHeader")) {
		attachChild(rect);		
	    }
	    */

        } else {

	    // Size update
	    rect.resize(width, height);

        }

	setTranslation(new Vector3f(x, y, 0f));

	super.update();

	/* TODO: debug 
	rect.printRenderState();
	rect.printGeometry();
	*/
    }

    /**
     * {@inheritDoc}
     */
    public void setColor (ColorRGBA color) {
	if (rect != null) {
	    rect.setColor(color);
	}
    }

    /**
     * {@inheritDoc}
     */
    public ColorRGBA getColor () {
	if (rect != null) {
	    return rect.getColor();
	} else {
	    return null;
	}
    }

    /**
     * Calculate the desired layout, based on the view size.
     */
    protected void updateLayout () {

	// Note: we are moving the side CENTERS in this routine. This is slightly
	// different from the old code.

	float innerWidth = view.getWidth();
	float innerHeight = view.getHeight();
	float sideThickness = FrameWorldDefault.SIDE_THICKNESS;
	
	switch (whichSide) {

	case TOP:
	    x = 0;
	    y = innerHeight/2f + FrameWorldDefault.HEADER_HEIGHT/2f;
	    width = innerWidth + 2f * sideThickness;
	    height = FrameWorldDefault.HEADER_HEIGHT;
	    break;

        case LEFT:
	    System.err.println("innerWidth = " + innerWidth);
	    System.err.println("innerHeight = " + innerHeight);
	    System.err.println("sideThickness = " + sideThickness);
	    x = -innerWidth/2f - sideThickness/2f;
	    y = 0;
	    width = sideThickness;
	    height = innerHeight;
	    System.err.println("x = " + x);
	    System.err.println("y = " + y);
	    System.err.println("w = " + width);
	    System.err.println("h = " + height);
	    break;

        case RIGHT:
	    x = innerWidth/2f + sideThickness/2f;
	    y = FrameWorldDefault.RESIZE_CORNER_HEIGHT / 2f;
	    width = sideThickness;
	    height = innerHeight - FrameWorldDefault.RESIZE_CORNER_HEIGHT;
	    break;

        case BOTTOM:
	    System.err.println("FrameWorldDefault.RESIZE_CORNER_WIDTH = " + FrameWorldDefault.RESIZE_CORNER_WIDTH);
	    System.err.println("sideThickness = " + sideThickness);
	    System.err.println("innerHeight = " + innerHeight);
	    System.err.println("innerWidth = " + innerWidth);
	    x = -FrameWorldDefault.RESIZE_CORNER_WIDTH / 2f - sideThickness / 2f;
	    y = -innerHeight/2f - sideThickness/2f;
	    width = innerWidth + sideThickness - FrameWorldDefault.RESIZE_CORNER_WIDTH;
	    height = sideThickness;
	    System.err.println("x = " + x);
	    System.err.println("y = " + y);
	    System.err.println("width = " + width);
	    System.err.println("height = " + height);
	    break;
	}
    }

    /**
     * Returns the x location of this component.
     */
    public float getX () {
	return x;
    }

    /**
     * Returns the y location of this component.
     */
    public float getY () {
	return y;
    }

    /**
     * Returns the width of this component. 
     */
    public float getWidth () {
	return rect.width;
    }

    /**
     * Returns the height of this component. 
     */
    public float getHeight () {
	return rect.height;
    }

}

