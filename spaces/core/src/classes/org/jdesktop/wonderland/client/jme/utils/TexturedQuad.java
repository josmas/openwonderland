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

package org.jdesktop.wonderland.client.jme.utils;

import com.jme.bounding.BoundingBox;
import com.jme.image.Texture;
import com.jme.scene.batch.TriangleBatch;
import com.jme.scene.shape.Quad;
import com.jme.scene.state.RenderState;
import com.jme.scene.state.TextureState;
import com.jme.system.DisplaySystem;
import com.jme.util.export.InputCapsule;
import com.jme.util.export.JMEExporter;
import com.jme.util.export.JMEImporter;
import com.jme.util.export.OutputCapsule;
import com.jme.util.geom.BufferUtils;
import java.io.IOException;
import java.nio.FloatBuffer;

/**
 * A quad with a texture displayed on it.
 */

public class TexturedQuad extends Quad {
    
    /** Version number for serializing */
    private static final long serialVersionUID = 1L;

    /** The texture which is displayed on the quad. */
    protected Texture texture;

    /** The width in of the quad, in local coordinates. */
    private float width;

    /** The height in of the quad, in local coordinates. */
    private float height;

    /**
     * Internal Only: Create a new instance of <code>TexturedQuad</code>. 
     * The width and height attributes must be supplied later by calling <code>initialize</code>. 
     *
     * @param texture The texture to display on the quad.
     */
    public TexturedQuad (Texture texture) {
	this(texture, "TempTexturedBox");
    }

    /**
     * Create a new instance of <code>TexturedQuad</code> object. The texture is displayed on the front face only.
     * The center and size vertice information must be supplied later. 
     * 
     * @param texture The texture to display on the quad.
     * @param name The name of the scene element. 
     */
    public TexturedQuad (Texture texture, String name) {
	super(name);
	this.texture = texture;
	initializeTexture();
    }

    /**
     * Create a new instance of <code>TexturedQuad</code> object given a width and height. The quad is centered
     * around its local origin.
     * 
     * @param texture The texture to display on the quad.
     * @param name The name of the scene element. 
     * @param width The width of the quad in local coordinates.
     * @param height The height of the quad in local coordinates.
     */
    public TexturedQuad (Texture texture, String name, float width, float height) {
	super(name, width, height);
	this.texture = texture;
	initializeTexture();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initialize (float width, float height) {
	super.initialize(width, height);
	this.width = width;
	this.height = height;
    }

    /**
     * Initialize texture attributes.
     */
    private void initializeTexture () {
	if (getRenderState(RenderState.RS_TEXTURE) == null) {
	    TextureState ts = DisplaySystem.getDisplaySystem().getRenderer().createTextureState();
	    ts.setTexture(texture);
	    setRenderState(ts);
	    setModelBound(new BoundingBox());
	    updateModelBound();
	}
    }

    /**
     * Creates a new TexturedQuad object containing the same data as this one.
     * 
     * @return The new TexturedQuad.
     */
    public Object clone() {
	TexturedQuad rVal = new TexturedQuad(texture, getName() + "_clone", width, height);
	return rVal;
    }

    public void write(JMEExporter e) throws IOException {
        super.write(e);
        OutputCapsule capsule = e.getCapsule(this);
	// TODO: texture attrs
    }

    public void read(JMEImporter e) throws IOException {
        super.read(e);
        InputCapsule capsule = e.getCapsule(this);
	// TODO: texture attrs
    }
}