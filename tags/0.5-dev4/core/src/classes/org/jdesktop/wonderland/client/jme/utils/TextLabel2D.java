/*
 * Sample code form JME wiki, http://jmonkeyengine.com/wiki/doku.php?id=billboard_awt_label
 */

package org.jdesktop.wonderland.client.jme.utils;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.util.Arrays;

import com.jme.image.Texture;
import com.jme.image.Texture.MagnificationFilter;
import com.jme.image.Texture.MinificationFilter;
import com.jme.math.FastMath;
import com.jme.math.Vector2f;
import com.jme.renderer.Renderer;
import com.jme.scene.BillboardNode;
import com.jme.scene.Spatial.LightCombineMode;
import com.jme.scene.shape.Quad;
import com.jme.scene.state.BlendState;
import com.jme.scene.state.TextureState;
import com.jme.scene.state.BlendState.TestFunction;
import com.jme.system.DisplaySystem;
import com.jme.util.TextureManager;

public class TextLabel2D {
    private String text;
    private float blurIntensity = 0.1f;
    private int kernelSize = 5;
    private ConvolveOp blur;
    private Color foreground = new Color(1f, 1f, 1f);
    private Color background = new Color(0f, 0f, 0f);
    private float fontResolution = 40f;
    private int shadowOffsetX = 2;
    private int shadowOffsetY = 2;
    private Font font;

    public TextLabel2D(String text) {
        this.text = text;
        updateKernel();
        setFont(Font.decode("Sans PLAIN 40"));
    }

    public void setFont(Font font){
        this.font = font;
    }

    public void setShadowOffsetX(int offsetPixelX){
        shadowOffsetX = offsetPixelX;
    }
    public void setShadowOffsetY(int offsetPixelY){
        shadowOffsetY = offsetPixelY;
    }
    public void setBlurSize(int kernelSize){
        this.kernelSize = kernelSize;
        updateKernel();
    }
    public void setBlurStrength(float strength){
        this.blurIntensity = strength;
        updateKernel();
    }
    public void setFontResolution(float fontResolution){
        this.fontResolution = fontResolution;
    }

    private void updateKernel() {
        float[] kernel = new float[kernelSize*kernelSize];
        Arrays.fill(kernel, blurIntensity);
        blur = new ConvolveOp(new Kernel(kernelSize, kernelSize, kernel));
    }

    /**
     *
     * @param scaleFactors is set to the factors needed to adjust texture coords
     * to the next-power-of-two- sized resulting image
     */
    private BufferedImage getImage(Vector2f scaleFactors){
        BufferedImage tmp0 = new BufferedImage(10, 10, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = (Graphics2D) tmp0.getGraphics();
        Font drawFont = font.deriveFont(fontResolution);
        g2d.setFont(drawFont);
        Rectangle2D b = g2d.getFontMetrics().getStringBounds(text, g2d);

        int actualX = (int)b.getWidth()+kernelSize+1+shadowOffsetX;
        int actualY = (int)b.getHeight()+kernelSize+1+shadowOffsetY;

        int desiredX = FastMath.nearestPowerOfTwo(actualX);
        int desiredY = FastMath.nearestPowerOfTwo(actualY);

        if(scaleFactors != null){
            scaleFactors.x = (float)actualX/desiredX;
            scaleFactors.y = (float)actualY/desiredY;
        }

        tmp0 = new BufferedImage(desiredX, desiredY, BufferedImage.TYPE_INT_ARGB);

        g2d = (Graphics2D) tmp0.getGraphics();
        g2d.setFont(drawFont);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int textX = kernelSize/2;
        int textY = g2d.getFontMetrics().getMaxAscent() - kernelSize/2;

        g2d.setColor(background);
        g2d.drawString(text, textX + shadowOffsetX, textY + shadowOffsetY);

        BufferedImage ret = blur.filter(tmp0, null);

        g2d = (Graphics2D) ret.getGraphics();
        g2d.setFont(drawFont);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        g2d.setColor(foreground);
        g2d.drawString(text, textX, textY);

        return ret;
    }

    public Quad getQuad(float height){
        Vector2f scales = new Vector2f();
        BufferedImage img = getImage(scales);
        float w = img.getWidth() * scales.x;
        float h = img.getHeight() * scales.y;
        float factor = height / h;
        Quad ret = new Quad("textLabel2d", w * factor , h * factor);
        TextureState ts = DisplaySystem.getDisplaySystem().getRenderer().createTextureState();
        Texture tex = TextureManager.loadTexture(img, MinificationFilter.BilinearNoMipMaps, MagnificationFilter.Bilinear, true);

//        TexCoords texCo = ret.getTextureCoords(0);
//        texCo.coords = BufferUtils.createFloatBuffer(16);
//        texCo.coords.rewind();
//        for(int i=0; i < texCo.coords.limit(); i+=2){
//            float u = texCo.coords.get();
//            float v = texCo.coords.get();
//            texCo.coords.put(u*scales.x);
//            texCo.coords.put(v*scales.y);
//        }
//        ret.setTextureCoords(texCo);
//        ret.updateGeometricState(0, true);

//        tex.setScale(new Vector3f(scales.x, scales.y, 1));
        ts.setTexture(tex);
        ts.setEnabled(true);
        ret.setRenderState(ts);

        ret.setRenderQueueMode(Renderer.QUEUE_TRANSPARENT);

        BlendState as = DisplaySystem.getDisplaySystem().getRenderer().createBlendState();
        as.setBlendEnabled(true);
        as.setTestEnabled(true);
        as.setTestFunction(TestFunction.GreaterThan);
        as.setEnabled(true);
        ret.setRenderState(as);

        ret.setLightCombineMode(LightCombineMode.Off);
        ret.updateRenderState();
        return ret;
    }
    
    public BillboardNode getBillboard(float height){
        BillboardNode bb = new BillboardNode("bb");
        Quad q = getQuad(height);
        bb.attachChild(q);
        return bb;
    }
}