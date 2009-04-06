/**
 * Project Wonderland
 *
 * Copyright (c) 2004-2009, Sun Microsystems, Inc., All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * Sun designates this particular file as subject to the "Classpath" 
 * exception as provided by Sun in the License file that accompanied 
 * this code.
 */
package org.jdesktop.wonderland.modules.appbase.client;

import com.jme.math.Vector2f;
import com.jmex.awt.swingui.ImageGraphics;
import java.awt.Graphics2D;
import java.util.logging.Logger;
import org.jdesktop.wonderland.common.InternalAPI;

/**
 * A window that provides a drawing surface (a buffered image) that can be drawn on by a Graphics2D. 
 *
 * @author deronj
 */
@InternalAPI
public class WindowGraphics2D extends Window2D {

    private static final Logger logger = Logger.getLogger(WindowGraphics2D.class.getName());
    /** The surface the client on which subclasses should draw */
    protected DrawingSurface surface;

    /**
     * Create an instance of WindowGraphics2D with a default name. The first such window created for an app 
     * becomes the primary window. Subsequent windows are secondary windows.
     * @param app The application to which this window belongs.
     * @param width The window width (in pixels).
     * @param height The window height (in pixels).
     * @param decorated Whether the window is decorated with a frame.
     * @param pixelScale The size of the window pixels.
     * @param surface The drawing surface on which the creator will draw
     */
    public WindowGraphics2D(App2D app, int width, int height, boolean decorated,
                            Vector2f pixelScale, DrawingSurface surface) {
        this(app, width, height, decorated, pixelScale, null, surface);
    }

    /**
     * Create an instance of WindowGraphics2D with the given. The first such window created for an app 
     * becomes the primary window. Subsequent windows are secondary windows.
     * @param app The application to which this window belongs.
     * @param width The window width (in pixels).
     * @param height The window height (in pixels).
     * @param decorated Whether the window is decorated with a frame.
     * @param pixelScale The size of the window pixels.
     * @param name The name of the window.
     * @param surface The drawing surface on which the creator will draw
     */
    public WindowGraphics2D(App2D app, int width, int height, boolean decorated,
                            Vector2f pixelScale, String name, DrawingSurface surface) {
        super(app, width, height, decorated, pixelScale, name);
        this.surface = surface;
        initialize();
    }

    /**
     * Create an instance of WindowGraphics2D of the given type with a default name. 
     * @param app The application to which this window belongs.
     * @param type The type of the window. If this is non-primary, the parent is set to the primary
     * window of the app (if there is one).
     * @param width The window width (in pixels).
     * @param height The window height (in pixels).
     * @param decorated Whether the window is decorated with a frame.
     * @param pixelScale The size of the window pixels.
     */
    protected WindowGraphics2D(App2D app, Type type, int width, int height, boolean decorated, 
                               Vector2f pixelScale, DrawingSurface surface) {
        this(app, type, width, height, decorated, pixelScale, null, surface);
    }

    /**
     * Create an instance of WindowGraphics2D of the given type with the given name. 
     * @param app The application to which this window belongs.
     * @param type The type of the window. If this is non-primary, the parent is set to the primary
     * window of the app (if there is one).
     * @param width The window width (in pixels).
     * @param height The window height (in pixels).
     * @param decorated Whether the window is top-level (e.g. is decorated) with a frame.
     * @param pixelScale The size of the window pixels.
     * @param name The name of the window.
     */
    public WindowGraphics2D(App2D app, Type type, int width, int height, boolean decorated, 
                            Vector2f pixelScale, String name, DrawingSurface surface) {
        this(app, type, app.getPrimaryWindow(), width, height, decorated, pixelScale, name, surface);
    }

    /**
     * Create an instance of WindowGraphics2D of the given type with the given parent with a default name. 
     * @param app The application to which this window belongs.
     * @param type The type of the window. 
     * @param parent The parent of the window. (Ignored for primary windows).
     * @param width The window width (in pixels).
     * @param height The window height (in pixels).
     * @param decorated Whether the window is decorated with a frame.
     * @param pixelScale The size of the window pixels.
     */
    protected WindowGraphics2D(App2D app, Type type, WindowGraphics2D parent, int width, int height, 
                               boolean decorated, Vector2f pixelScale, DrawingSurface surface) {
        this(app, type, parent, width, height, decorated, pixelScale, null, surface);
    }

    /**
     * Create an instance of WindowGraphics2D of the given type with the given parent with the given name. 
     * @param app The application to which this window belongs.
     * @param type The type of the window. If this is non-primary, the parent is set to the primary
     * window of the app (if there is one).
     * @param parent The parent of the window. (Ignored for primary windows).
     * @param width The window width (in pixels).
     * @param height The window height (in pixels).
     * @param decorated Whether the window is top-level (e.g. is decorated) with a frame.
     * @param pixelScale The size of the window pixels.
     * @param name The name of the window.
     */
    public WindowGraphics2D(App2D app, Type type, Window2D parent, int width, int height, boolean decorated, 
                            Vector2f pixelScale, String name, DrawingSurface surface) {
        super(app, type, parent, width, height, decorated, pixelScale, name);
        this.surface = surface;
        initialize();
    }

    private void initialize () {
        // Arrange for the surface contents to be continuously copied into this window's texture.
        updateTexture();
        surface.setWindow(this);
        surface.setTexture(texture);
        surface.setUpdateEnable(true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void cleanup() {
        if (surface != null) {
            surface.setUpdateEnable(false);
            surface.cleanup();
            surface = null;
        }
        super.cleanup();
    }

    /**
     * {@inheritDoc}
     * Note: the arguments do NOT include the borderWidth.
     */
    @Override
    public void setSize(int width, int height) {
        super.setSize(width, height);
        if (surface != null) {
            surface.setTexture(texture);
            surface.setSize(width, height);
        }
    }

    /**
     * Initialize the contents of the surface.
     */
    protected void initializeSurface() {
        if (surface != null) {
            surface.initializeSurface();
        }
    }

    /**
     * Returns the drawing surface of this windows. 
     */
    public DrawingSurface getSurface() {
        return surface;
    }

    protected void paint(Graphics2D g) {
    }

    protected void repaint() {
    }
}
