// TODO: register mtgame controller update
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
package org.jdesktop.wonderland.modules.appbase.client;

import com.jme.image.Texture;
import java.awt.Graphics2D;
import com.jmex.awt.swingui.ImageGraphics;
import java.awt.Color;
import java.awt.Point;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.mtgame.NewFrameCondition;
import org.jdesktop.mtgame.ProcessorArmingCollection;
import org.jdesktop.mtgame.ProcessorComponent;
import org.jdesktop.wonderland.client.jme.ClientContextJME;
import org.jdesktop.wonderland.common.ExperimentalAPI;
import java.util.logging.Logger;

/**
 * A rectangular, pixel-based drawing surface (image) onto which 2D graphics can be drawn.  You can draw
 * on the surface's image using the Graphics2D returned by <code>getGraphics</code>.
 *
 * When this drawing surface is associated with a texture (via <code>setTexture</code>) and updating
 * is enabled (via <code>setUpdateEnable</code>) the contents of the surface are continually copied 
 * into the texture. To be specific, once per frame all of the newly drawn pixels in the surface's image
 * are copied into the texture. Initially updating is disabled. It must be explicitly enabled.
 *
 * This class provides to subclasses the protected members (penX, penY) which the subclass can use to indicate the 
 * current position at which drawing operations take place (the "pen"). An XOR cursor is drawn on the 
 * image to visually indicate this position.
 *
 * This class is mainly used by WindowGraphics2D. This drawing surface which is implemented 
 *
 * @author paulby, deronj
 */

@ExperimentalAPI
public class DrawingSurface {

    private static final Logger logger = Logger.getLogger(DrawingSurface.class.getName());

    /* The ImageGraphics onto which drawing for this surface is rendered. */
    protected ImageGraphics imageGraphics;

    /* The X location of the current cursor position (in image coordinates). */
    protected int penX;

    /* The Y location of the current cursor position (in image coordinates). */
    protected int penY;

    /* The color in which to draw the cursor */
    protected Color cursorColor = Color.RED;
    
    /** The width of the surface (in pixels) */
    protected int surfaceWidth;

    /** The height of the surface (in pixels) */
    protected int surfaceHeight;
    
    /** The desired destination texture. */
    private Texture texture;

    /** Whether texture updating is enabled */
    private boolean updateEnable;

    /** The processor performing the updates. */
    private UpdateProcessor updateProcessor;

    /** The entity to which the processor is attached. */
    private Entity updateEntity;

    /** The 2D window which is served by this drawing surface. */
    private Window2D window;

    /**  
     * Create an instance of DrawingSurface.
     * <br>
     * Note: You must do a setSize before using a surface created in this way.
     */
    public DrawingSurface () {}

    /** 
     * Create an instance of DrawingSurface.
     * @param width The width of the surface in pixels.
     * @param height The height of the surface in pixels.
     */
    public DrawingSurface (int width, int height) {
	this();
	setSize(width, height);
    }
    
    /**
     * Clean up resources held.
     */
    public void cleanup () {
	setUpdateEnable(false);
	imageGraphics = null;
	texture = null;
    }

    /**
     * Returns this drawing surface's window.
     */
    public Window2D getWindow () {
	return window;
    }

    /**
     * Specify the window which uses this drawing surface.
     * @param window The 2D window which is served by this drawing surface.
     */
    public void setWindow (Window2D window) {
	this.window = window;
    }

    /**
     * Resize the surface. 
     *
     * @param width The new width of the surface in pixels.
     * @param height The new height of the surface in pixels.
     */
    public synchronized void setSize(int width, int height) {
	imageGraphics = ImageGraphics.createInstance(width, height, 0);
        surfaceWidth = width;
        surfaceHeight = height;

	// Erase new surface to all white
        imageGraphics.setBackground(Color.WHITE);
	imageGraphics.setColor(Color.WHITE);
        imageGraphics.fillRect(0, 0, width, height);
	// TODO: bug this should fill the image with white, but it doesn't! */
	// TODO: GraphicsUtils.printImage(imageGraphics.getImage());

	updateUpdating();
    }
    
    
    /**
     * Initialize the contents of the surface.
     */
    void initializeSurface () {
	initSurface(imageGraphics);
    }

    /**
     * Set the clip of the given Graphics2D to render the entire image of this surface.
     *
     * @param g The Graphics2D whose clip is to be set.
     * @param clipEnabled TODO: I have no idea what this does. Currently seems to be a no-op.
     */
    protected void setClip(Graphics2D g, boolean clipEnabled) {
        // TODO: why do both clauses do the exact same thing?
        if (clipEnabled)
            g.setClip(0, 0, surfaceWidth, surfaceHeight);
        else
            g.setClip(0, 0, surfaceWidth, surfaceHeight);
    }
    
    /**
     * Given an (x,y) coordinate, where both x and y are in the range [-1,1]
     * relative to the center of the image, calculate the corresponding
     * pixel location in the image.
     *
     * @return The corresponding pixel location in the image.
     */
    protected Point computeImagePoint(float x, float y) {
        int pX = surfaceWidth / 2 + (int)((x * surfaceWidth) / 2);
        int pY = surfaceHeight / 2 + (int)((y * surfaceHeight) / 2);
        
        return new Point(pX, pY);
    }
    
    /**
     * Called on surface initialization after the surface has been
     * initially erased. The subclass can draw whatever it wants on 
     * the surface. However, care should be taken to draw in a place
     * that doesn't get overdrawn by future paints.
     *
     * @param g The Graphics2D that must be used during this call 
     * to draw to the surface. Use g instead of getGraphics().
     */
    protected void initSurface (Graphics2D g) {}

    /**
     * Returns a Graphics2D to draw on the surface.
     */
    public Graphics2D getGraphics () {
        return imageGraphics;
    }

    /**
     * Returns the width of the surface.
     */
    public int getWidth () {
	return surfaceWidth;
    }

    /**
     * Returns the height of the surface.
     */
    public int getHeight () {
	return surfaceHeight;
    }

    /**
     * Specify the texture that this surface's contents should be copied into.
     */
    public synchronized void setTexture (Texture texture) {
	this.texture = texture;
	updateUpdating();
    }

    /**
     * Return this surface's associated texture.
     */
    public Texture getTexture () {
	return texture;
    }

    /**
     * Enable or disabling the updating of the texture.
     */
    // TODO: must tie processor enable in with setvisible. 
    public synchronized void setUpdateEnable (boolean enable) {
	if (enable == updateEnable) return;
	updateEnable = enable;
	updateUpdating();
    }

    /**
     * Return whether texture updating is enabled.
     */
    public boolean getUpdateEnable () {
	return updateEnable;
    }

    /**
     * Check whether or not updating should be activated.
     */
    private void updateUpdating () {
	if (updateEnable && imageGraphics != null && texture != null) {
	    if (updateProcessor == null) {
		updateProcessor = new UpdateProcessor();
		updateEntity = new Entity("DrawingSurface updateEntity");
		updateEntity.addComponent(ProcessorComponent.class, updateProcessor);
		ClientContextJME.getWorldManager().addEntity(updateEntity);
		updateProcessor.start();
	    }
	} else {
	    if (updateProcessor != null) {
		updateProcessor.stop();
		ClientContextJME.getWorldManager().removeEntity(updateEntity);
		updateEntity.removeComponent(ProcessorComponent.class);
		updateEntity = null;
		updateProcessor = null;
	    }
	}
    }

    private class UpdateProcessor extends ProcessorComponent {

	/**
	 * Initialze the processor to be called once per frame.
	 */
	// TODO: don't enable until the window is visible
	public void initialize () {
	    start();
	}

	/**
	 * Called once per frame to perform the update.
	 */
	public void compute (ProcessorArmingCollection collection) {
	}

	/**
	 * Called once per frame to perform the update.
	 */
	public void commit (ProcessorArmingCollection collection) {
	    synchronized (DrawingSurface.this) {
		// TODO: doug: okay to be doing a lock and this much work in a commit?
		if (texture.getTextureId() == 0) {
		    if (window == null) return;
		    window.forceTextureIdAssignment();
		    if (texture.getTextureId() == 0) {
			Thread.dumpStack();
			logger.severe("********* imageGraphics.update when texture id is still unassigned!!!!");
		    }
		}
		
		imageGraphics.update(texture, true);
	    }
	}

	private void start () {
	    setArmingCondition(new NewFrameCondition(this));
	}

	private void stop () {
	    setArmingCondition(null);
	}

    }
}
