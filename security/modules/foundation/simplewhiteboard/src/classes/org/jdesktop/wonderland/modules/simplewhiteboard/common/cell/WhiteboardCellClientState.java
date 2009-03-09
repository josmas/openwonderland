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
package org.jdesktop.wonderland.modules.simplewhiteboard.common.cell;

import java.lang.reflect.Array;
import java.util.logging.Logger;
import com.jme.math.Vector2f;
import org.jdesktop.wonderland.common.ExperimentalAPI;
import org.jdesktop.wonderland.modules.appbase.common.cell.App2DCellClientState;

/**
 * Container for whiteboard cell data
 *
 * @author nsimpson,deronj
 */

@ExperimentalAPI
public class WhiteboardCellClientState extends App2DCellClientState {

    private static final Logger logger = Logger.getLogger(WhiteboardCellClientState.class.getName());
    
    private static final int DEFAULT_WIDTH = 640;
    private static final int DEFAULT_HEIGHT = 480;
    private static final int GROWTH_DELTA = 256;
    
    private int dimensions[] = {0, 4};
    private int actions[][] = (int[][])Array.newInstance(int.class, dimensions);
    private int index = 0;
    private int preferredWidth = DEFAULT_WIDTH;
    private int preferredHeight = DEFAULT_HEIGHT;
    private String checksum;
    
    public WhiteboardCellClientState () {
        this(null);
    }
    
    public WhiteboardCellClientState (Vector2f pixelScale) {
	super(pixelScale);
    }
    
    /*
     * Get the checksum for the whiteboard
     * @return the checksum of the whiteboard
     */
    public String getChecksum() {
        return checksum;
    }
    
    /*
     * Set the checksum of the whiteboard
     * @param checksum the checksum of the whiteboard
     */
    public void setChecksum(String checksum) {
        this.checksum = checksum;
    }
    
    /*
     * Set the preferred width of the whiteboard
     * @param preferredWidth the preferred width in pixels
     */
    public void setPreferredWidth(int preferredWidth) {
        this.preferredWidth = preferredWidth;
    }
    
    /*
     * Get the preferred width of the whiteboard
     * @return the preferred width, in pixels
     */
    public int getPreferredWidth() {
        return preferredWidth;
    }
    
    /*
     * Set the preferred height of the whiteboard
     * @param preferredHeight the preferred height, in pixels
     */
    public void setPreferredHeight(int preferredHeight) {
        this.preferredHeight = preferredHeight;
    }
    
    /*
     * Get the preferred height of the whiteboard
     * @return the preferred height, in pixels
     */
    public int getPreferredHeight() {
        return preferredHeight;
    }
}
