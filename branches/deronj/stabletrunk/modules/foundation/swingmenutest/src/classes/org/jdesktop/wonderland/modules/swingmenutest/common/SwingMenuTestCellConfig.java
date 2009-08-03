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
package org.jdesktop.wonderland.modules.swingmenutest.common;

import java.awt.Point;
import java.lang.reflect.Array;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.logging.Logger;
import com.jme.math.Vector2f;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.ExperimentalAPI;
import org.jdesktop.wonderland.modules.appbase.common.App2DCellConfig;

/**
 * Container for Swing test client cell configuration data.
 *
 * @author deronj
 */

@ExperimentalAPI
public class SwingMenuTestCellConfig extends App2DCellConfig {

    private static final Logger logger = Logger.getLogger(SwingMenuTestCellConfig.class.getName());
    
    private static final int DEFAULT_WIDTH = 640;
    private static final int DEFAULT_HEIGHT = 480;
    
    private int preferredWidth = DEFAULT_WIDTH;
    private int preferredHeight = DEFAULT_HEIGHT;
    
    public SwingMenuTestCellConfig () {
        this(null);
    }
    
    public SwingMenuTestCellConfig (Vector2f pixelScale) {
	super(pixelScale);
    }
    
    /*
     * Set the preferred width of the Swing test
     * @param preferredWidth the preferred width in pixels
     */
    public void setPreferredWidth(int preferredWidth) {
        this.preferredWidth = preferredWidth;
    }
    
    /*
     * Get the preferred width of the Swing test
     * @return the preferred width, in pixels
     */
    public int getPreferredWidth() {
        return preferredWidth;
    }
    
    /*
     * Set the preferred height of the Swing test
     * @param preferredHeight the preferred height, in pixels
     */
    public void setPreferredHeight(int preferredHeight) {
        this.preferredHeight = preferredHeight;
    }
    
    /*
     * Get the preferred height of the Swing test
     * @return the preferred height, in pixels
     */
    public int getPreferredHeight() {
        return preferredHeight;
    }
}