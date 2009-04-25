/*
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
package org.jdesktop.wonderland.client.hud;

import java.awt.Rectangle;

/**
 * A service provider interface for factories which create new HUD object
 * instances.
 * 
 * A visual Wonderland client will typically have one HUDFactory which is
 * the source of all HUD instances.
 *
 * @author nsimpson
 */
public interface HUDFactorySPI {

    /**
     * Creates a new HUD instance
     * @return a new HUD instance with default location and size
     */
    public HUD createHUD();

    /**
     * Creates a new HUD instance
     * @param x the x-coordinate of the HUD
     * @param y the y-coordinate of the HUD
     * @param width the width of the HUD
     * @param height the height of the HUD
     * @return a new HUS instance with default location and size
     */
    public HUD createHUD(int x, int y, int width, int height);

    /**
     * Creates a new HUD instance
     * @param bounds the location and size of the HUD
     * @return a new HUD instance with the specified location and size
     */
    public HUD createHUD(Rectangle bounds);
}
