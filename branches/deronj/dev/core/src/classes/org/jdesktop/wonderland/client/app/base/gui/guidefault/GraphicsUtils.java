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
package org.jdesktop.wonderland.client.app.base.gui.guidefault;

import com.jme.image.Image;
import com.jme.image.Texture;
import com.jme.scene.Geometry;
import com.jme.scene.state.AlphaState;
import com.jme.scene.state.MaterialState;
import com.jme.scene.state.RenderState;
import com.jme.scene.state.TextureState;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

/**
 * Miscellaneous Graphics Utilities
 *
 * @author deronj
 */ 

@ExperimentalAPI
public class GraphicsUtils {

    /**
     * Print the given texture transparency mode.
     */
    /*
    private static void printTransparencyMode (int transparencyMode) {
	System.err.print("TransparencyMode = ");
	switch (transparencyMode) {
        case TransparencyAttributes.NONE:
	    System.err.println("NONE");
	    break;
        case TransparencyAttributes.FASTEST:
	    System.err.println("FASTEST");
	    break;
        case TransparencyAttributes.NICEST:
	    System.err.println("NICEST");
	    break;
        case TransparencyAttributes.SCREEN_DOOR:
	    System.err.println("SCREEN_DOOR");
	    break;
        case TransparencyAttributes.BLENDED:
	    System.err.println("BLENDED");
	    break;
	}
    }
    */

    /**
     * Print the given source blend function.
     */
    /*
    private static void printSrcBlendFunction (int srcBlendFunction) {
	System.err.print("SrcBlendFunction = ");
	switch (srcBlendFunction) {
        case TransparencyAttributes.BLEND_ZERO:
	    System.err.println("BLEND_ZERO");
	    break;
        case TransparencyAttributes.BLEND_ONE:
	    System.err.println("BLEND_ONE");
	    break;
        case TransparencyAttributes.BLEND_SRC_ALPHA:
	    System.err.println("BLEND_SRC_ALPHA");
	    break;
        case TransparencyAttributes.BLEND_ONE_MINUS_SRC_ALPHA:
	    System.err.println("BLEND_ONE_MINUS_SRC_ALPHA");
	    break;
        case TransparencyAttributes.BLEND_DST_COLOR:
	    System.err.println("BLEND_DST_COLOR");
	    break;
        case TransparencyAttributes.BLEND_ONE_MINUS_DST_COLOR:
	    System.err.println("BLEND_ONE_MINUS_DST_COLOR");
	    break;
	}
    }
    */

    /**
     * Print the given destination blend function.
     */
    /*
    private static void printDstBlendFunction (int dstBlendFunction) {
	System.err.print("DstBlendFunction = ");
	switch (dstBlendFunction) {
        case TransparencyAttributes.BLEND_ZERO:
	    System.err.println("BLEND_ZERO");
	    break;
        case TransparencyAttributes.BLEND_ONE:
	    System.err.println("BLEND_ONE");
	    break;
        case TransparencyAttributes.BLEND_SRC_ALPHA:
	    System.err.println("BLEND_SRC_ALPHA");
	    break;
        case TransparencyAttributes.BLEND_ONE_MINUS_SRC_ALPHA:
	    System.err.println("BLEND_ONE_MINUS_SRC_ALPHA");
	    break;
        case TransparencyAttributes.BLEND_SRC_COLOR:
	    System.err.println("BLEND_SRC_COLOR");
	    break;
        case TransparencyAttributes.BLEND_ONE_MINUS_SRC_COLOR:
	    System.err.println("BLEND_ONE_MINUS_SRC_COLOR");
	    break;
	}
    }
    */

    /**
     * Print the pertinent attributes of the given alpha state.
     */
    private static void printAlphaState (AlphaState as) {
	System.err.println("as = " + as);
	System.err.println();

	System.err.println("isEnabled = " + as.isEnabled());
	System.err.println("isBlendEnabled = " + as.isBlendEnabled());
	System.err.println("dstFunction = " + as.getDstFunction());
	System.err.println("srcFunction = " + as.getSrcFunction());
    }

    /**
     * Print the given texture environment mode.
     */
    /*
    private static void printTextureMode (int textureMode) {
	System.err.print("TextureMode = ");
	switch (textureMode) {
	case TextureAttributes.BLEND:
	    System.err.println("BLEND");
	    break;
	case TextureAttributes.COMBINE:
	    System.err.println("COMBINE");
	    break;
	case TextureAttributes.DECAL:
	    System.err.println("DECAL");
	    break;
	case TextureAttributes.MODULATE:
	    System.err.println("MODULATE");
	    break;
	case TextureAttributes.REPLACE:
	    System.err.println("REPLACE");
	    break;
	}
    }
    */

    /**
     * Print the given texture boundary mode.
     */
    /*
    private static void printBoundaryMode (String which, int boundaryMode) {
	System.err.print("BoundaryMode" + which + " = ");
	switch(boundaryMode) {
	case Texture.CLAMP:
	    System.err.println("CLAMP");
	    break;
	case Texture.CLAMP_TO_BOUNDARY:
	    System.err.println("CLAMP_TO_BOUNDARY");
	    break;
	case Texture.CLAMP_TO_EDGE:
	    System.err.println("CLAMP_TO_EDGE");
	    break;
	case Texture.WRAP:
	    System.err.println("WRAP");
	    break;
	}
    }
    */

    /**
     * Print the given texture format.
     */
    /*
    private static void printFormat (int format) {
	System.err.print("Format = ");
	switch(format) {
	case Texture.ALPHA:
	    System.err.println("ALPHA");
	    break;
	case Texture.INTENSITY:
	    System.err.println("INTENSITY");
	    break;
	case Texture.LUMINANCE:
	    System.err.println("LUMINANCE");
	    break;
	case Texture.LUMINANCE_ALPHA:
	    System.err.println("LUMINANCE_ALPHA");
	    break;
	case Texture.RGB:
	    System.err.println("RGB");
	    break;
	case Texture.RGBA:
	    System.err.println("RGBA");
	    break;
	}
    }
    */

    /**
     * Print the given texture filter.
     */
    /*
    private static void printFilter (String which, int filter) {
	System.err.print(which + "Filter = ");
	switch(filter) {
	case Texture.FASTEST:
	    System.err.println("FASTEST");
	    break;
	case Texture.NICEST:
	    System.err.println("NICEST");
	    break;
	case Texture.BASE_LEVEL_LINEAR:
	    System.err.println("BASE_LEVEL_LINEAR");
	    break;
	case Texture.BASE_LEVEL_POINT:
	    System.err.println("BASE_LEVEL_POINT");
	    break;
	case Texture.MULTI_LEVEL_LINEAR:
	    System.err.println("MULTI_LEVEL_LINEAR");
	    break;
	case Texture.MULTI_LEVEL_POINT:
	    System.err.println("MULTI_LEVEL_POINT");
	    break;
	case Texture.FILTER4:
	    System.err.println("FILTER4");
	    break;
	}
    }
    */

    /**
     * Print the given texture mipmap mode.
     */
    /*
    private static void printMipMapMode (int mipMapMode) {
	System.err.print("mipMapMode = ");
	switch(mipMapMode) {
	case Texture.BASE_LEVEL:
	    System.err.println("BASE_LEVEL");
	    break;
	case Texture.MULTI_LEVEL_MIPMAP:
	    System.err.println("MULTI_LEVEL_MIPMAP");
	    break;
	}
    }

    /**
     * Print the given image format.
     */
    /*
    private static void printImageFormat (int format) {
	System.err.print("imageFormat = ");
	switch (format) {
	case ImageComponent.FORMAT_CHANNEL8:
	    System.err.println("FORMAT_CHANNEL8");
	    break;
	case ImageComponent.FORMAT_LUM4_ALPHA4:
	    System.err.println("FORMAT_LUM4_ALPHA4");
	    break;
	case ImageComponent.FORMAT_LUM8_ALPHA8:
	    System.err.println("FORMAT_LUM8_ALPHA8");
	    break;
	case ImageComponent.FORMAT_R3_G3_B2:
	    System.err.println("FORMAT_R3_G3_B2");
	    break;
	case ImageComponent.FORMAT_RGB4:
	    System.err.println("FORMAT_RGB4");
	    break;
	case ImageComponent.FORMAT_RGB5:
	    System.err.println("FORMAT_RGB5");
	    break;
	case ImageComponent.FORMAT_RGB5_A1:
	    System.err.println("FORMAT_RGB5_A1");
	    break;
	case ImageComponent.FORMAT_RGB8:
	    System.err.println("FORMAT_RGB8");
	    break;
	case ImageComponent.FORMAT_RGBA4:
	    System.err.println("FORMAT_RGBA4");
	    break;
	case ImageComponent.FORMAT_RGBA8:
	    System.err.println("FORMAT_RGBA8");
	    break;
	}
    }
    */

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
    private static void printImage (Image image) {
	System.err.println("Image = " + image);
	if (image == null) return;


	System.err.println("type = " + image.getType());
	int w = image.getWidth();
	int h = image.getHeight();
	System.err.println("width/height = " + w + " " + h);

	ByteBuffer data = image.getData();
	printImageContentsSubset(data, w, h);
    }

    /**
     * Print the pertinent attributes of the given texture.
     */
    private static void printTexture (Texture texture) {
	System.err.println("texture = " + texture);
	if (texture == null) return;

	System.err.println("id = " + texture.getTextureId());
	System.err.println("apply = " + texture.getApply());
	System.err.println("wrap = " + texture.getWrap());
	System.err.println("filter = " + texture.getFilter());
	System.err.println("mipmap = " + texture.getMipmap());
	System.err.println("mipmapState = " + texture.getMipmapState());
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

	System.err.println();
	System.err.println("isEnabled = " + ts.isEnabled());

	Texture texture = ts.getTexture();
	System.err.println();
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
	System.err.println("rs = " + rs);
	if (rs == null) return;

	if        (rs instanceof MaterialState) {
	    printMaterialState((MaterialState)rs);
	} else if (rs instanceof TextureState) {
	    printTextureState((TextureState)rs);
	} else if (rs instanceof AlphaState) {
	    printAlphaState((AlphaState)rs);
	} else {
	    throw new RuntimeException("Unsupported render state: " + rs);
	}
    }

    /**
     * Print 1 bits in the given vertex format.
     */
    /*
    private static void printVertexFormat (int vertexFormat) {
	if        ((vertexFormat & GeometryArray.COORDINATES) != 0) {
	    System.err.print("COORDINATES ");
	}

	if        ((vertexFormat & GeometryArray.COLOR_3) != 0) {
	    System.err.print("COLOR_3 ");
	} else if ((vertexFormat & GeometryArray.COLOR_4) != 0) {
	    System.err.print("COLOR_4 ");
	}

	if        ((vertexFormat & GeometryArray.NORMALS) != 0) {
	    System.err.print("NORMALS ");
	}

	if        ((vertexFormat & GeometryArray.TEXTURE_COORDINATE_2) != 0) {
	    System.err.print("TEXTURE_COORDINATE_2 ");
	} else if ((vertexFormat & GeometryArray.TEXTURE_COORDINATE_3) != 0) {
	    System.err.print("TEXTURE_COORDINATE_3 ");
	} else if ((vertexFormat & GeometryArray.TEXTURE_COORDINATE_4) != 0) {
	    System.err.print("TEXTURE_COORDINATE_4 ");
	}
    }
    */

    /*
    private static Point3f[] coords = null;
    private static Color3f[] colors3 = null;
    private static Color4f[] colors4 = null;
    private static Vector3f[] normals = null;

    /**
     * Extract the coordinate, color, and normal data from the given geometry array.
     */
    /*
    protected static void getVertexData (GeometryArray ga, int vertexFormat, int vertexCount) {
	if ((vertexFormat & GeometryArray.COORDINATES) != 0) {
	    coords = new Point3f[vertexCount];
	    for (int i = 0; i < vertexCount; i++) {
		coords[i] = new Point3f();
	    }
	    ga.getCoordinates(0, coords);
	}

	if        ((vertexFormat & GeometryArray.COLOR_3) != 0) {
	    colors3 = new Color3f[vertexCount];
	    for (int i = 0; i < vertexCount; i++) {
		colors3[i] = new Color3f();
	    }
	    ga.getColors(0, colors3);
	} else if ((vertexFormat & GeometryArray.COLOR_4) != 0) {
	    colors4 = new Color4f[vertexCount];
	    for (int i = 0; i < vertexCount; i++) {
		colors4[i] = new Color4f();
	    }
	    ga.getColors(0, colors4);
	}

	if ((vertexFormat & GeometryArray.NORMALS) != 0) {
	    normals = new Vector3f[vertexCount];
	    for (int i = 0; i < vertexCount; i++) {
		normals[i] = new Vector3f();
	    }
	    ga.getNormals(0, normals);
	}
    }
    */

    /**
     * Print the coordinates, color, and/or normals given vertex from the geometry array provided 
     * to a previous call to getVertexData.
     */
    /*
    protected static void printVertex (int vertexNum) {
	System.err.print("V" + vertexNum + " ");

	if (coords != null) {
	    System.err.print("Coord = " + coords[vertexNum] + " ");
	}

	if (colors3 != null) {
	    System.err.print("Color3 = " + colors3[vertexNum] + " ");
	} else if (colors4 != null) {
	    System.err.print("Color4 = " + colors4[vertexNum] + " ");
	}

	if (normals != null) {
	    System.err.print("Normal = " + normals[vertexNum] + " ");
	}
    }
    */

    /**
     * Print the contents of the given non-textured geometry.
     */
    public static void printGeometry (Geometry geometry) {
	/*
	if (!(geometry instanceof QuadArray)) {
	    throw new RuntimeException("Geometry type not supported: " + geometry.getClass());
	}

	System.err.println();
	GeometryArray ga = (GeometryArray) geometry;
	int vertexFormat = ga.getVertexFormat();
	System.err.print("VertexFormat = "); printVertexFormat(vertexFormat); System.err.println();
	int vertexCount = ga.getVertexCount();
        System.err.println("VertexCount = " + vertexCount);

	getVertexData(ga, vertexFormat, vertexCount);
	for (int i = 0; i < vertexCount; i++) {
	    printVertex(i);
	    System.err.println();
	}
	*/
    }

    /*
    private static TexCoord2f[] texCoords2;
    private static TexCoord3f[] texCoords3;
    private static TexCoord4f[] texCoords4;
    */

    /**
     * Extract the texture coordinates for texture coordinate set 0 from the given geometry array.
     */
    /*
    protected static void getVertexDataTex (GeometryArray ga, int vertexFormat, int vertexCount) {
	getVertexData(ga, vertexFormat, vertexCount);

	if        ((vertexFormat & GeometryArray.TEXTURE_COORDINATE_2) != 0) {
	    texCoords2 = new TexCoord2f[vertexCount];
	    for (int i = 0; i < vertexCount; i++) {
		texCoords2[i] = new TexCoord2f();
	    }
	    ga.getTextureCoordinates(0, 0, texCoords2);
	} else if ((vertexFormat & GeometryArray.TEXTURE_COORDINATE_3) != 0) {
	    texCoords3 = new TexCoord3f[vertexCount];
	    for (int i = 0; i < vertexCount; i++) {
		texCoords3[i] = new TexCoord3f();
	    }
	    ga.getTextureCoordinates(0, 0, texCoords3);
	} else if ((vertexFormat & GeometryArray.TEXTURE_COORDINATE_4) != 0) {
	    texCoords4 = new TexCoord4f[vertexCount];
	    for (int i = 0; i < vertexCount; i++) {
		texCoords4[i] = new TexCoord4f();
	    }
	    ga.getTextureCoordinates(0, 0, texCoords4);
	}
    }
    */

    /**
     * Print the texture coordinates from the geometry array provided to a previous call to getVertexTex.
     */
    /*
    protected static void printVertexTex (int vertexNum) {
	printVertex(vertexNum);

	if (texCoords2 != null) {
	    System.err.print("TexCoord2 = " + texCoords2[vertexNum] + " ");
	} else if (texCoords3 != null) {
	    System.err.print("TexCoord3 = " + texCoords3[vertexNum] + " ");
	} else if (texCoords4 != null) {
	    System.err.print("TexCoord4 = " + texCoords4[vertexNum] + " ");
	}
    }
    */

    /**
     * Print the contents of the given textured geometry.
     */
    public static void printGeometryTex (Geometry geometry) {
	/*

>>> Only need to print the texcoords

	if (!(geometry instanceof QuadArray)) {
	    throw new RuntimeException("Geometry type not supported: " + geometry.getClass());
	}

	System.err.println();
	GeometryArray ga = (GeometryArray) geometry;
	int vertexFormat = ga.getVertexFormat();
	System.err.print("VertexFormat = "); printVertexFormat(vertexFormat); System.err.println();
	int vertexCount = ga.getVertexCount();
        System.err.println("VertexCount = " + vertexCount);

	getVertexDataTex(ga, vertexFormat, vertexCount);
	for (int i = 0; i < vertexCount; i++) {
	    printVertexTex(i);
	    System.err.println();
	}
	*/
    }
}
