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

import com.jme.math.Vector3f;
import com.jme.renderer.ColorRGBA;
import java.util.logging.Logger;
import org.jdesktop.wonderland.modules.appbase.client.Window2DView;
import org.jdesktop.wonderland.common.ExperimentalAPI;

/**
 * The resize corner for FrameWorldDefault.
 *
 * @author deronj
 */ 

@ExperimentalAPI
public class FrameResizeCorner extends FrameComponent {

    private static final Logger logger = Logger.getLogger(FrameResizeCorner.class.getName());

    /** The width of this resize corner */
    protected float RESIZE_CORNER_WIDTH = FrameWorldDefault.RESIZE_CORNER_WIDTH;

    /** The height of this resize corner */
    protected float RESIZE_CORNER_HEIGHT = FrameWorldDefault.RESIZE_CORNER_HEIGHT;

    /** The distance of this component above its underlying component */
    protected float Z_OFFSET = 0;

    /** The color the component has when the mouse is inside it */
    protected static final ColorRGBA MOUSE_INSIDE_COLOR = new ColorRGBA(1.0f, 1.0f, 0f, 1.0f);

    /** The horizontal bar */
    protected FrameRect horizBar;

    /** The vertical bar */
    protected FrameRect vertBar;

    /** Whether the mouse pointer is inside this component */
    protected boolean mouseInside;

    /** The origin of the resize corner (in cell local coordinates) */
    protected Vector3f origin;
    
    /** The x position of the horizontal bar (relative to the origin) */
    protected float horizX;

    /** The y position of the horizontal bar (relative to the origin) */
    protected float horizY;

    /** The width of the horizontal bar */
    protected float horizWidthWorld;

    /** The height of the horizontal bar */
    protected float horizHeightWorld;

    /** The x position of the vertical bar (relative to the origin) */
    protected float vertX;

    /** The y position of the vertical bar (relative to the origin) */
    protected float vertY;

    /** The width of the vertical bar */
    protected float vertWidthWorld;

    /** The height of the vertical bar */
    protected float vertHeightWorld;

    /** The bordering right side frame component */
    private FrameSide rightSide;

    /** The bordering bottom side frame component */
    private FrameSide bottomSide;

    /** 
     * Create a new instance of FrameResizeCorner.
     *
     * @param view The frame's view.
     */
    public FrameResizeCorner (Window2DView view, FrameSide rightSide, FrameSide bottomSide) {
        super("FrameResizeCorner", view, null/*TODO new Gui2DResizeCorner(view)*/);
	this.rightSide = rightSide;
	this.bottomSide = bottomSide;
	//TODO ((Gui2DResizeCorner)gui).setComponent(this);
    }

    /**
     * {@inheritDoc}
     */
    public void cleanup () {
	super.cleanup();
	if (horizBar != null) {
	    removeChild(horizBar);
	    horizBar.cleanup();
	    horizBar = null;
	}
	if (horizBar != null) {
	    removeChild(vertBar);
	    horizBar.cleanup();
	    horizBar = null;
	}
    }

    /**
     * {@inheritDoc}
     */
    public void update () throws InstantiationException {
        updateLayout();

	// Translate the resize corner's coordinate system
	setTranslation(origin);

        if (horizBar == null) {
	    
	    // First time creation

	    horizBar = new FrameRect("HorizontalBar", view, gui, horizWidthWorld, horizHeightWorld);
	    attachChild(horizBar);

	    vertBar = new FrameRect("Vertical Bar", view, gui, vertWidthWorld, vertHeightWorld);
	    attachChild(vertBar);

	} else {

	    // Update size
	    horizBar.resize(horizWidthWorld, horizHeightWorld);
	    vertBar.resize(vertWidthWorld, vertHeightWorld);
	}

	// Update position
	horizBar.setTranslation(new Vector3f(horizX, horizY, Z_OFFSET));
	vertBar.setTranslation(new Vector3f(vertX, vertY, Z_OFFSET));

	super.update();
    }

    /**
     * Layout the two bars of the resize corner.
     */
    protected void updateLayout () {

	// First make sure that the geometry of the neighboring components is up to date
	rightSide.updateLayout();
	bottomSide.updateLayout();

	// Origin of the resize corner coordinate system is the lower right
	// corner of the view.
	origin = new Vector3f(0f, 0f, Z_OFFSET);

	horizX = (view.getWidth() - FrameWorldDefault.RESIZE_CORNER_WIDTH) / 2f;
	horizY = (-view.getHeight() - FrameWorldDefault.SIDE_THICKNESS) / 2f;
	horizWidthWorld = RESIZE_CORNER_WIDTH;
	horizHeightWorld = FrameWorldDefault.SIDE_THICKNESS; 

	vertX = (view.getWidth() + FrameWorldDefault.SIDE_THICKNESS) / 2f;
	vertY = (-view.getHeight() - FrameWorldDefault.SIDE_THICKNESS + RESIZE_CORNER_HEIGHT) / 2f;
	vertWidthWorld = FrameWorldDefault.SIDE_THICKNESS;
	vertHeightWorld = RESIZE_CORNER_HEIGHT + FrameWorldDefault.SIDE_THICKNESS;

	logger.warning("vertX = " + vertX);
	logger.warning("vertY = " + vertY);
	logger.warning("vertWidthWorld = " + vertWidthWorld);
	logger.warning("vertHeightWorld = " + vertHeightWorld);

    }

    /**
     * {@inheritDoc}
     */
    public void setColor (ColorRGBA color) {
	if (horizBar != null) {
	    logger.warning("horiz color = " + color);
	    horizBar.setColor(color);
	}
	if (vertBar != null) {
	    logger.warning("vert color = " + color);
	    vertBar.setColor(color);
	}
    }

    /**
     * {@inheritDoc}
     */
    public ColorRGBA getColor () {
	if (horizBar != null) {
	    return horizBar.getColor();
	} else {
	    return null;
	}
    }

    /**
     * Specify whether the mouse pointer is inside the close button.
     *
     * @param inside True if the mouse pointer is inside.
     */
    public void setMouseInside (boolean inside) {
	if (mouseInside == inside) return;
	mouseInside = inside;

	if (mouseInside) {
	    // Use the underlay to highlight button when mouse is inside
	    setColor(MOUSE_INSIDE_COLOR);
	} else {
	    // When mouse is outside make underlay the same color as the 
	    // underlaying component
	    if (controlArb.hasControl()) {
		setColor(HAS_CONTROL_COLOR);
	    } else {
		setColor(NO_CONTROL_COLOR);
	    }
	}               
    }
}

