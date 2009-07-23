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

import java.awt.Canvas;
import java.util.logging.Logger;
import org.jdesktop.wonderland.client.BaseClientPlugin;
import org.jdesktop.wonderland.client.hud.HUD;
import org.jdesktop.wonderland.client.hud.HUDComponentManager;
import org.jdesktop.wonderland.client.hud.HUDFactory;
import org.jdesktop.wonderland.client.hud.HUDManager;
import org.jdesktop.wonderland.client.hud.HUDManagerFactory;
import org.jdesktop.wonderland.client.jme.JmeClientMain;
import org.jdesktop.wonderland.client.login.ServerSessionManager;
import org.jdesktop.wonderland.common.annotation.Plugin;

/**
 * Plugin for initializing the HUD
 * @author nsimpson
 */
@Plugin
public class HUDClientPlugin extends BaseClientPlugin {

    private static final Logger logger = Logger.getLogger(HUDClientPlugin.class.getName());
    private static final float DEFAULT_HUD_X = 0.0f;
    private static final float DEFAULT_HUD_Y = 0.0f;
    private static final float DEFAULT_HUD_WIDTH = 1.0f;
    private static final float DEFAULT_HUD_HEIGHT = 1.0f;

    @Override
    public void initialize(ServerSessionManager loginManager) {
        logger.fine("initializing HUD client plugin");

        // Create the default HUD factory
        HUDFactory.setHUDFactorySPI(new WonderlandHUDFactory());

        // create the main Wonderland HUD
        Canvas canvas = JmeClientMain.getFrame().getCanvas();
        logger.fine("creating Wonderland HUD: " +
                DEFAULT_HUD_WIDTH + "x" + DEFAULT_HUD_HEIGHT +
                " at " + DEFAULT_HUD_X + ", " + DEFAULT_HUD_Y);
        final HUD wonderlandHUD = HUDFactory.createHUD(canvas.getSize(),
                DEFAULT_HUD_X, DEFAULT_HUD_Y, DEFAULT_HUD_WIDTH, DEFAULT_HUD_HEIGHT);
        wonderlandHUD.setName("main");

        // create the default HUD Manager factory
        HUDManagerFactory.setHUDManagerFactorySPI(new WonderlandHUDManagerFactory());

        // create a HUD manager instance to manage all the HUDs
        HUDManager manager = HUDManagerFactory.createHUDManager(canvas);

        // define how HUDs are laid out on the screen
        manager.setLayoutManager(new HUDAbsoluteLayoutManager(wonderlandHUD));

        // manage the main HUD
        manager.addHUD(wonderlandHUD);

        // create a component manager for the HUD components in this HUD
        HUDComponentManager compManager = new WonderlandHUDComponentManager();

        // define the layout of HUD components in the Wonderland main HUD
        compManager.setLayoutManager(new HUDCompassLayoutManager(wonderlandHUD));

        // manage the components in the main HUD
        wonderlandHUD.setComponentManager(compManager);

        // call the superclass's initialize method
        super.initialize(loginManager);
    }
}
