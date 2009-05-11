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
import org.jdesktop.wonderland.modules.appbase.client.Window2D;

/**
 * Maintains the display state of a HUD component
 *
 * @author nsimpson
 */
public class HUDComponentState {

    private static final Logger logger = Logger.getLogger(HUDComponentState.class.getName());
    private HUDComponent component;
    private HUDComponentVisualState state;
    private Window2D window;
    private HUDView2D view;
    private HUDView3D worldView;
    private boolean decorated;
    private int zorder;

    public enum HUDComponentVisualState {

        MINIMIZED, NORMAL, MAXIMIZED, ICONIFIED
    };

    public HUDComponentState(HUDComponent component) {
        this(component, false, HUDComponentVisualState.NORMAL, 0);
    }

    public HUDComponentState(HUDComponent component,
            boolean decorated, HUDComponentVisualState state, int zorder) {
        this.component = component;
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
     * Sets the window of the HUDComponent
     * @param window the HUDComponent's window
     */
    public void setWindow(Window2D window) {
        this.window = window;
    }

    /**
     * Gets the HUDComponent's window
     * @return the HUDComponent's window
     */
    public Window2D getWindow() {
        return window;
    }

    /**
     * Sets the view of the HUDComponent
     * @param view the HUDComponent's view
     */
    public void setView(HUDView2D view) {
        this.view = view;
    }

    /**
     * Gets the HUDComponent's view
     * @return the HUDComponent's view
     */
    public HUDView2D getView() {
        return view;
    }

    /**
     * Sets the world view of the HUDComponent
     * @param view the HUDComponent's view
     */
    public void setWorldView(HUDView3D view) {
        this.worldView = view;
    }

    /**
     * Gets the HUDComponent's world view
     * @return the HUDComponent's world view
     */
    public HUDView3D getWorldView() {
        return worldView;
    }

    /**
     * Gets whether the HUDComponent is visible
     * @return true if the HUDComponent is visible, false if it's hidden
     */
    public boolean isVisible() {
        return ((view != null) && (view.isActuallyVisible()));
    }

    /**
     * Gets whether the HUDComponent is visible in world
     * @return true if the HUDComponent is visible in world, false if it's hidden
     */
    public boolean isWorldVisible() {
        return ((worldView != null) && (worldView.isActuallyVisible()));
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
