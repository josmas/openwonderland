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
package org.jdesktop.wonderland.client.cell;

import java.nio.FloatBuffer;

import com.jme.math.Vector3f;
import com.jme.renderer.ColorRGBA;
import com.jme.scene.TriMesh;
import com.jme.scene.batch.TriangleBatch;
import com.jme.util.geom.BufferUtils;

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
        TriangleBatch batch = getBatch(0);
        batch.setVertexCount(4);
        batch.setVertexBuffer(BufferUtils.createVector3Buffer(batch.getVertexCount()));
        batch.setNormalBuffer(BufferUtils.createVector3Buffer(batch.getVertexCount()));
        batch.setColorBuffer(BufferUtils.createColorBuffer(batch.getVertexCount()));
        FloatBuffer tbuf = BufferUtils.createVector2Buffer(batch.getVertexCount());
        setTextureBuffer(0, tbuf);
        batch.setTriangleQuantity(2);
        batch.setIndexBuffer(BufferUtils.createIntBuffer(batch.getTriangleCount() * 3));

        batch.getVertexBuffer().put(-width / 2f).put(0).put(height / 2f);
        batch.getVertexBuffer().put(-width / 2f).put(0).put(-height / 2f);
        batch.getVertexBuffer().put(width / 2f).put(0).put(-height / 2f);
        batch.getVertexBuffer().put(width / 2f).put(0).put(height / 2f);

        batch.getNormalBuffer().put(0).put(1).put(0);
        batch.getNormalBuffer().put(0).put(1).put(0);
        batch.getNormalBuffer().put(0).put(1).put(0);
        batch.getNormalBuffer().put(0).put(1).put(0);

        ColorRGBA c1 = ColorRGBA.blue;
        ColorRGBA c2 = ColorRGBA.cyan;
        ColorRGBA c3 = ColorRGBA.green;
        ColorRGBA c4 = ColorRGBA.red;
        
        batch.getColorBuffer().put(c1.getColorArray());
        batch.getColorBuffer().put(c2.getColorArray());
        batch.getColorBuffer().put(c3.getColorArray());
        batch.getColorBuffer().put(c4.getColorArray());

        tbuf.put(0).put(1);
        tbuf.put(0).put(0);
        tbuf.put(1).put(0);
        tbuf.put(1).put(1);

        batch.getIndexBuffer().put(0);
        batch.getIndexBuffer().put(1);
        batch.getIndexBuffer().put(2);
        batch.getIndexBuffer().put(0);
        batch.getIndexBuffer().put(2);
        batch.getIndexBuffer().put(3);
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
