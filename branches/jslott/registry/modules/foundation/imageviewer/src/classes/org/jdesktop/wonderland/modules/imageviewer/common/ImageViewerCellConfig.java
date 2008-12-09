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
package org.jdesktop.wonderland.modules.imageviewer.common;

import com.jme.math.Vector2f;
import org.jdesktop.wonderland.common.ExperimentalAPI;
import org.jdesktop.wonderland.modules.appbase.common.App2DCellConfig;

/**
 * Container for image viewer cell data
 *
 * @author nsimpson,deronj
 */

@ExperimentalAPI
public class ImageViewerCellConfig extends App2DCellConfig {
    
    private static final int DEFAULT_WIDTH = 640;
    private static final int DEFAULT_HEIGHT = 480;
    private static final int GROWTH_DELTA = 256;
    
    private int preferredWidth = DEFAULT_WIDTH;
    private int preferredHeight = DEFAULT_HEIGHT;
    private String imageURI = null;
    
    public ImageViewerCellConfig () {
        this(null);
    }
    
    public ImageViewerCellConfig (Vector2f pixelScale) {
	super(pixelScale);
    }

    public String getImageURI() {
        return imageURI;
    }

    public void setImageURI(String imageURI) {
        this.imageURI = imageURI;
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
