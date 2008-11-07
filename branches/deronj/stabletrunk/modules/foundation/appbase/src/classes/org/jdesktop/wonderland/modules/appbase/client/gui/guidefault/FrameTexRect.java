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
import com.jme.image.Texture;
import com.jme.scene.Node;
import com.jme.scene.state.RenderState;
import com.jme.scene.state.TextureState;
import com.jme.system.DisplaySystem;
import java.util.logging.Logger;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.mtgame.RenderComponent;
import org.jdesktop.wonderland.client.jme.ClientContextJME;
import org.jdesktop.wonderland.modules.appbase.client.WindowView;
import org.jdesktop.wonderland.client.jme.utils.graphics.TexturedQuad;
import org.jdesktop.wonderland.client.jme.utils.graphics.GraphicsUtils;
import org.jdesktop.wonderland.common.ExperimentalAPI;

/**
 * A textured rectangle component derived from <code>FrameRect</code>.
 *
 * NOTE: The entire texture is displayed. If you wish to display only a portion of
 * the texture on the object you must subclass this class, override createGeometry, and modify the 
 * texture coordinates appropriately.
 *
 * @author deronj
 */ 

@ExperimentalAPI
public class FrameTexRect extends FrameRect {

    private static final Logger logger = Logger.getLogger(FrameTexRect.class.getName());

    /** The texture of the component. */
    protected Texture texture;

    /** 
     * Create a new instance of <code>FrameTexRect</code> with a default name.
     *
     * @param view The view the frame encloses.
     * @param gui The event handler.
     * @param texture The texture to display in the rectangle.
     * @param width The width of side in local coordinates.
     * @param height The height of side in local coordinates.
     */
    public FrameTexRect (WindowView view, Gui2D gui, Texture texture, float width, float height) {
        this("FrameTexRect", view, gui, texture, width, height);
    }

    /** 
     * Create a new instance of <code>FrameTexRect</code>.
     *
     * @param name The node name.
     * @param view The view the frame encloses.
     * @param gui The event handler.
     * @param texture The texture to display in the rectangle.
     * @param width The width of side in local coordinates.
     * @param height The height of side in local coordinates.
     */
    public FrameTexRect (String name, WindowView view, Gui2D gui, Texture texture, 
			 float width, float height) {
        super(name, view, gui, width, height);
	this.texture = texture;
	try {
	    update();
        } catch (InstantiationException ex) {
            logger.warning("Cannot update FrameTexRect component");
        }
	setTexture(texture);
    }

    /**
     * {@inheritDoc}
     */
    public void cleanup () {
        super.cleanup();
	texture = null;
    }

    /**
     * {@inheritDoc}
     */
    public void update () throws InstantiationException {
	updateLayout();

	System.err.println("********** Enter update: entity = " + entity);

	if (quad == null) {
	    quad = new TexturedQuad(texture, "FrameTexRect-Quad", width, height);
	    quad.setModelBound(new BoundingBox());
	} else {
	    System.err.println("******** quad = " + quad);
	    quad.resize(width, height);
	}
	quad.updateModelBound();

	// This should be the same as FrameComponent.update
	updateColor();
    }

    /**
     * Specify a new texture for this object.
     */
    public void setTexture (Texture texture) {
	TextureState ts = (TextureState) quad.getRenderState(RenderState.RS_TEXTURE);
	if (ts == null) {
	    ts = DisplaySystem.getDisplaySystem().getRenderer().createTextureState();
	    quad.setRenderState(ts);
	}
	ts.setTexture(texture);
    }

    /**
     * Returns the texture of this component.
     */
    public Texture getTexture () {
	return texture;
    }
}
