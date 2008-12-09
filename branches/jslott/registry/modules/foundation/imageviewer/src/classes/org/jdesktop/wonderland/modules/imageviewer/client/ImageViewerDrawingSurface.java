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
import org.jdesktop.wonderland.modules.appbase.client.DrawingSurfaceBufferedImage;
import org.jdesktop.wonderland.common.ExperimentalAPI;


/**
 * @author paulby,deronj
 */

@ExperimentalAPI
public class ImageViewerDrawingSurface extends DrawingSurfaceBufferedImage {

    private static final Logger logger = Logger.getLogger(ImageViewerDrawingSurface.class.getName());
    
    public ImageViewerDrawingSurface (int width, int height) {
	this();
	setSize(width, height);
    }

    public ImageViewerDrawingSurface() {
        super();
    }
    
    @Override
    public void setSize(int width, int height) {
        super.setSize(width, height);
    }
    
    
}
