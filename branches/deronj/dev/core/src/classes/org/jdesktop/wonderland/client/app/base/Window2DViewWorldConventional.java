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
package org.jdesktop.wonderland.client.app.base;

import java.nio.ByteBuffer;
import org.jdesktop.wonderland.common.ExperimentalAPI;

/**
 * An interface used by conventional view world objects provided by the gui factory.
 *
 * @author deronj
 */

public interface Window2DViewWorldConventional {

    /**
     * Insert the given pixels into the window's image into a subrectangle starting at (x, y) 
     * (in borderless coordinates) and having dimensions w x h.
     * 
     * @param x The X coordinate of the top-lel corner of the image subrectangle which is to be changed.
     * @param y The Y coordinate of the top left corner of the image subrectangle which is to be changed.
     * @param w The width of the image subrectangle which is to be changed.
     * @param h The height of the image subrectangle which is to be changed.
     * @param pixels An array which contains the pixels. It must be of length w x h.
     */
    public void displayPixels (int x, int y, int w, int h, ByteBuffer pixels);
}
