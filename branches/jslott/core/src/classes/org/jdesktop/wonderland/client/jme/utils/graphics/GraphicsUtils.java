/**
 * Project Wonderland
 * 
 * $RCSfile: AppApertureRect.java,v $
 *
 * Copyright (c) 2008, Sun Microsystems, Inc., All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * $Revision: 1.3 $
 * $Date: 2007/11/05 23:21:55 $
 * $State: Exp $
 */

package org.jdesktop.wonderland.client.jme.utils.graphics;

import com.jme.image.Image;
import com.jme.image.Texture;
import com.jme.math.Vector2f;
import com.jme.math.Vector3f;
import com.jme.renderer.ColorRGBA;
import com.jme.scene.TexCoords;
import com.jme.scene.TriMesh;
import com.jme.scene.state.MaterialState;
import com.jme.scene.state.RenderState;
import com.jme.scene.state.TextureState;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import org.jdesktop.wonderland.common.ExperimentalAPI;

/**
 * Miscellaneous Graphics Utilities
 *
 * @author deronj
 */ 

@ExperimentalAPI
public class GraphicsUtils {

    /**
     * Print a small subset of the given image, namely the upper left 20x20.
     */
    private static void printImageContentsSubset (ByteBuffer buf, int w, int h) {
	// Line size (in ints)
	int lineSize = w * 4;

	w = (w > 20) ? 20 : w;
	h = (h > 20) ? 20 : h;

	IntBuffer ibuf = buf.asIntBuffer();
	int nextLine = 0;
	for (int y = 0; y < h; y++, nextLine += lineSize) {
	    ibuf.position(nextLine);
	    for (int x = 0; x < w; x++) {
		int pixel = ibuf.get();
		System.err.print(Integer.toHexString(pixel) + " ");
	    }
	    System.err.println();
	}
    }
	
    /**
     * Print the attributes of the given image and a small subset of its contents.
     */
    public static void printImage (Image image) {
	System.err.println("Image = " + image);
	if (image == null) return;


	System.err.println("type = " + image.getFormat());
	int w = image.getWidth();
	int h = image.getHeight();
	System.err.println("width/height = " + w + " " + h);

	ByteBuffer data = image.getData(0);
	printImageContentsSubset(data, w, h);
    }

    /**
     * Print the pertinent attributes of the given texture.
     */
    public static void printTexture (Texture texture) {
	System.err.println("texture = " + texture);
	if (texture == null) return;

	System.err.println("id = " + texture.getTextureId());
	System.err.println("apply = " + texture.getApply());
	System.err.println("wrapS = " + texture.getWrap(Texture.WrapAxis.S));
	System.err.println("wrapT = " + texture.getWrap(Texture.WrapAxis.T));
	System.err.println("wrapR = " + texture.getWrap(Texture.WrapAxis.R));
	System.err.println("min filter = " + texture.getMinificationFilter());
	System.err.println("mag filter = " + texture.getMagnificationFilter());
	System.err.println("blendColor = " + texture.getBlendColor());
	System.err.println("environmentalMapMode = " + texture.getEnvironmentalMapMode());
	System.err.println("matrix = " + texture.getMatrix());
           
	printImage(texture.getImage());
    }

    /**
     * Print the pertinent texture attributes of the given texture state.
     */
    private static void printTextureState (TextureState ts) {
	System.err.println("ts = " + ts);
	if (ts == null) return;

	System.err.println("isEnabled = " + ts.isEnabled());

	Texture texture = ts.getTexture();
	printTexture(texture);
    }

    /**
     * Print the pertinent attributes of the given material state.
     */
    private static void printMaterialState (MaterialState ms) {
	System.err.println("ms = " + ms);
	if (ms != null) {
	    System.err.println("isEnabled = " + ms.isEnabled());
	    System.err.println("emissive = " + ms.getEmissive());
	    System.err.println("ambient = " + ms.getAmbient());
	    System.err.println("diffuse = " + ms.getDiffuse());
	    System.err.println("specular = " + ms.getSpecular());
	    System.err.println("shininess = " + ms.getShininess());
	    System.err.println("colorMaterial = " + ms.getColorMaterial());
	    System.err.println("materialFace = " + ms.getMaterialFace());
	}
    }

    /**
     * Print the pertinent attributes of the given render state.
     */
    public static void printRenderState (RenderState rs) {
	System.err.println();
	System.err.println("renderState = " + rs);
	if (rs == null) return;

	if        (rs instanceof MaterialState) {
	    printMaterialState((MaterialState)rs);
	} else if (rs instanceof TextureState) {
	    printTextureState((TextureState)rs);
	} else {
	    throw new RuntimeException("Unsupported render state: " + rs);
	}
    }

    /**
     * Return the next Vector2f in the given FloatBuffer.
     *
     * @param buf The source FloatBuffer.
     * @return The next Vector2f from the buffer.
     */
    private static Vector2f getVector2f (FloatBuffer buf) {
	float x = buf.get();
	float y = buf.get();
	return new Vector2f(x, y);
    }

    /**
     * Return the next Vector3f in the given FloatBuffer.
     *
     * @param buf The source FloatBuffer.
     * @return The next Vector3f from the buffer.
     */
    private static Vector3f getVector3f (FloatBuffer buf) {
	float x = buf.get();
	float y = buf.get();
	float z = buf.get();
	return new Vector3f(x, y, z);
    }

    /**
     * Return the next ColorRGBA in the given FloatBuffer.
     *
     * @param buf The source FloatBuffer.
     * @return The next ColorRGBA from the buffer.
     */
    private static ColorRGBA getColorRGBA (FloatBuffer buf) {
	float r = buf.get();
	float g = buf.get();
	float b = buf.get(2);
	float a = buf.get(3);
	return new ColorRGBA(r, g, b, a);
    }

    /**
     * Print the contents of the given Vector2f.
     */
    private static void printVector2f (Vector2f v) {
	System.err.print("(" + v.x + ", " + v.y + ")");
    }

    /**
     * Print the contents of the given Vector3f.
     */
    private static void printVector3f (Vector3f v) {
	System.err.print("(" + v.x + ", " + v.y + ", " + v.z + ")");
    }

    /**
     * Print the contents of the given geometry, except for texture coords.
     */
    public static void printGeometry (TriMesh triMesh) {
	printGeometry(triMesh, true);
    }

    /**
     * Print the contents of the given ColorRGBA
     */
    private static void printColorRGBA (ColorRGBA v) {
	System.err.print("(" + v.r + ", " + v.g + ", " + v.b + ", " + v.a + ")");
    }

    /**
     * Print the contents of the given geometry.
     *
     * @param printTextureCoords Print texture coordinates if true.
     */
    public static void printGeometry (TriMesh triMesh, boolean printTextureCoords) {
	System.err.println();
	System.err.println("geometry = " + triMesh);

	System.err.println("name = " + triMesh.getName());
	System.err.println("parent = " + triMesh.getParent());
	System.err.println("worldBound = " + triMesh.getWorldBound());
	System.err.println("zOrder = " + triMesh.getZOrder());
	System.err.println("localCullHint = " + triMesh.getLocalCullHint());
	System.err.println("localLightCombineMode = " + triMesh.getLocalLightCombineMode());
	System.err.println("localNormalsMode = " + triMesh.getLocalNormalsMode());
	System.err.println("localRenderQueueMode = " + triMesh.getLocalRenderQueueMode());
	System.err.println("localTextureCombineMode = " + triMesh.getLocalTextureCombineMode());
	System.err.print("localTranslation = "); printVector3f(triMesh.getLocalTranslation()); System.err.println();
	System.err.println("localRotation = " + triMesh.getLocalRotation());
	System.err.print("localScale = "); printVector3f(triMesh.getLocalScale()); System.err.println();
	System.err.print("worldTranslation = "); printVector3f(triMesh.getWorldTranslation()); System.err.println();
	System.err.println("worldRotation = " + triMesh.getWorldRotation());
	System.err.print("worldScale = "); printVector3f(triMesh.getWorldScale()); System.err.println();

	System.err.println("mode = " + triMesh.getMode());
	System.err.println("zOrder = " + triMesh.getZOrder());
	System.err.println("localCullHint = " + triMesh.getLocalCullHint());
	System.err.println("localLightCombineMode = " + triMesh.getLocalLightCombineMode());
	System.err.println("localNormalsMode = " + triMesh.getLocalNormalsMode());
	System.err.println("localRenderQueueMode = " + triMesh.getLocalRenderQueueMode());
	System.err.println("localTextureCombineMode = " + triMesh.getLocalTextureCombineMode());
	System.err.println("defaultColor = " + triMesh.getDefaultColor());
	System.err.println("modelBound = " + triMesh.getModelBound());

	int vertCount = triMesh.getVertexCount();
	FloatBuffer localCoords = triMesh.getVertexBuffer();
	assert localCoords != null;
	FloatBuffer worldCoords = triMesh.getWorldCoords(null);
	assert worldCoords != null;
	localCoords.rewind();
	FloatBuffer localNormals = triMesh.getNormalBuffer();
	FloatBuffer worldNormals = triMesh.getWorldNormals(null);
	if (localNormals != null) {
	    assert worldNormals != null;
	    localNormals.rewind();
	}
	FloatBuffer colors = triMesh.getColorBuffer();
	if (colors != null) {
	    colors.rewind();
	}

	ArrayList<TexCoords> texCoords = null;
	if (printTextureCoords) {
	    texCoords = triMesh.getTextureCoords();
	    assert texCoords != null;
	    assert texCoords.get(0).coords != null;
	    texCoords.get(0).coords.rewind();
	}

	for (int i = 0; i < vertCount; i++) {
	    System.err.print("V" + i + ": ");
	    Vector3f lc = getVector3f(localCoords);
	    System.err.print("lc = "); printVector3f(lc);
	    System.err.print(", wc = "); printVector3f(getVector3f(worldCoords));
	    if (localNormals != null) {
		Vector3f ln = getVector3f(localNormals);
		System.err.print(", ln = "); printVector3f(ln);
		System.err.print(", wn = "); printVector3f(getVector3f(worldNormals));
	    }
	    if (colors != null) {
		System.err.print(", col = "); printColorRGBA(getColorRGBA(colors));
	    }
	    if (printTextureCoords) {
		System.err.print(", tc = "); printVector2f(getVector2f(texCoords.get(0).coords));
	    }
		
	    System.err.println();
	}
			   
	// Note: this really returns the number of indices, not the maximum index
	int idxCount = triMesh.getMaxIndex();
	IntBuffer indices = triMesh.getIndexBuffer();
	if (idxCount <= 0) return;
	assert indices != null;
	System.err.print("indices = ");
	for (int i = 0; i < idxCount; i++) {
	    System.err.print(indices.get(i) + " ");
	}
	System.err.println();
    }
}
