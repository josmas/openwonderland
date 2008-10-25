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

import com.jme.bounding.BoundingBox;
import com.jme.renderer.ColorRGBA;
import com.jme.scene.shape.Quad;
import com.jme.scene.state.MaterialState;
import com.jme.scene.state.RenderState;
import com.jme.system.DisplaySystem;
import java.util.logging.Logger;
import org.jdesktop.wonderland.modules.appbase.client.WindowView;
import org.jdesktop.wonderland.client.jme.utils.graphics.GraphicsUtils;
import org.jdesktop.wonderland.common.ExperimentalAPI;

/**
 * A non-textured rectangle component. Used in several places in the frame.
 * The the origin (0, 0) of the the local coordinate space is the center of the rectangle.
 * The width and height of the rectangle are as specified.
 *
 * @author deronj
 */ 

@ExperimentalAPI
public class FrameRect extends FrameComponent {

    private static final Logger logger = Logger.getLogger(FrameRect.class.getName());

    /** The width of the side in local coordinates. */
    protected float width;

    /** The height of the side in local coordinates. */
    protected float height;

    /** The quad. */
    protected Quad quad;
    
    /** 
     * Create a new instance of <code>FrameRect</code> with a default name. The size must later be specified 
     * with <code>resize</code>. 
     *
     * @param view The view the frame encloses.
     * @param gui The event handler.
     * @param width The width of rectangle in local coordinates.
     * @param height The height of rectangle in local coordinates.
     */
    public FrameRect (WindowView view, /*TODO: Gui2D*/ Object gui) {
        this("FrameRect", view, gui);
    }

    /** 
     * Create a new instance of <code>FrameRect</code>. The size must later be specified with <code>resize</code>. 
     *
     * @param name The name of the node.
     * @param view The view the frame encloses.
     * @param gui The event handler.
     * @param width The width of rectangle in local coordinates.
     * @param height The height of rectangle in local coordinates.
     */
    public FrameRect (String name, WindowView view, /*TODO: Gui2D*/ Object gui) {
        super(name, view, gui);
    }

    /** 
     * Create a new instance of <code>FrameRect</code> a default name and with the specified size.
     *
     * @param view The view the frame encloses.
     * @param gui The event handler.
     * @param width The width of rectangle in local coordinates.
     * @param height The height of rectangle in local coordinates.
     */
    public FrameRect (WindowView view, /*TODO: Gui2D*/ Object gui, float width, float height) {
        this("FrameRect", view, gui, width, height);
    }

    /** 
     * Create a new instance of <code>FrameRect</code> with the specified size.
     *
     * @param name The name of the node.
     * @param view The view the frame encloses.
     * @param gui The event handler.
     * @param width The width of rectangle in local coordinates.
     * @param height The height of rectangle in local coordinates.
     */
    public FrameRect (String name, WindowView view, /*TODO: Gui2D*/ Object gui, float width, float height) {
        super(name, view, gui);
	try {
	    resize(width, height);
        } catch (InstantiationException ex) {
            logger.warning("Cannot update FrameRect component");
        }
    }

    /**
     * {@inheritDoc}
     */
    public void cleanup () {
        super.cleanup();
	if (quad != null) {
	    detachChild(quad);
	    quad = null;
	}
    }

    /**
     * {@inheritDoc}
     */
    public void update () throws InstantiationException {
	updateLayout();

	if (quad == null) {
	    // Init GUI only once
	    if (gui != null) {
		// TODO: gui.initEventHandling(this);
	    }
	} else {
	    detachChild(quad);
	}

	// Create state
	quad = new Quad("FrameRect-Quad", width, height);
	quad.setModelBound(new BoundingBox());
	quad.updateModelBound();
	attachChild(quad);

	super.update();
    }

    /**
     * Returns the width of this component. 
     */
    public float getWidth () {
	return width;
    }

    /**
     * Returns the height of this component. 
     */
    public float getHeight () {
	return height;
    }

    /**
     * {@inheritDoc}
     */
    public void setColor (ColorRGBA color) {
	if (quad != null) {
	    MaterialState ms = (MaterialState) quad.getRenderState(RenderState.RS_MATERIAL);
	    if (ms == null) {
		ms = DisplaySystem.getDisplaySystem().getRenderer().createMaterialState();
		quad.setRenderState(ms);
	    }
	    ms.setAmbient(new ColorRGBA(color));
	    ms.setDiffuse(new ColorRGBA(color));
	}
    }

    /**
     * {@inheritDoc}
     */
    public ColorRGBA getColor () {
	MaterialState ms = null;
	if (quad != null) {
	    ms = (MaterialState) quad.getRenderState(RenderState.RS_MATERIAL);
	}
	if (ms == null) {
	    return new ColorRGBA(1f, 1f, 1f, 1f);
	} else {
	    return ms.getDiffuse();
	}
    }

    /**
     * Change the size of this component.
     *
     * @param width The new width.
     * @param height The new height.
     */
    public void resize (float width, float height) throws InstantiationException {
	this.width = width;
	this.height = height;
	update();
    }

    /**
     * Calculate the geometry layout.
     */
    protected void updateLayout () {
	// Nothing to do for this class
    }

    /**
     * For debug: Print the contents of this component's render state.
     */
    public void printRenderState () {
	MaterialState ms = (MaterialState) quad.getRenderState(RenderState.RS_MATERIAL);
	GraphicsUtils.printRenderState(ms);
    }    

    /**
     * For debug: Print the contents of this component's geometry
     */
    public void printGeometry () {
	GraphicsUtils.printGeometry(quad);
    }    
}

