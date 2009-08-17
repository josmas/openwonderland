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

import java.util.logging.Logger;
import org.jdesktop.wonderland.modules.appbase.client.App2D;
import org.jdesktop.wonderland.modules.appbase.client.swing.WindowSwing;
import com.jme.math.Vector2f;
import org.jdesktop.wonderland.client.jme.JmeClientMain;
import org.jdesktop.wonderland.common.ExperimentalAPI;

/**
 *
 * The window for the Swing menu test.
 *
 * @author deronj
 */

@ExperimentalAPI
public class SwingMenuTestWindow extends WindowSwing  {

    /** The logger used by this class. */
    private static final Logger logger = Logger.getLogger(SwingMenuTestWindow.class.getName());

    /**
     * Create a new instance of SwingMenuTestWindow.
     *
     * @param app The app which owns the window.
     * @param width The width of the window (in pixels).
     * @param height The height of the window (in pixels).
     * @param decorated Whether the window is decorated with a frame.
     * @param pixelScale The size of the window pixels.
     */
    public SwingMenuTestWindow (App2D app, int width, int height, boolean decorated, Vector2f pixelScale)
        throws InstantiationException
    {
	super(app, width, height, decorated, pixelScale);

	setTitle("Swing Menu Test");
	
	MenuPanel menuPanel = new MenuPanel();

	// Parent to Wonderland main window for proper focus handling 
       	JmeClientMain.getFrame().getCanvas3DPanel().add(menuPanel);

	setComponent(menuPanel);
    }
}
