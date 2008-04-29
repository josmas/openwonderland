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
import com.jme.math.Vector3f;
import com.jme.scene.Node;
import com.jme.scene.TriMesh;
import com.jme.scene.batch.TriangleBatch;
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
 * A textured version of a JME Box. The given texture is displayed on multiple faces of the box.
 * Derived from JME Box and presents a similar interface.
 */

public class TexturedBox extends Node {
    
    /** Version number for serializing */
    private static final long serialVersionUID = 1L;

    /** Designates the back (+z) face of the box. */
    public static int BACK   = 0x01;
    
    /** Designates the right side (+x) face of the box. */
    public static int RIGHT  = 0x02;

    /** Designates the front (+z) face of the box. */
    public static int FRONT  = 0x04;

    /** Designates the left side (-x) face of the box. */
    public static int LEFT   = 0x08;

    /** Designates the top side (+y) face of the box. */
    public static int TOP    = 0x10;

    /** Designates the bottom side (-y) face of the box. */
    public static int BOTTOM = 0x20;

    /** The ordinals of this enum are indices for faces[]. These must be in the same order as the bitmasks */
    protected static enum Face { BACK, RIGHT, FRONT, LEFT, TOP, BOTTOM };

    /** The texture which is displayed on the textured surfaces of the box. */
    protected Texture texture;

    /** A bitmask which indicates which faces are textured. */
    protected int texturedFaces;

    /** Extent along the X axis (in both directions). */
    public float xExtent;

    /** Extent along the Y axis (in both directions). */
    public float yExtent;

    /** Extent along the Z axis (in both directions). */
    public float zExtent;

    /** The center of the box (in local coordinates) */
    public final Vector3f center = new Vector3f(0f, 0f, 0f);

    /** The geometry of the faces. */
    protected TriMesh[] faces = new TriMesh[6];

    /**
     * Internal Only: Create a new instance of <code>TexturedBox</code>. The texture is displayed on the front face only.
     * All other attributes must be supplied later. 
     *
     * @param texture The texture to display on the box.
     */
    public TexturedBox (Texture texture) {
	this(texture, FRONT);
    }

    /**
     * Internal Only: Create a new instance of <code>TexturedBox</code>. The texture is displayed on the specified faces.
     * All other attributes must be supplied later. 
     *
     * @param texture The texture to display on the box.
     * @param texturedFaces Specifies the faces on which to display the texture. This is the logical OR 
     * of FRONT, BACK, etc.
     */
    public TexturedBox (Texture texture, int texturedFaces) {
	this(texture, texturedFaces, "TempTexturedBox");
    }

    /**
     * Create a new instance of <code>TexturedBox</code> object. The texture is displayed on the front face only.
     * The center and size vertice information must be supplied later. 
     * 
     * @param texture The texture to display on the box.
     * @param name The name of the scene element. 
     */
    public TexturedBox (Texture texture, String name) {
	this(texture, FRONT, name);
    }

    /**
     * Create a new instance of <code>Textured</code> object. The center and size vertice information must be 
     * supplied later.
     * 
     * @param texture The texture to display on the box.
     * @param texturedFaces Specifies the faces on which to display the texture. This is the logical OR 
     * of FRONT, BACK, etc.
     * @param name The name of the scene element. 
     */
    public TexturedBox (Texture texture, int texturedFaces, String name) {
	super(name);
	this.texture = texture;
	this.texturedFaces = texturedFaces;
    }

    /**
     * Create a new instance of <code>TexturedBox</code> object. The given minimum and maximum points define the size
     * of the box,  but not it's orientation or position. You should use the <code>setLocalTranslation</code> 
     * and <code>setLocalRotation</code> for those attributes. The texture is displayed on the front face only.
     * 
     * @param texture The texture to display on the box.
     * @param name The name of the scene element. 
     * @param min The minimum point that defines the box.
     * @param max The maximum point that defines the box.
     */
    public TexturedBox (Texture texture, String name, Vector3f min, Vector3f max) {
	this(texture, FRONT, name, min, max);
    }

    /**
     * Create a new instance of <code>TexturedBox</code> object. The given minimum and maximum points define the size
     * of the box,  but not it's orientation or position. You should use the <code>setLocalTranslation</code> 
     * and <code>setLocalRotation</code> for those attributes. The texture is displayed on the specified faces.
     * 
     * @param texture The texture to display on the box.
     * @param texturedFaces Specifies the faces on which to display the texture. This is the logical OR 
     * of FRONT, BACK, etc.
     * @param name The name of the scene element. 
     * @param min The minimum point that defines the box.
     * @param max The maximum point that defines the box.
     */
    public TexturedBox (Texture texture, int texturedFaces, String name, Vector3f min, Vector3f max) {
	this(texture, texturedFaces, name);
	setData(min, max);
    }

    /**
     * Create a new instance of <code>TexturedBox</code>. The box has the given center and extends in the x,
     * y, and z out from the center (+ and -) by the given amounts. So, for example, a box with extent 
     * of .5 would be the unit cube. The texture is displayed on the front face only.
     * 
     * @param texture The texture to display on the box.
     * @param name Name of the box.
     * @param center The center of the box.
     * @param xExtent The x extent of the box, in both directions.
     * @param yExtent The y extent of the box, in both directions.
     * @param zExtent The z extent of the box, in both directions.
     */
    public TexturedBox (Texture texture, String name, Vector3f center, float xExtent, float yExtent, float zExtent) {
	this(texture, FRONT, name, center, xExtent, yExtent, zExtent);
    }

    /**
     * Create a new instance of <code>TexturedBox</code>. The box has the given center and extends in the x,
     * y, and z out from the center (+ and -) by the given amounts. So, for example, a box with extent 
     * of .5 would be the unit cube. The texture is displayed on the specified faces.
     * 
     * @param texture The texture to display on the box.
     * @param texturedFaces Specifies the faces on which to display the texture. This is the logical OR 
     * of FRONT, BACK, etc.
     * @param name Name of the box.
     * @param center The center of the box.
     * @param xExtent The x extent of the box, in both directions.
     * @param yExtent The y extent of the box, in both directions.
     * @param zExtent The z extent of the box, in both directions.
     */
    public TexturedBox (Texture texture, int texturedFaces, String name, Vector3f center, float xExtent, 
			float yExtent, float zExtent) {
	this(texture, texturedFaces, name);
	setData(center, xExtent, yExtent, zExtent);
    }

    /**
     * Changes the data of the box so that the two opposite corners are minPoint
     * and maxPoint. The other corners are created from those two points. The vertex/normal/texture/color/index
     * buffers are updated when the data is changed.
     * 
     * @param minPoint The new minPoint of the box.
     * @param maxPoint The new maxPoint of the box.
     */
    public void setData(Vector3f minPoint, Vector3f maxPoint) {
	center.set(maxPoint).addLocal(minPoint).multLocal(0.5f);

	float x = maxPoint.x - center.x;
	float y = maxPoint.y - center.y;
	float z = maxPoint.z - center.z;
	setData(center, x, y, z);
    }

    /**
     * Changes the data of the box so that its center is <code>center</code>
     * and it extends in the x, y, and z directions by the given extent. Note
     * that the actual sides will be 2x the given extent values because the box
     * extends in + & - from the center for each extent.
     * 
     * @param center The center of the box.
     * @param xExtent x extent of the box, in both directions.
     * @param yExtent y extent of the box, in both directions.
     * @param zExtent z extent of the box, in both directions.
     */
    public void setData(Vector3f center, float xExtent, float yExtent, float zExtent) {

        for (int i = 0; i < 6; i++) {
	    if (faces[i] == null) {
		TriMesh mesh = new TriMesh();
		TextureState ts = DisplaySystem.getDisplaySystem().getRenderer().createTextureState();
		ts.setTexture(texture);
		mesh.setRenderState(ts);
		mesh.setModelBound(new BoundingBox());
		mesh.updateModelBound();
		faces[i] = mesh;
		attachChild(mesh);
	    }
	}

	if (center != null) {
	    this.center.set(center);
	}

	this.xExtent = xExtent;
	this.yExtent = yExtent;
	this.zExtent = zExtent;

	setVertexData();
	// TODO: setColorData();
	setNormalData();
	setTextureData();
	setIndexData();
    }

    /**
     * 
     * <code>setVertexData</code> sets the vertex positions that define the
     * box. These eight points are determined from the minimum and maximum
     * point.
     *  
     */
    private void setVertexData() {
	Vector3f[] vert = computeVertices(); // returns 8 vertices

        TriangleBatch batch;

	// Back
	batch = faces[Face.BACK.ordinal()].getBatch(0);
        batch.setVertexBuffer(BufferUtils.createVector3Buffer(batch.getVertexBuffer(), 4));
	batch.setVertexCount(4);
	batch.getVertexBuffer().put(vert[0].x).put(vert[0].y).put(vert[0].z);
	batch.getVertexBuffer().put(vert[1].x).put(vert[1].y).put(vert[1].z);
	batch.getVertexBuffer().put(vert[2].x).put(vert[2].y).put(vert[2].z);
	batch.getVertexBuffer().put(vert[3].x).put(vert[3].y).put(vert[3].z);

	// Right
	batch = faces[Face.RIGHT.ordinal()].getBatch(0);
        batch.setVertexBuffer(BufferUtils.createVector3Buffer(batch.getVertexBuffer(), 4));
	batch.setVertexCount(4);
	batch.getVertexBuffer().put(vert[1].x).put(vert[1].y).put(vert[1].z);
	batch.getVertexBuffer().put(vert[4].x).put(vert[4].y).put(vert[4].z);
	batch.getVertexBuffer().put(vert[6].x).put(vert[6].y).put(vert[6].z);
	batch.getVertexBuffer().put(vert[2].x).put(vert[2].y).put(vert[2].z);

	// Front
	batch = faces[Face.FRONT.ordinal()].getBatch(0);
        batch.setVertexBuffer(BufferUtils.createVector3Buffer(batch.getVertexBuffer(), 4));
	batch.setVertexCount(4);
	batch.getVertexBuffer().put(vert[4].x).put(vert[4].y).put(vert[4].z);
	batch.getVertexBuffer().put(vert[5].x).put(vert[5].y).put(vert[5].z);
	batch.getVertexBuffer().put(vert[7].x).put(vert[7].y).put(vert[7].z);
	batch.getVertexBuffer().put(vert[6].x).put(vert[6].y).put(vert[6].z);

	// Left
	batch = faces[Face.LEFT.ordinal()].getBatch(0);
        batch.setVertexBuffer(BufferUtils.createVector3Buffer(batch.getVertexBuffer(), 4));
	batch.setVertexCount(4);
	batch.getVertexBuffer().put(vert[5].x).put(vert[5].y).put(vert[5].z);
	batch.getVertexBuffer().put(vert[0].x).put(vert[0].y).put(vert[0].z);
	batch.getVertexBuffer().put(vert[3].x).put(vert[3].y).put(vert[3].z);
	batch.getVertexBuffer().put(vert[7].x).put(vert[7].y).put(vert[7].z);

	// Top
	batch = faces[Face.TOP.ordinal()].getBatch(0);
        batch.setVertexBuffer(BufferUtils.createVector3Buffer(batch.getVertexBuffer(), 4));
	batch.setVertexCount(4);
	batch.getVertexBuffer().put(vert[2].x).put(vert[2].y).put(vert[2].z);
	batch.getVertexBuffer().put(vert[6].x).put(vert[6].y).put(vert[6].z);
	batch.getVertexBuffer().put(vert[7].x).put(vert[7].y).put(vert[7].z);
	batch.getVertexBuffer().put(vert[3].x).put(vert[3].y).put(vert[3].z);

	// Bottom
	batch = faces[Face.BOTTOM.ordinal()].getBatch(0);
        batch.setVertexBuffer(BufferUtils.createVector3Buffer(batch.getVertexBuffer(), 4));
	batch.setVertexCount(4);
	batch.getVertexBuffer().put(vert[0].x).put(vert[0].y).put(vert[0].z);
	batch.getVertexBuffer().put(vert[5].x).put(vert[5].y).put(vert[5].z);
	batch.getVertexBuffer().put(vert[4].x).put(vert[4].y).put(vert[4].z);
	batch.getVertexBuffer().put(vert[1].x).put(vert[1].y).put(vert[1].z);
    }

    /**
     * <code>setNormalData</code> sets the normals of each of the box's
     * planes. Normals are only initialized once.
     * 
     */
    private void setNormalData() {
        TriangleBatch batch;

	// Back
	batch = faces[Face.BACK.ordinal()].getBatch(0);
	if (batch.getNormalBuffer() != null) {
	    return;
	}
	batch.setNormalBuffer(BufferUtils.createVector3Buffer(4));
	for (int i = 0; i < 4; i++) {
	    batch.getNormalBuffer().put(0).put(0).put(-1);
	}
	
	// Right
	batch = faces[Face.RIGHT.ordinal()].getBatch(0);
	batch.setNormalBuffer(BufferUtils.createVector3Buffer(4));
	for (int i = 0; i < 4; i++) {
	    batch.getNormalBuffer().put(1).put(0).put(0);
	}
	
	// Front
	batch = faces[Face.FRONT.ordinal()].getBatch(0);
	batch.setNormalBuffer(BufferUtils.createVector3Buffer(4));
	for (int i = 0; i < 4; i++) {
	    batch.getNormalBuffer().put(0).put(0).put(1);
	}
	
	// Left
	batch = faces[Face.LEFT.ordinal()].getBatch(0);
	batch.setNormalBuffer(BufferUtils.createVector3Buffer(4));
	for (int i = 0; i < 4; i++) {
	    batch.getNormalBuffer().put(-1).put(0).put(0);
	}
	
	// Top
	batch = faces[Face.TOP.ordinal()].getBatch(0);
	batch.setNormalBuffer(BufferUtils.createVector3Buffer(4));
	for (int i = 0; i < 4; i++) {
	    batch.getNormalBuffer().put(0).put(1).put(0);
	}
	
	// Bottom
	batch = faces[Face.BOTTOM.ordinal()].getBatch(0);
	batch.setNormalBuffer(BufferUtils.createVector3Buffer(4));
	for (int i = 0; i < 4; i++) {
	    batch.getNormalBuffer().put(0).put(-1).put(0);
	}
    }

    /**
     * Sets the points that define the layer 0 texture coordinates of the box. It's a one-to-one ratio, where each 
     * plane of the box has it's own copy of the texture coordinates. That is, the texture coordinates are repeated for
     * each six faces. 
     */
    private void setTextureData() {
        TriangleBatch batch;
	FloatBuffer tex;
	int mask = 0x1;

	for (int i = 0; i < 6; i++, mask <<= 1) {
            batch = faces[i].getBatch(0);
	    if ((texturedFaces & mask) != 0) {
		batch.getTextureBuffers().set(0,BufferUtils.createVector2Buffer(4));
		tex = batch.getTextureBuffers().get(0);
		tex.put(1).put(0);
		tex.put(0).put(0);
		tex.put(0).put(1);
		tex.put(1).put(1);
	    } else {
		batch.getTextureBuffers().set(0,null);
	    }
	}
    }

    /**
     * Sets the indices into the list of vertices, defining all triangles that constitute the faces of box.
     * indices are only initialized once. 
     */
    private void setIndexData() {
	int[] indices = { 2, 1, 0, 3, 2, 0 };       
        TriangleBatch batch;

	batch = faces[0].getBatch(0);
	if (batch.getIndexBuffer() != null) {
	    return;
	}

	for (int i = 0; i < 6; i++) {
	    batch = faces[i].getBatch(0);
	    batch.setIndexBuffer(BufferUtils.createIntBuffer(indices));
	}
    }

    /**
     * @return a size 8 array of Vectors representing the 8 points of the box.
     */
    public Vector3f[] computeVertices() {
	
	Vector3f akEAxis[] = { Vector3f.UNIT_X.mult(xExtent), Vector3f.UNIT_Y.mult(yExtent),
			       Vector3f.UNIT_Z.mult(zExtent) };

	Vector3f rVal[] = new Vector3f[8];
	rVal[0] = center.subtract(akEAxis[0]).subtractLocal(akEAxis[1])
	    .subtractLocal(akEAxis[2]);
	rVal[1] = center.add(akEAxis[0]).subtractLocal(akEAxis[1])
	    .subtractLocal(akEAxis[2]);
	rVal[2] = center.add(akEAxis[0]).addLocal(akEAxis[1]).subtractLocal(
									    akEAxis[2]);
	rVal[3] = center.subtract(akEAxis[0]).addLocal(akEAxis[1])
	    .subtractLocal(akEAxis[2]);
	rVal[4] = center.add(akEAxis[0]).subtractLocal(akEAxis[1]).addLocal(
									    akEAxis[2]);
	rVal[5] = center.subtract(akEAxis[0]).subtractLocal(akEAxis[1])
	    .addLocal(akEAxis[2]);
	rVal[6] = center.add(akEAxis[0]).addLocal(akEAxis[1]).addLocal(
								       akEAxis[2]);
	rVal[7] = center.subtract(akEAxis[0]).addLocal(akEAxis[1]).addLocal(
									    akEAxis[2]);

	return rVal;
    }

    /**
     * Returns the current center of the box.
     * 
     * @return The box's center.
     */
    public Vector3f getCenter() {
	return center;
    }

    /**
     * Sets the center of the box. Note that even though the center is set,
     * Geometry information is not updated. In most cases, you'll want to use
     * setData()
     * 
     * @param aCenter
     *            The new center.
     */
    public void setCenter(Vector3f aCenter) {
	center.set(aCenter);
    }

    /**
     * Creates a new TexturedBox object containing the same data as this one.
     * 
     * @return The new TexturedBox.
     */
    public Object clone() {
	TexturedBox rVal = new TexturedBox(texture, texturedFaces, getName() + "_clone", (Vector3f) center.clone(), 
					   xExtent, yExtent, zExtent);
	return rVal;
    }

    public void write(JMEExporter e) throws IOException {
        super.write(e);
        OutputCapsule capsule = e.getCapsule(this);
        capsule.write(xExtent, "xExtent", 0);
        capsule.write(yExtent, "yExtent", 0);
        capsule.write(zExtent, "zExtent", 0);
        capsule.write(center, "center", Vector3f.ZERO);
	// TODO: texture attrs
    }

    public void read(JMEImporter e) throws IOException {
        super.read(e);
        InputCapsule capsule = e.getCapsule(this);
        xExtent = capsule.readFloat("xExtent", 0);
        yExtent = capsule.readFloat("yExtent", 0);
        zExtent = capsule.readFloat("zExtent", 0);
        center.set((Vector3f) capsule.readSavable("center", Vector3f.ZERO.clone()));
	// TODO: texture attrs
    }
}