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
package org.jdesktop.wonderland.modules.imageviewer.client;

import java.util.logging.Logger;
import org.jdesktop.wonderland.modules.appbase.client.App;
import org.jdesktop.wonderland.modules.appbase.client.WindowGraphics2D;
import com.jme.math.Vector2f;
import org.jdesktop.wonderland.common.ExperimentalAPI;

/**
 *
 * The window for the whiteboard.
 *
 * @author deronj
 */

@ExperimentalAPI
public class ImageViewerWindow extends WindowGraphics2D  {

    /** The logger used by this class */
    private static final Logger logger = Logger.getLogger(ImageViewerWindow.class.getName());

    /** The image which is drawn on */
    private ImageViewerDrawingSurface wbSurface;

    /**
     * Create a new instance of ImageViewerWindow.
     *
     * @param app The whiteboard app which owns the window.
     * @param width The width of the window (in pixels).
     * @param height The height of the window (in pixels).
     * @param topLevel Whether the window is top-level (e.g. is decorated) with a frame.
     * @param pixelScale The size of the window pixels.
     * @param commComponent The communications component for communicating with the server.
     */
    public ImageViewerWindow (final App app, int width, int height, boolean topLevel, Vector2f pixelScale)
        throws InstantiationException
    {
	super(app, width, height, topLevel, pixelScale, new ImageViewerDrawingSurface(width, height));
	initializeSurface();

	// For debug
	setTitle("IMAGE VIEWER WINDOW");
	
	wbSurface = (ImageViewerDrawingSurface)getSurface();
    }
}
