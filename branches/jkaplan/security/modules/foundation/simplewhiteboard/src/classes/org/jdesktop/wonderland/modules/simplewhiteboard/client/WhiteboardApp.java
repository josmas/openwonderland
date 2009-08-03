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
package org.jdesktop.wonderland.modules.simplewhiteboard.client;

import org.jdesktop.wonderland.modules.appbase.client.AppGraphics2D;
import org.jdesktop.wonderland.modules.appbase.client.ControlArbAppFocus;
import com.jme.math.Vector2f;
import org.jdesktop.wonderland.common.ExperimentalAPI;

/**
 *
 * A 2D whiteboard application
 *
 * @author paulby,deronj
 */

@ExperimentalAPI
public class WhiteboardApp extends AppGraphics2D  {
    
    /**
     * Create a new instance of WhiteboardApp. This in turn creates
     * and makes visible the single window used by the app.
     *
     * @param name The name of the app.
     * @param pixelScale The horizontal and vertical pixel sizes
     * (in world meters per pixel).
     * @param commComponent The communications component for communicating with the server.
     */
    public WhiteboardApp (String name, Vector2f pixelScale) {
	super(name, new ControlArbAppFocus(), pixelScale);
	controlArb.setApp(this);
    }

    /** 
     * Clean up resources.
     */
    public void cleanup () {
	super.cleanup();
    }
}