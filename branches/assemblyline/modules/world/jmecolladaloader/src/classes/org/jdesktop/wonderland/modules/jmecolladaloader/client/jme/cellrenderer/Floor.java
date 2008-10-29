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
package org.jdesktop.wonderland.modules.jmecolladaloader.client.jme.cellrenderer;

import java.nio.FloatBuffer;

import com.jme.math.Vector3f;
import com.jme.renderer.ColorRGBA;
import com.jme.scene.TexCoords;
import com.jme.scene.TriMesh;
//import com.jme.scene.batch.TriangleBatch;
import com.jme.scene.state.MaterialState;
import com.jme.scene.state.RenderState;
import com.jme.util.geom.BufferUtils;
import org.jdesktop.wonderland.client.jme.ClientContextJME;

public class Floor extends TriMesh {

    public Floor() {
    }

    /**
     * Constructor creates a new <code>Quad</code> object. That data for the
     * <code>Quad</code> is not set until a call to <code>initialize</code>
     * is made.
     * 
     * @param name
     *            the name of this <code>Quad</code>.
     */
    public Floor(String name) {
        this(name, 1, 1);
    }

    /**
     * Constructor creates a new <code>Quade</code> object with the provided
     * width and height.
     * 
     * @param name
     *            the name of the <code>Quad</code>.
     * @param width
     *            the width of the <code>Quad</code>.
     * @param height
     *            the height of the <code>Quad</code>.
     */
    public Floor(String name, float width, float height) {
        super(name);
        initialize(width, height);
    }

    /**
     * 
     * <code>initialize</code> builds the data for the <code>Quad</code>
     * object.
     * 
     * 
     * @param width
     *            the width of the <code>Quad</code>.
     * @param height
     *            the height of the <code>Quad</code>.
     */
    public void initialize(float width, float height) {
        setVertexCount(4);
        setVertexBuffer(BufferUtils.createVector3Buffer(getVertexCount()));
        setNormalBuffer(BufferUtils.createVector3Buffer(getVertexCount()));
//        setColorBuffer(BufferUtils.createColorBuffer(getVertexCount()));
        
        FloatBuffer tbuf = BufferUtils.createVector2Buffer(getVertexCount());
        setTextureCoords(new TexCoords(tbuf));
        
        setTriangleQuantity(2);
        setIndexBuffer(BufferUtils.createIntBuffer(getTriangleCount() * 3));

        getVertexBuffer().put(-width / 2f).put(0).put(height / 2f);
        getVertexBuffer().put(-width / 2f).put(0).put(-height / 2f);
        getVertexBuffer().put(width / 2f).put(0).put(-height / 2f);
        getVertexBuffer().put(width / 2f).put(0).put(height / 2f);

        getNormalBuffer().put(0).put(1).put(0);
        getNormalBuffer().put(0).put(1).put(0);
        getNormalBuffer().put(0).put(1).put(0);
        getNormalBuffer().put(0).put(1).put(0);

//        ColorRGBA c1 = ColorRGBA.blue;
//        ColorRGBA c2 = ColorRGBA.cyan;
//        ColorRGBA c3 = ColorRGBA.green;
//        ColorRGBA c4 = ColorRGBA.red;
//        
//        getColorBuffer().put(c1.getColorArray());
//        getColorBuffer().put(c2.getColorArray());
//        getColorBuffer().put(c3.getColorArray());
//        getColorBuffer().put(c4.getColorArray());

        tbuf.put(0).put(1);
        tbuf.put(0).put(0);
        tbuf.put(1).put(0);
        tbuf.put(1).put(1);

        getIndexBuffer().put(0);
        getIndexBuffer().put(1);
        getIndexBuffer().put(2);
        getIndexBuffer().put(0);
        getIndexBuffer().put(2);
        getIndexBuffer().put(3);
    }

    /**
     * <code>getCenter</code> returns the center of the <code>Quad</code>.
     * 
     * @return Vector3f the center of the <code>Quad</code>.
     */
    public Vector3f getCenter() {
        return worldTranslation;
    }
}
