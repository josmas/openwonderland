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
package org.jdesktop.wonderland.modules.swingtest.client;

import java.util.logging.Logger;
import org.jdesktop.wonderland.modules.appbase.client.App2D;
import org.jdesktop.wonderland.modules.appbase.client.swing.WindowSwing;
import com.jme.math.Vector2f;
import com.jme.scene.Node;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.mtgame.RenderComponent;
import org.jdesktop.wonderland.client.jme.JmeClientMain;
import org.jdesktop.wonderland.client.jme.utils.graphics.GraphicsUtils;
import org.jdesktop.wonderland.common.ExperimentalAPI;
import org.jdesktop.wonderland.modules.appbase.client.view.View2DEntity;
import org.jdesktop.wonderland.modules.swingtest.client.cell.SwingTestCell;
import com.jme.math.Vector3f;

/**
 *
 * The window for the Swing test.
 *
 * @author deronj
 */
@ExperimentalAPI
public class SwingTestWindow 
    extends WindowSwing  
    implements TestPanel.Container 
{
    /** The logger used by this class. */
    private static final Logger logger = Logger.getLogger(SwingTestWindow.class.getName());

    /** The cell in which this window is displayed. */
    private SwingTestCell cell;

    /** Whether the window is currently displayed "on the glass". */
    private boolean ortho;

    /**
     * Create a new instance of SwingTestWindow.
     *
     * @param cell The cell in which this window is displayed.
     * @param app The app which owns the window.
     * @param width The width of the window (in pixels).
     * @param height The height of the window (in pixels).
     * @param decorated Whether the window is decorated with a frame.
     * @param pixelScale The size of the window pixels.
     */
    public SwingTestWindow (SwingTestCell cell, App2D app, int width, int height, boolean decorated, 
                            Vector2f pixelScale)
        throws InstantiationException
    {
	super(app, width, height, decorated, pixelScale);
        this.cell = cell;

	setTitle("Swing Test");

	TestPanel testPanel = new TestPanel();

	// Parent to Wonderland main window for proper focus handling 
       	JmeClientMain.getFrame().getCanvas3DPanel().add(testPanel);

        testPanel.setContainer(this);

	setComponent(testPanel);
        setTitle("Swing Test");

        /* Test Force a the preferred size
        System.err.println("test panel size = " + width + ", " + height);
        setSize(width, height);
        */
    }

    public void setOrtho(boolean ortho) {
        if (this.ortho == ortho) return;

        View2DEntity view = (View2DEntity) getView(cell);

        if (ortho) {
            
            // In this test, the view in the ortho plane is at a fixed location.
            view.setLocationOrtho(new Vector2f(300f, 300f), false);
            
            // Test
            //view.setPixelScaleOrtho(2.0f, 2.0f);
            //view.setPixelScaleOrtho(0.5f, 0.5f);

            // Move the window view into the ortho plane
            view.setOrtho(true, false);

        } else {

            // Move the window view into the cell
            view.setOrtho(false, false);
        }

        // Now make it all happen
        view.update();

        this.ortho = ortho;
    }
}
