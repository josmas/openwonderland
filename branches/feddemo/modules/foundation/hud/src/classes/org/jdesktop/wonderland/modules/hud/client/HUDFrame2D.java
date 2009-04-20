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

import com.jme.math.Vector2f;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.wonderland.client.hud.HUD;
import org.jdesktop.wonderland.modules.appbase.client.App2D;
import org.jdesktop.wonderland.modules.appbase.client.swing.WindowSwing;

/**
 * A 2D frame for a HUDComponent2D
 *
 * @author nsimpson
 */
public class HUDFrame2D extends HUDComponent2D {

    private static final Logger logger = Logger.getLogger(HUDFrame2D.class.getName());
    private HUDFrame2DImpl frame2DImpl;
    private WindowSwing window;
    private App2D app;
    private HUDComponent2D component;

    public HUDFrame2D(App2D app, HUDComponent2D component) {
        super();
        this.app = app;
        this.component = component;
        initializeFrame();
    }

    /**
     * Create the frame components
     */
    private void initializeFrame() {
        if (frame2DImpl == null) {
            frame2DImpl = new HUDFrame2DImpl();

            try {
                logger.log(Level.FINE, "creating WindowSwing: " + frame2DImpl.getWidth() + "x" + frame2DImpl.getHeight());
                window = new WindowSwing(app, frame2DImpl.getWidth(), frame2DImpl.getHeight(),
                        false, new Vector2f(0.02f, 0.02f));
                window.setComponent(frame2DImpl);
                // TODO: nigel: view = window.getPrimaryView();
                HUD mainHUD = WonderlandHUDManager.getHUDManager().getHUD("main");
                mainHUD.addComponent(this);
                this.setLocation(500, 300);
                this.setSize(frame2DImpl.getWidth(), frame2DImpl.getHeight() - 20);
                this.setVisible(true);
                //TODO: nigel: probably need to set view visibleUser: window.setVisible(true);
            } catch (Exception e) {
                logger.log(Level.WARNING, "failed to create HUD frame: " + e);
            }
        }
    }
}
