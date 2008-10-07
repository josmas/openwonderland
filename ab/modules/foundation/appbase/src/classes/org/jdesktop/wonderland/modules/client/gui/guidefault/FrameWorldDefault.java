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

import com.jme.scene.Node;
import java.util.logging.Logger;
import org.jdesktop.wonderland.modules.appbase.client.AppCell;
import org.jdesktop.wonderland.modules.appbase.client.ControlArb;
import org.jdesktop.wonderland.modules.appbase.client.Window2DFrame;
import org.jdesktop.wonderland.modules.appbase.client.Window2DView;
import org.jdesktop.wonderland.modules.appbase.client.WindowView;
import org.jdesktop.wonderland.common.ExperimentalAPI;

/**
 * A simple implementation of Window2DFrame that uses 3D rendering.
 *
 * @author deronj
 */ 

@ExperimentalAPI
public class FrameWorldDefault extends Window2DFrame {

    private static final Logger logger = Logger.getLogger(FrameWorldDefault.class.getName());

    /** The height of the header */
    public static final float HEADER_HEIGHT = /* 0.2f */ 6.3f;

    /** The thickness (in the plane of the frame) of the other parts of the border */
    public static final float SIDE_THICKNESS = /*0.07f*/ 3.0f;

    /** The width of the resize corner - currently the same as a header height */
    public static final float RESIZE_CORNER_WIDTH = HEADER_HEIGHT;

    /** The height of the resize corner - currently the same as a header height */
    public static final float RESIZE_CORNER_HEIGHT = HEADER_HEIGHT;

    /** The frame's header (top side) */
    protected FrameHeader header;

    /** The frame's left side */
    protected FrameSide leftSide;

    /** The frame's right side */
    protected FrameSide rightSide;

    /** The frame's bottom side */
    protected FrameSide bottomSide;

    /** The resize corner */
    protected FrameResizeCorner resizeCorner;

    /** 
     * The root of the frame subgraph. This contains all geometry and is 
     * connected to the local scene graph of the cell.
     */
    protected Node frameNode;

    /** The frame's geometry is connected to its cell */
    protected boolean connectedToCell;

    /**
     * Create a new instance of FrameWorldDefault.
     *
     * @param view The view the frame encloses.
     */
    public FrameWorldDefault (WindowView frameView) {
	super((Window2DView)frameView);

        header = new FrameHeader(view, closeListeners);
	leftSide = new FrameSide(view, FrameSide.Side.LEFT, null/*TODO new Gui2DSide(view)*/);
	rightSide = new FrameSide(view, FrameSide.Side.RIGHT, null/*TODO new Gui2DSide(view)*/);
	bottomSide = new FrameSide(view, FrameSide.Side.BOTTOM, null/*TODO new Gui2DSide(view)*/);
	resizeCorner = new FrameResizeCorner(view, rightSide, bottomSide);

	frameNode = new Node("FrameWorldDefault Node");

	frameNode.attachChild(header);
	//frameNode.attachChild(leftSide);
	//frameNode.attachChild(rightSide);
	//frameNode.attachChild(bottomSide);
	//frameNode.attachChild(resizeCorner);	

	updateVisibility();
    }

    /**
     * {@inheritDoc}
     */
    public void cleanup () {
	super.cleanup();
	disconnect();

	if (header != null) {
	    header.cleanup();
	    header = null;
	}
	if (leftSide != null) {
	    leftSide.cleanup();
	    leftSide = null;
	}
	if (rightSide != null) {
	    rightSide.cleanup();
	    rightSide = null;
	}
	if (bottomSide != null) {
	    bottomSide.cleanup();
	    bottomSide = null;
	}
	if (resizeCorner != null) {
	    resizeCorner.cleanup();
	    resizeCorner = null;
	}
    }

    /**
     * Returns the cell of this view.
     */
    private AppCell getCell () {
	return getView().getWindow().getCell();
    }

    /**
     * Disconnect the frame components from the frame's window view.
     */
    void disconnect () {
	if (view == null) return;


	if (header != null) {
	    frameNode.detachChild(header);
	}
	if (leftSide != null) {
	    frameNode.detachChild(leftSide);
	}
	if (rightSide != null) {
	    frameNode.detachChild(rightSide);
	}
	if (bottomSide != null) {
	    frameNode.detachChild(bottomSide);
	}
	if (resizeCorner != null) {
	    frameNode.detachChild(resizeCorner);
	}

	AppCell cell = getCell();
	if (cell != null && connectedToCell) {
//TODO	    cell.detachLocalChild(frameNode);
	}
    }

    /** 
     * The size of the view has changed. Make the corresponding
     * position and/or size updates for the frame components.
     *
     * @throw InstantiationException if couldn't allocate resources for the visual representation.
     */
    void update () throws InstantiationException {
	// Sometimes some of these are null during debugging
	if (header != null) {
	    header.update();
	}
	if (leftSide != null) {
	    leftSide.update();
	}
	if (rightSide != null) {
	    rightSide.update();
	}
	if (bottomSide != null) {
	    bottomSide.update();
	}
	if (resizeCorner != null) {
	    resizeCorner.update();
	}
	updateVisibility();
    }

    /**
     * Make sure that the frame is visible when it needs to be. It should be visible when the view is visible.
     */
    private void updateVisibility () {
	AppCell cell = getCell();

	((ViewWorldDefault)view).updateVisibility();
	boolean viewIsVisible = ((ViewWorldDefault)view).getActuallyVisible();

	if (viewIsVisible && !connectedToCell) {
	    if (cell == null) {
		logger.warning("Frame is not attached to cell. Cannot make it visible");
	    } else {
		//TODO:cell.attachLocalChild(frameNode);
		connectedToCell = true;
	    }
	} else {
	    if (cell != null) {
//TODO		cell.detachLocalChild(frameNode);
	    }
	    connectedToCell = false;
	}

	updateControl(controlArb);
    }

    /**
     * {@inheritDoc}
     */
    public void setTitle (String title) {
	super.setTitle(title);
	if (header != null) {
	    header.setTitle(title);
	}
    }
    
    /**
     * {@inheritDoc}
     */
    public void updateControl (ControlArb controlArb) {
	if (view == null || ((ViewWorldDefault)view).getActuallyVisible()) return;

	// Sometimes some of these are null during debugging
	if (header != null) {
	    header.updateControl(controlArb);
	}
	if (leftSide != null) {
	    leftSide.updateControl(controlArb);
	}
	if (rightSide != null) {
	    rightSide.updateControl(controlArb);
	}
	if (bottomSide != null) {
	    bottomSide.updateControl(controlArb);
	}
	if (resizeCorner != null) {
	    resizeCorner.updateControl(controlArb);
	}
    }
}

