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

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Logger;
import org.jdesktop.wonderland.client.hud.HUD;
import org.jdesktop.wonderland.client.hud.HUDLayoutManager;
import org.jdesktop.wonderland.client.hud.HUDManager;

/**
 * The HUD system allows multiple HUD instances to share a client window.
 * Each HUD has a 2D position and a width and height.
 *
 * A HUDManager manages the placement and visual attributes of all the
 * HUD instances in a given client window.
 *
 * @author nsimpson
 */
public class WonderlandHUDManager extends HUDManager {

    private static final Logger logger = Logger.getLogger(WonderlandHUDManager.class.getName());
    protected String name;
    protected HUDLayoutManager layout;
    protected boolean visible;

    protected enum visualState {

        MINIMIZED, NORMAL, MAXIMIZED
    };
    protected static Map<String, HUD> huds = Collections.synchronizedMap(new HashMap());

    /**
     * {@inheritDoc}
     */
    public void addHUD(HUD hud) {
        if (hud != null) {
            huds.put(hud.getName(), hud);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void removeHUD(HUD hud) {
        if ((hud != null) && huds.containsValue(hud)) {
            huds.remove(hud.getName());
        }
    }

    /**
     * {@inheritDoc}
     */
    public HUD getHUD(String name) {
        HUD hud = null;
        if (name != null) {
            hud = huds.get(name);
        }
        return hud;
    }

    /**
     * {@inheritDoc}
     */
    public Iterator<HUD> getHUDs() {
        Collection c = huds.values();
        return c.iterator();
    }

    /**
     * {@inheritDoc}
     */
    public void setLayoutManager(HUDLayoutManager layout) {
        this.layout = layout;
    }

    /**
     * {@inheritDoc}
     */
    public HUDLayoutManager getLayoutManager() {
        return layout;
    }

    /**
     * {@inheritDoc}
     */
    public void showHUD(HUD hud) {
        if (huds.containsValue(hud)) {
            hud.show();
        }
    }

    /**
     * {@inheritDoc}
     */
    public void hideHUD(HUD hud) {
        if (huds.containsValue(hud)) {
            hud.hide();
        }
    }

    /**
     * {@inheritDoc}
     */
    public boolean isHUDShowing(HUD hud) {
        boolean showing = false;

        if ((hud != null) && huds.containsValue(hud)) {
            showing = hud.isShowing();
        }
        return showing;
    }

    /**
     * {@inheritDoc}
     */
    public void minimizeHUD(HUD hud) {
        // TODO: implement
    }

    /**
     * {@inheritDoc}
     */
    public void maximizeHUD(HUD hud) {
        // TODO: implement
    }

    /**
     * {@inheritDoc}
     */
    public void raiseHUD(HUD hud) {
        // TODO: implement
    }

    /**
     * {@inheritDoc}
     */
    public void lowerHUD(HUD hud) {
        // TODO: implement
    }
}
