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
package org.jdesktop.wonderland.modules.swingexample.common.cell;

import java.util.logging.Logger;
import com.jme.math.Vector2f;
import org.jdesktop.wonderland.common.ExperimentalAPI;
import org.jdesktop.wonderland.modules.appbase.common.cell.App2DCellClientState;

/**
 * Container for Swing example client cell client state data.
 *
 * @author deronj
 */

@ExperimentalAPI
public class SwingExampleCellClientState extends App2DCellClientState {

    private static final Logger logger = Logger.getLogger(SwingExampleCellClientState.class.getName());
    
    private static final int DEFAULT_WIDTH = 640;
    private static final int DEFAULT_HEIGHT = 480;
    
    private int preferredWidth = DEFAULT_WIDTH;
    private int preferredHeight = DEFAULT_HEIGHT;
    
    public SwingExampleCellClientState () {
        this(null);
    }
    
    public SwingExampleCellClientState (Vector2f pixelScale) {
	super(pixelScale);
    }
    
    /*
     * Set the preferred width of the Swing example
     * @param preferredWidth the preferred width in pixels
     */
    public void setPreferredWidth(int preferredWidth) {
        this.preferredWidth = preferredWidth;
    }
    
    /*
     * Get the preferred width of the Swing example
     * @return the preferred width, in pixels
     */
    public int getPreferredWidth() {
        return preferredWidth;
    }
    
    /*
     * Set the preferred height of the Swing example
     * @param preferredHeight the preferred height, in pixels
     */
    public void setPreferredHeight(int preferredHeight) {
        this.preferredHeight = preferredHeight;
    }
    
    /*
     * Get the preferred height of the Swing example
     * @return the preferred height, in pixels
     */
    public int getPreferredHeight() {
        return preferredHeight;
    }
}
