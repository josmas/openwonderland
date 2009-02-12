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
package org.jdesktop.wonderland.modules.swingmenutest.client;

import org.jdesktop.wonderland.modules.appbase.client.AppType;
import org.jdesktop.wonderland.modules.appbase.client.AppGraphics2D;
import org.jdesktop.wonderland.modules.appbase.client.ControlArbMulti;
import com.jme.math.Vector2f;
import org.jdesktop.wonderland.common.ExperimentalAPI;
import org.jdesktop.wonderland.modules.appbase.client.cell.AppCell;

/**
 *
 * A Swing test application.
 *
 * @author paulby,deronj
 */

@ExperimentalAPI
public class SwingMenuTestApp extends AppGraphics2D  {
    
    /**
     * Create a new instance of SwingMenuTestApp. This in turn creates
     * and makes visible the single window used by the app.
     *
     * @param appType The type of app (should be SwingMenuTestAppType).
     * @param pixelScale The horizontal and vertical pixel sizes (in world meters per pixel).
     */
    public SwingMenuTestApp (AppType appType, Vector2f pixelScale) {

	// configWorld can be null because the server cell is already configured
	super(appType, new ControlArbMulti(), pixelScale);
	controlArb.setApp(this);
    }

    /** 
     * Clean up resources.
     */
    public void cleanup () {
	super.cleanup();
    }
}
