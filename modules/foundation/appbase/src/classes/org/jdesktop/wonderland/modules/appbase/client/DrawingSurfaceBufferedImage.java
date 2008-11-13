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

import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.Paint;
import java.awt.RenderingHints;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ImageObserver;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.RenderableImage;
import java.text.AttributedCharacterIterator;
import java.util.Map;
import java.util.logging.Logger;
import org.jdesktop.mtgame.NewFrameCondition;
import org.jdesktop.wonderland.common.ExperimentalAPI;

/**
 * A type of drawing surface is based on using an AWT Graphics2D to draw to an AWT BufferedImage
 * and then copies the rendering into an ImageGraphics and, ultimately, the Texture. This is not as
 * optimal as DrawingSurfaceImageGraphics, but because it uses a normal AWT Graphics2D its rendering
 * functionality is more complete.
 *
 * @author deronj
 */

@ExperimentalAPI
public class DrawingSurfaceBufferedImage extends DrawingSurfaceImageGraphics {

    private static final Logger logger = Logger.getLogger(DrawingSurfaceBufferedImage.class.getName());

    /** The buffered image. */
    protected BufferedImage bufImage;

    /** 
     * Tracks whether the bufImage is dirty (i.e. has been drawn on). 
     * Note: we must maintain this flag with the buffered image rather then with the DirtyTrackingGraphics
     * because swing makes several copies of the graphics.
     */
    protected ImageDirtyTracker dirtyTracker = new ImageDirtyTracker();

    /** The Graphics2D we return from getGraphics */
    protected DirtyTrackingGraphics g;

    /**  
     * Create an instance of DrawingSurfaceBufferedImage.
     * <br>
     * Note: You must do a setSize before using a surface created in this way.
     */
    public DrawingSurfaceBufferedImage () {}

    /** 
     * Create an instance of DrawingSurface.
     * @param width The width of the surface in pixels.
     * @param height The height of the surface in pixels.
     */
    public DrawingSurfaceBufferedImage (int width, int height) {
	this();
	setSize(width, height);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void cleanup () {
	bufImage = null;
	g = null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void setSize(int width, int height) {
	super.setSize(width, height);

	if (bufImage == null || 
	    width != bufImage.getWidth() || height != bufImage.getHeight()) {
	    bufImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
	}

	// Create a dirty-tracking Graphics2D which renders onto this buffered image
	g = new DirtyTrackingGraphics((Graphics2D)bufImage.getGraphics());
	g.clipRect(0, 0, width, height);

	// Erase the buffered image to all white
	Color bkgdSave = g.getBackground();
        g.setBackground(Color.WHITE);
        g.clearRect(0, 0, width, height);
        g.setBackground(bkgdSave);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Graphics2D getGraphics () {
        return g;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initializeSurface () {
	initSurface(g);
	if (!imageGraphics.drawImage(bufImage, 0, 0, null)) {
	    logger.warning("imageGraphics.drawImage returned false! Skipping image rendering.");
	}
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected UpdateProcessor createUpdateProcessor () {
	return new BufferedImageUpdateProcessor();
    }

    /**
     * A subclass of DrawingSurfaceImageGraphicsBuffer which only performs an update if 
     * the buffered image has been dirtied since it was last cleaned.
     */
    protected class BufferedImageUpdateProcessor extends UpdateProcessor {

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected boolean checkForUpdate () {
	    // In this implementation, we need to check our DirtyTrackingGraphics to see whether it is dirty.
	    // If it is dirty, we copy the entire buffered image into the imageGraphics.
	    if (dirtyTracker.isDirty()) {
		if (!imageGraphics.drawImage(bufImage, 0, 0, null)) {
		    logger.warning("imageGraphics.drawImage returned false! Skipping image rendering.");
		}
		dirtyTracker.clearDirty();
		return true;
	    }
	    return false;
	}

	private void start () {
	    setArmingCondition(new NewFrameCondition(this));
	}

	private void stop () {
	    setArmingCondition(null);
	}
    }

    /**
     * This is an encapsulation of a dirty flag. Used to track whether the buffered
     * image of the parent class has been drawn upon.
     */
    private class ImageDirtyTracker { 
	private boolean isDirty;
	private boolean isDirty () { return isDirty; }
	private void makeDirty () { isDirty = true; }
	private void clearDirty () { isDirty = false; }
    }

    /**
     * A Graphics2D which sets a dirty flag whenever it is used for rendering.
     */
    // TODO: public for debugging: was private
    public class DirtyTrackingGraphics extends Graphics2D {

	/** The delegate Graphics2D */
	private Graphics2D delegate;

	private DirtyTrackingGraphics (Graphics2D delegate) {
	    this.delegate = delegate;
	}

	/** 
	 * Have any rendering methods of this Graphics2D been called since we last cleared the dirty flag?
	 */
	public synchronized boolean isDirty() {
	    return dirtyTracker.isDirty();
	}

	/**
	 * Set the dirty flag. Must be called from inside a synchronized (this) block.
	 */
      	private final void makeDirty () {
	    dirtyTracker.makeDirty();
	}

	/**
	 * Clear the dirty flag.
	 */
	private synchronized final void clearDirty () {
	    dirtyTracker.clearDirty();
	}

	public Color getColor() {
	    return delegate.getColor();
	}

	public void setColor( Color c ) {
	    delegate.setColor( c );
	}

	public void setPaintMode() {
	    delegate.setPaintMode();
	}

	public void setXORMode( Color c1 ) {
	    delegate.setXORMode( c1 );
	}

	public Font getFont() {
	    return delegate.getFont();
	}

	public void setFont( Font font ) {
	    delegate.setFont( font );
	}

	public FontMetrics getFontMetrics( Font f ) {
	    return delegate.getFontMetrics( f );
	}

	public Rectangle getClipBounds() {
	    return delegate.getClipBounds();
	}

	public void clipRect( int x, int y, int width, int height ) {
	    delegate.clipRect( x, y, width, height );
	}

	public void setClip( int x, int y, int width, int height ) {
	    delegate.setClip( x, y, width, height );
	}

	public Shape getClip() {
	    return delegate.getClip();
	}

	public void setClip( Shape clip ) {
	    delegate.setClip( clip );
	}

	public void dispose() {
	    delegate.dispose();
	}

	public boolean hit( Rectangle rect, Shape s, boolean onStroke ) {
	    return delegate.hit( rect, s, onStroke );
	}

	public GraphicsConfiguration getDeviceConfiguration() {
	    return delegate.getDeviceConfiguration();
	}

	public void setComposite( Composite comp ) {
	    delegate.setComposite( comp );
	}

	public void setPaint( Paint paint ) {
	    delegate.setPaint( paint );
	}

	public void setStroke( Stroke s ) {
	    delegate.setStroke( s );
	}

	public void setRenderingHint( RenderingHints.Key hintKey, Object hintValue ) {
	    delegate.setRenderingHint( hintKey, hintValue );
	}

	public Object getRenderingHint( RenderingHints.Key hintKey ) {
	    return delegate.getRenderingHint( hintKey );
	}

	public void setRenderingHints( Map<?,?> hints ) {
	    delegate.setRenderingHints( hints );
	}

	public void addRenderingHints( Map<?,?> hints ) {
	    delegate.addRenderingHints( hints );
	}

	public RenderingHints getRenderingHints() {
	    return delegate.getRenderingHints();
	}

	public void translate( int x, int y ) {
	    delegate.translate( x, y );
	}

	public void translate( double tx, double ty ) {
	    delegate.translate( tx, ty );
	}

	public void rotate( double theta ) {
	    delegate.rotate( theta );
	}

	public void rotate( double theta, double x, double y ) {
	    delegate.rotate( theta, x, y );
	}

	public void scale( double sx, double sy ) {
	    delegate.scale( sx, sy );
	}

	public void shear( double shx, double shy ) {
	    delegate.shear( shx, shy );
	}

	public void transform( AffineTransform Tx ) {
	    delegate.transform( Tx );
	}

	public void setTransform( AffineTransform Tx ) {
	    delegate.setTransform( Tx );
	}

	public AffineTransform getTransform() {
	    return delegate.getTransform();
	}

	public Paint getPaint() {
	    return delegate.getPaint();
	}

	public Composite getComposite() {
	    return delegate.getComposite();
	}

	public void setBackground( Color color ) {
	    delegate.setBackground( color );
	}

	public Color getBackground() {
	    return delegate.getBackground();
	}

	public Stroke getStroke() {
	    return delegate.getStroke();
	}

	public void clip( Shape s ) {
	    delegate.clip( s );
	}

	public FontRenderContext getFontRenderContext() {
	    return delegate.getFontRenderContext();
	}

	public Graphics create () {
	    return new DirtyTrackingGraphics((Graphics2D)delegate.create());
	}

	public Graphics create(int x, int y, int width, int height) {
	    return new DirtyTrackingGraphics((Graphics2D)delegate.create(x, y, width, height));
        }

	public synchronized void copyArea( int x, int y, int width, int height, int dx, int dy ) {
	    makeDirty();
	    delegate.copyArea( x, y, width, height, dx, dy );
	}

	public synchronized void drawLine( int x1, int y1, int x2, int y2 ) {
	    makeDirty();
	    delegate.drawLine( x1, y1, x2, y2 );
	}

	public synchronized void fillRect( int x, int y, int width, int height ) {
	    makeDirty();
	    delegate.fillRect( x, y, width, height );
	}

	public synchronized void clearRect( int x, int y, int width, int height ) {
	    makeDirty();
	    delegate.clearRect( x, y, width, height );
	}

	public synchronized void drawRoundRect( int x, int y, int width, int height, int arcWidth, 
						int arcHeight ) {
	    makeDirty();
	    delegate.drawRoundRect( x, y, width, height, arcWidth, arcHeight );
	}

	public synchronized void fillRoundRect( int x, int y, int width, int height, int arcWidth, 
						int arcHeight ) {
	    makeDirty();
	    delegate.fillRoundRect( x, y, width, height, arcWidth, arcHeight );
	}

	public synchronized void drawOval( int x, int y, int width, int height ) {
	    makeDirty();
	    delegate.drawOval( x, y, width, height );
	}

	public synchronized void fillOval( int x, int y, int width, int height ) {
	    makeDirty();
	    delegate.fillOval( x, y, width, height );
	}

	public synchronized void drawArc( int x, int y, int width, int height, int startAngle, 
					  int arcAngle ) {
	    makeDirty();
	    delegate.drawArc( x, y, width, height, startAngle, arcAngle );
	}

	public synchronized void fillArc( int x, int y, int width, int height, int startAngle, 
					  int arcAngle ) {
	    makeDirty();
	    delegate.fillArc( x, y, width, height, startAngle, arcAngle );
	}

	public synchronized void drawPolyline( int[] xPoints, int[] yPoints, int nPoints ) {
	    makeDirty();
	    delegate.drawPolyline( xPoints, yPoints, nPoints );
	}

	public synchronized void drawPolygon( int[] xPoints, int[] yPoints, int nPoints ) {
	    makeDirty();
	    delegate.drawPolygon( xPoints, yPoints, nPoints );
	}

	public synchronized void fillPolygon( int[] xPoints, int[] yPoints, int nPoints ) {
	    makeDirty();
            delegate.fillPolygon( xPoints, yPoints, nPoints );
	}

	public synchronized boolean drawImage( java.awt.Image img, int x, int y, ImageObserver observer ) {
	    makeDirty();
	    return delegate.drawImage( img, x, y, observer );
	}

	public synchronized boolean drawImage( java.awt.Image img, int x, int y, int width, int height, 
					       ImageObserver observer ) {
	    makeDirty();
	    return delegate.drawImage( img, x, y, width, height, observer );
	}

	public synchronized boolean drawImage( java.awt.Image img, int x, int y, Color bgcolor, 
					       ImageObserver observer ) {
	    makeDirty();
	    return delegate.drawImage( img, x, y, bgcolor, observer );
	}

	public synchronized boolean drawImage( java.awt.Image img, int x, int y, int width, int height, 
					       Color bgcolor, ImageObserver observer ) {
	    makeDirty();
	    return delegate.drawImage( img, x, y, width, height, bgcolor, observer );
	}

	public synchronized boolean drawImage( java.awt.Image img, int dx1, int dy1, int dx2, int dy2, 
					       int sx1, int sy1, int sx2, int sy2, ImageObserver observer ) {
	    makeDirty();
	    return delegate.drawImage( img, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, observer );
	}

	public synchronized boolean drawImage( java.awt.Image img, int dx1, int dy1, int dx2, int dy2, 
					       int sx1, int sy1, int sx2, int sy2, Color bgcolor, 
					       ImageObserver observer ) {
	    makeDirty();
	    return delegate.drawImage( img, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, bgcolor, observer );
	}

	public synchronized void draw( Shape s ) {
	    makeDirty();
	    delegate.draw( s );
	}

	public synchronized boolean drawImage( java.awt.Image img, AffineTransform xform, 
					       ImageObserver obs ) {
	    makeDirty();
	    return delegate.drawImage( img, xform, obs );
	}

	public synchronized void drawImage( BufferedImage img, BufferedImageOp op, int x, int y ) {
	    makeDirty();
	    delegate.drawImage( img, op, x, y );
	}

	public synchronized void drawRenderedImage( RenderedImage img, AffineTransform xform ) {
	    makeDirty();
	    delegate.drawRenderedImage( img, xform );
	}

	public synchronized void drawRenderableImage( RenderableImage img, AffineTransform xform ) {
	    makeDirty();
	    delegate.drawRenderableImage( img, xform );
	}

	public synchronized void drawString( String str, int x, int y ) {
	    makeDirty();
	    delegate.drawString( str, x, y );
	}

	public synchronized void drawString( String s, float x, float y ) {
	    makeDirty();
	    delegate.drawString( s, x, y );
	}

	public synchronized void drawString( AttributedCharacterIterator iterator, int x, int y ) {
	    makeDirty();
	    delegate.drawString( iterator, x, y );
	}

	public synchronized void drawString( AttributedCharacterIterator iterator, float x, float y ) {
	    makeDirty();
	    delegate.drawString( iterator, x, y );
	}

	public synchronized void drawGlyphVector( GlyphVector g, float x, float y ) {
	    makeDirty();
	    delegate.drawGlyphVector( g, x, y );
	}

	public synchronized void fill( Shape s ) {
	    makeDirty(); 
	    delegate.fill( s );
	}
    }
}
