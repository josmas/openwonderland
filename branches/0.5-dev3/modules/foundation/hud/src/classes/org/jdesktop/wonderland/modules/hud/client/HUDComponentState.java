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

import java.util.logging.Logger;
import org.jdesktop.wonderland.client.hud.HUDComponent;

/**
 * HUDComponentState maintains the display state of all the HUDComponents
 * in a HUD.
 *
 * @author nsimpson
 */
public class HUDComponentState {

    private static final Logger logger = Logger.getLogger(HUDComponentState.class.getName());
    private HUDComponent component;
    private HUDComponentVisualState state;
    private boolean showing;
    private boolean decorated;
    private int zorder;

    public enum HUDComponentVisualState {

        MINIMIZED, NORMAL, MAXIMIZED, ICONIFIED
    };

    public HUDComponentState(HUDComponent component) {
        this(component, true, true, HUDComponentVisualState.NORMAL, 0);
    }

    public HUDComponentState(HUDComponent component, boolean showing,
            boolean decorated, HUDComponentVisualState state, int zorder) {
        this.component = component;
        this.showing = showing;
        this.decorated = decorated;
        this.state = state;
        this.zorder = zorder;
    }

    /**
     * Sets the managed HUDComponent
     * @param component the HUDComponent
     */
    public void setComponent(HUDComponent component) {
        this.component = component;
    }

    /**
     * Gets the managed HUDComponent
     * @return the managed HUDComponent
     */
    public HUDComponent getComponent() {
        return component;
    }

    /**
     * Sets whether the HUDComponent is visible
     * @param showing true to show the HUDComponent, false to hide
     */
    public void setShowing(boolean showing) {
        this.showing = showing;
    }

    /**
     * Gets whether the HUDComponent is visible
     * @return true if the HUDComponent is visible, false if it's hidden
     */
    public boolean isShowing() {
        return showing;
    }

    /**
     * Sets whether the HUDComponent is decorated
     * @param decorated true to decorate the HUDComponent, false to remove
     * decorations
     */
    public void setDecorated(boolean decorated) {
        this.decorated = decorated;
    }

    /**
     * Gets whether the HUDComponent is decorated
     * @return true if the HUDComponent is decorated, false if it is not
     */
    public boolean isDecorated() {
        return decorated;
    }

    /**
     * Sets the visual state of the HUDComponent, whether it's minimized,
     * maximized or in normal state
     * @param state the new visual state of the HUDComponent
     */
    public void setState(HUDComponentVisualState state) {
        this.state = state;
    }

    /**
     * Get's the visual state of the HUDComponent
     * @return the visual state
     */
    public HUDComponentVisualState getState() {
        return state;
    }

    /**
     * Sets the z-order of the HUDComponent
     * @param zorder the z-order of the HUDComponent
     */
    public void setZOrder(int zorder) {
        this.zorder = zorder;
    }

    /**
     * Gets the z-order of the HUDComponent
     * @return the HUDComponent's z-order
     */
    public int getZOrder() {
        return zorder;
    }
}
