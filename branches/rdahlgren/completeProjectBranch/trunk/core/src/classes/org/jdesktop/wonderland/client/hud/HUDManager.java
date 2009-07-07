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

import java.util.Iterator;

/**
 *
 * @author nsimpson
 */
public abstract class HUDManager {

    /**
     * Adds a HUD to the set of managed HUDs
     * @param hud the HUD to add
     */
    public abstract void addHUD(HUD hud);

    /**
     * Removes a HUD from the set of managed HUDs
     * @param hud the HUD to remove
     */
    public abstract void removeHUD(HUD hud);

    /**
     * Gets a HUD by name
     * @param name the name of the HUD to get
     * @return a HUD if there's a HUD with the specified name
     */
    public abstract HUD getHUD(String name);

    /**
     * Gets an iterator that will iterate over the current set of HUDs managed
     * by the HUDManager
     * @return an iterator for HUDs
     */
    public abstract Iterator<HUD> getHUDs();

    /**
     * Assigns a layout manager which determines how HUDs are laid out by the
     * HUDManager
     * @param layout
     */
    public abstract void setLayoutManager(HUDLayoutManager layout);

    /**
     * Gets the layout manager
     * @return the HUDLayoutManager, if set
     */
    public abstract HUDLayoutManager getLayoutManager();

    /**
     * Show a HUD
     * @param hud the HUD to show
     */
    public abstract void showHUD(HUD hud);

    /**
     * Hide a HUD
     * @param hud the HUD to hide
     */
    public abstract void hideHUD(HUD hud);

    /**
     * Gets whether the HUD manager is showing a specified HUD
     * @param hud the HUD to check for visibility
     * @return true if the HUD is showing, false if the HUD is hidden
     */
    public abstract boolean isHUDShowing(HUD hud);

    /**
     * Minimize a HUD
     * @param hud the HUD to minimize
     */
    public abstract void minimizeHUD(HUD hud);

    /**
     * Maximize a HUD
     * @param hud the HUD to maximize
     */
    public abstract void maximizeHUD(HUD hud);

    /**
     * Raise a HUD in the stacking order
     * @param hud the HUD to raise
     */
    public abstract void raiseHUD(HUD hud);

    /**
     * Lower a HUD in the stacking order
     * @param hud the HUD to lower
     */
    public abstract void lowerHUD(HUD hud);
}
