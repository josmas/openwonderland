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
package org.jdesktop.wonderland.modules.hud.client;

import java.awt.Rectangle;
import java.util.logging.Logger;
import org.jdesktop.wonderland.client.hud.HUD;
import org.jdesktop.wonderland.client.hud.HUDFactorySPI;

/**
 * The HUDFactory creates new WonderlandHUD object instances. A visual Wonderland
 * client will typically have one HUDFactory which is the source of all WonderlandHUD
 * instances.
 *
 * @author nsimpson
 */
public class WonderlandHUDFactory implements HUDFactorySPI {

    private static final Logger logger = Logger.getLogger(WonderlandHUDFactory.class.getName());

    /**
     * {@inheritDoc}
     */
    public HUD createHUD() {
        return new WonderlandHUD();
    }

    /**
     * {@inheritDoc}
     */
    public HUD createHUD(int x, int y, int width, int height) {
        return new WonderlandHUD(x, y, width, height);
    }

    /**
     * {@inheritDoc}
     */
    public HUD createHUD(Rectangle bounds) {
        return new WonderlandHUD(bounds);
    }
}
