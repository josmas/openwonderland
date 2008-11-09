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
package org.jdesktop.wonderland.modules.swingsettest.client;

import org.jdesktop.wonderland.modules.appbase.client.AppType;
import org.jdesktop.wonderland.modules.appbase.client.AppGraphics2D;
import org.jdesktop.wonderland.modules.appbase.client.ControlArbMulti;
import com.jme.math.Vector2f;
import org.jdesktop.wonderland.common.ExperimentalAPI;
import org.jdesktop.wonderland.modules.appbase.client.AppCell;

/**
 *
 * A Swing test application which is based on the JFC demo SwingSet2.
 *
 * @author paulby,deronj
 */

@ExperimentalAPI
public class SwingSetTestApp extends AppGraphics2D  {
    
    /** The single window created by this app */
    private SwingSetTestWindow window;

    /**
     * Create a new instance of SwingSetTestApp. This in turn creates
     * and makes visible the single window used by the app.
     *
     * @param appType The type of app (should be SwingSetTestAppType).
     * @param width The width (in pixels) of the window.
     * @param height The height (in pixels) of the window.
     * @param pixelScale The horizontal and vertical pixel sizes (in world meters per pixel).
     */
    public SwingSetTestApp (AppType appType, int width, int height, Vector2f pixelScale) {

	// configWorld can be null because the server cell is already configured
	super(appType, new ControlArbMulti(), pixelScale);
	controlArb.setApp(this);

	// This app has only one window, so it is always top-level 
        try {
            window = new SwingSetTestWindow(this, width, height, /*TODO: until debugged: true*/false, pixelScale);
        } catch (InstantiationException ex) {
            throw new RuntimeException(ex);
        }
    }

    /** 
     * Clean up resources.
     */
    public void cleanup () {
	super.cleanup();
	if (window != null) {
	    window.setVisible(false);
	    window.cleanup();
	    window = null;
	}
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setCell (AppCell cell) 
	throws IllegalArgumentException, IllegalStateException 
    {
	super.setCell(cell);

	// TODO: temp for popups
	// TODO: NOTYET: WindowSwingPopup.setCell(this);
    }

    /**
     * Returns the app's window.
     */
    public SwingSetTestWindow getWindow () {
	return window;
    }

    /**
     * Change the visibility of the app.
     *
     * @param visible Whether the application is visible.
     */
    public void setVisible (boolean visible) {
	window.setVisible(visible);
    }
}
