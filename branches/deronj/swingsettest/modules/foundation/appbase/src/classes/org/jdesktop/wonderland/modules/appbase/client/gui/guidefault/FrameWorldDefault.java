/**
 * Project Wonderland
 *
 * Copyright (c) 2004-2009, Sun Microsystems, Inc., All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * Sun designates this particular file as subject to the "Classpath" 
 * exception as provided by Sun in the License file that accompanied 
 * this code.
 */
package org.jdesktop.wonderland.modules.appbase.client.gui.guidefault;

import com.jme.scene.Node;
import com.jme.scene.Spatial;
import java.util.logging.Logger;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.mtgame.RenderComponent;
import org.jdesktop.wonderland.client.jme.ClientContextJME;
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
    public static final float HEADER_HEIGHT = /* 0.2f */ /*6.3f*/ 1.25f;

    /** The thickness (in the plane of the frame) of the other parts of the border */
    public static final float SIDE_THICKNESS = /*0.07f*/ /* 3.0f */ 0.75f;

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

    /** 
     * The root entity for this frame.
     */
    protected Entity entity;

    /**
     * Create a new instance of FrameWorldDefault.
     *
     * @param view The view the frame encloses.
     */
    public FrameWorldDefault (WindowView frameView) {
	super((Window2DView)frameView);

	entity = new Entity("Frame Entity");
	frameNode = new Node("FrameWorldDefault Node");
	RenderComponent rc = 
	    ClientContextJME.getWorldManager().getRenderManager().createRenderComponent(frameNode);
	entity.addComponent(RenderComponent.class, rc);

        header = new FrameHeader(view, closeListeners);
	header.setParentEntity(entity);

	leftSide = new FrameSide(view, FrameSide.Side.LEFT, new Gui2DSide(view));
	leftSide.setParentEntity(entity);

	rightSide = new FrameSide(view, FrameSide.Side.RIGHT, new Gui2DSide(view));
	rightSide.setParentEntity(entity);

	bottomSide = new FrameSide(view, FrameSide.Side.BOTTOM, new Gui2DSide(view));
	bottomSide.setParentEntity(entity);

	resizeCorner = new FrameResizeCorner(view, rightSide, bottomSide);
	resizeCorner.setParentEntity(entity);

	updateVisibility();
    }

    /**
     * {@inheritDoc}
     */
    public void cleanup () {
	super.cleanup();

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
	setParentEntity(null);
	if (entity != null) {
	    entity.removeComponent(RenderComponent.class);
	    frameNode = null;
	    entity = null;
	}
    }
    
    public Entity getEntity () {
	return entity;
    }

    public void setParentEntity (Entity parentEntity) {
	if (entity == null) return;

	// Detach from previous parent entity
	Entity prevParentEntity = entity.getParent();
	if (prevParentEntity != null) {
	    prevParentEntity.removeEntity(entity);
	    RenderComponent rcEntity = (RenderComponent)entity.getComponent(RenderComponent.class);
	    if (rcEntity != null) {
		rcEntity.setAttachPoint(null);
	    }
	}
	
	// Attach to new parent entity
	if (parentEntity != null) {
	    parentEntity.addEntity(entity);

	    RenderComponent rcParentEntity = 
		(RenderComponent) parentEntity.getComponent(RenderComponent.class);
	    RenderComponent rcEntity = (RenderComponent)entity.getComponent(RenderComponent.class);

	    // TODO: hack
	    ClientContextJME.getWorldManager().addEntity(rcEntity.getEntity());

	    if (rcParentEntity != null && rcParentEntity.getSceneRoot() != null && rcEntity != null) {
		rcEntity.setAttachPoint(rcParentEntity.getSceneRoot());
	    }
	}
    }

    /**
     * Returns the cell of this view.
     */
    private AppCell getCell () {
	return getView().getWindow().getCell();
    }

    /** 
     * The size of the view has changed. Make the corresponding
     * position and/or size updates for the frame components.
     *
     * @throw InstantiationException if couldn't allocate resources for the visual representation.
     */
    void update () throws InstantiationException {
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

    /**
     * Attach the event listeners of this frame to the given entity.
     */
    void attachEventListeners (Entity entity) {
	if (header != null) {
	    header.attachEventListeners(entity);
	}
	if (leftSide != null) {
	    leftSide.attachEventListeners(entity);
	}
	if (rightSide != null) {
	    rightSide.attachEventListeners(entity);
	}
	if (bottomSide != null) {
	    bottomSide.attachEventListeners(entity);
	}
	if (resizeCorner != null) {
	    resizeCorner.attachEventListeners(entity);
	}
    }
    
    /**
     * Detach the event listeners of this frame from the entity to which they are attached.
     */
    void detachEventListeners (Entity entity) {
	if (header != null) {
	    header.detachEventListeners(entity);
	}
	if (leftSide != null) {
	    leftSide.detachEventListeners(entity);
	}
	if (rightSide != null) {
	    rightSide.detachEventListeners(entity);
	}
	if (bottomSide != null) {
	    bottomSide.detachEventListeners(entity);
	}
	if (resizeCorner != null) {
	    resizeCorner.detachEventListeners(entity);
	}
    }
}

