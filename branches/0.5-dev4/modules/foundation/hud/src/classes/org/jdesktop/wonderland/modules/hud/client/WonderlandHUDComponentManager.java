/*
 * Project Wonderland
 * 
 * Copyright (c) 2004-2009, Sun Microsystems, Inc., All Rights Reserved
 * 
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 * 
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License") { } you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 * 
 * Sun designates this particular file as subject to the "Classpath"
 * exception as provided by Sun in the License file that accompanied
 * this code.
 */
package org.jdesktop.wonderland.modules.hud.client;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.wonderland.client.hud.HUDComponent;
import org.jdesktop.wonderland.client.hud.HUDComponentEvent;
import org.jdesktop.wonderland.client.hud.HUDComponentListener;
import org.jdesktop.wonderland.client.hud.HUDComponentManager;
import org.jdesktop.wonderland.client.hud.HUDLayoutManager;
import org.jdesktop.wonderland.modules.hud.client.HUDComponentState.HUDComponentVisualState;

/**
 * A WonderlandHUDComponentManager manages a set of HUDComponents.
 *
 * It lays out HUDComponents within a HUD with a HUDLayoutManager layout manager.
 * It also decorates HUDComponents with a frame border that allows the user
 * to move, resize, minimize and maximize and close a HUDComponent.
 *
 * @author nsimpson
 */
public class WonderlandHUDComponentManager implements HUDComponentManager, HUDComponentListener {

    private static final Logger logger = Logger.getLogger(WonderlandHUDComponentManager.class.getName());

    // a mapping between HUDComponents and HUDComponentStates
    private Map HUDStateMap;
    // the layout manager for the HUD
    private HUDLayoutManager layout;

    public WonderlandHUDComponentManager() {
        HUDStateMap = Collections.synchronizedMap(new HashMap());
    }

    /**
     * {@inheritDoc}
     */
    public void addComponent(HUDComponent component) {
        component.addComponentListener(this);
        HUDStateMap.put(component, new HUDComponentState(component));
    }

    /**
     * {@inheritDoc}
     */
    public void removeComponent(HUDComponent component) {
        component.removeComponentListener(this);
        HUDStateMap.remove(component);
    }

    /**
     * {@inheritDoc}
     */
    public Iterator<HUDComponent> getComponents() {
        return HUDStateMap.entrySet().iterator();
    }

    /**
     * {@inheritDoc}
     */
    public void HUDComponentChanged(HUDComponentEvent event) {
        HUDComponent comp;

        logger.log(Level.FINE, "HUDComponentChanged: " + event);
        switch (event.getEventType()) {
            case APPEARED:
                logger.log(Level.INFO, "HUDComponent visible, decorating");
                comp = event.getComponent();
                break;
            case DISAPPEARED:
                logger.log(Level.INFO, "HUDComponent invisible, undecorating");
                comp = event.getComponent();
                break;
            case CREATED:
            case MOVED:
            case RESIZED:
            case MINIMIZED:
            case MAXIMIZED:
            case ICONIFIED:
            case ENABLED:
            case DISABLED:
                logger.log(Level.FINE, "TODO: handle event type: " + event.getEventType());
                break;
        }
    }

    /**
     * {@inheritDoc}
     */
    public void setLayout(HUDLayoutManager layout) {
        this.layout = layout;
    }

    /**
     * {@inheritDoc}
     */
    public HUDLayoutManager getLayout() {
        return layout;
    }

    /**
     * {@inheritDoc}
     */
    public void relayout() {
    }

    /**
     * {@inheritDoc}
     */
    public void relayout(HUDComponent component) {
    }

    /**
     * {@inheritDoc}
     */
    public void showComponent(HUDComponent component) {
        HUDComponentState state = (HUDComponentState) HUDStateMap.get(component);
        if (state != null) {
            state.setShowing(true);
        // TODO: update display
        }
    }

    /**
     * {@inheritDoc}
     */
    public void hideComponent(HUDComponent component) {
        HUDComponentState state = (HUDComponentState) HUDStateMap.get(component);
        if (state != null) {
            state.setShowing(false);
        // TODO: update display
        }
    }

    /**
     * {@inheritDoc}
     */
    public boolean isComponentShowing(HUDComponent component) {
        boolean showing = false;
        HUDComponentState state = (HUDComponentState) HUDStateMap.get(component);
        if (state != null) {
            showing = state.isShowing();
        }
        return showing;
    }

    /**
     * {@inheritDoc}
     */
    public void minimizeComponent(HUDComponent component) {
        HUDComponentState state = (HUDComponentState) HUDStateMap.get(component);
        if (state != null) {
            state.setState(HUDComponentVisualState.MINIMIZED);
        // TODO: update display
        }
    }

    /**
     * {@inheritDoc}
     */
    public void maximizeComponent(HUDComponent component) {
        HUDComponentState state = (HUDComponentState) HUDStateMap.get(component);
        if (state != null) {
            state.setState(HUDComponentVisualState.MAXIMIZED);
        // TODO: update display
        }
    }

    /**
     * {@inheritDoc}
     */
    public void raiseComponent(HUDComponent component) {
        HUDComponentState state = (HUDComponentState) HUDStateMap.get(component);
        if (state != null) {
            int zorder = state.getZOrder();
            state.setZOrder(zorder++);
        // TODO: update component
        }
    }

    /**
     * {@inheritDoc}
     */
    public void lowerComponent(HUDComponent component) {
        HUDComponentState state = (HUDComponentState) HUDStateMap.get(component);
        if (state != null) {
            int zorder = state.getZOrder();
            state.setZOrder(zorder--);
        // TODO: update component
        }
    }

    /**
     * {@inheritDoc}
     */
    public int getComponentZOrder(HUDComponent component) {
        int zorder = 0;
        HUDComponentState state = (HUDComponentState) HUDStateMap.get(component);
        if (state != null) {
            zorder = state.getZOrder();

        }
        return zorder;
    }

    /**
     * {@inheritDoc}
     */
    public void decorateComponent(HUDComponent component, boolean decorate) {
        HUDComponentState state = (HUDComponentState) HUDStateMap.get(component);
        if (state != null) {
            state.setDecorated(decorate);
        // TODO: update display
        }
    }

    /**
     * {@inheritDoc}
     */
    public boolean isComponentDecorated(HUDComponent component) {
        boolean decorated = false;
        HUDComponentState state = (HUDComponentState) HUDStateMap.get(component);
        if (state != null) {
            decorated = state.isDecorated();
        }
        return decorated;
    }
}
