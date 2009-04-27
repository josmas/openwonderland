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

import com.jme.math.Vector2f;
import com.jme.math.Vector3f;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Logger;
import javax.swing.JComponent;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.hud.HUDComponent;
import org.jdesktop.wonderland.client.hud.HUDComponent.DisplayMode;
import org.jdesktop.wonderland.client.hud.HUDComponentEvent;
import org.jdesktop.wonderland.client.hud.HUDComponentListener;
import org.jdesktop.wonderland.client.hud.HUDComponentManager;
import org.jdesktop.wonderland.client.hud.HUDLayoutManager;
import org.jdesktop.wonderland.modules.appbase.client.Window2D;
import org.jdesktop.wonderland.modules.appbase.client.Window2D.Type;
import org.jdesktop.wonderland.modules.appbase.client.swing.WindowSwing;
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

    // a mapping between HUD components and their states
    private Map HUDStateMap;
    // the layout manager for the HUD
    protected HUDLayoutManager layout;
    // displays HUD components on the glass
    protected HUDView2DDisplayer hudDisplayer;
    // displays HUD components in-world, associated with some cell
    protected HUDView3DDisplayer worldDisplayer;

    public WonderlandHUDComponentManager() {
        HUDStateMap = Collections.synchronizedMap(new HashMap());
    }

    /**
     * {@inheritDoc}
     */
    public void addComponent(HUDComponent component) {
        logger.fine("adding HUD component to component manager: " + component + ", " + component.getWidth() + "x" + component.getHeight());
        Window2D window = null;
        HUDComponentState state = new HUDComponentState(component);

        try {
            HUDApp2D hudApp = new HUDApp2D("HUD", new ControlArbHUD(), new Vector2f(0.2f, 0.2f));
            window = hudApp.createWindow(component.getWidth(), component.getHeight(), Type.PRIMARY,
                    false, new Vector2f(0.2f, 0.2f), "HUD component");

            JComponent comp = ((HUDComponent2D) component).getComponent();
            ((WindowSwing) window).setComponent(comp);
        } catch (InstantiationException e) {
            logger.warning("failed to create window for HUD component: " + e);
        }
        state.setWindow(window);
        component.addComponentListener(this);
        HUDStateMap.put(component, state);
    }

    /**
     * {@inheritDoc}
     */
    public void removeComponent(HUDComponent component) {
        HUDComponentState state = (HUDComponentState) HUDStateMap.get(component);

        if (state != null) {
            // remove on-HUD view
            HUDView2D view2D = state.getView();
            if (view2D != null) {
                view2D.cleanup();
                view2D = null;
            }

            // remove in-world view
            HUDView3D view3D = state.getWorldView();
            if (view3D != null) {
                view3D.cleanup();
                view3D = null;
            }
            component.removeComponentListener(this);
            HUDStateMap.remove(component);
        }
    }

    /**
     * {@inheritDoc}
     */
    public Iterator<HUDComponent> getComponents() {
        return HUDStateMap.entrySet().iterator();
    }

    private void componentVisible(HUDComponent2D component) {
        logger.info("showing HUD component on HUD: " + component);

        HUDComponentState state = (HUDComponentState) HUDStateMap.get(component);

        if (state.isVisible()) {
            return;
        }

        HUDView2D view = state.getView();

        if (view == null) {
            if (hudDisplayer == null) {
                logger.fine("creating new HUD displayer");
                hudDisplayer = new HUDView2DDisplayer();
            }

            logger.fine("creating new HUD view");
            view = hudDisplayer.createView(state.getWindow());
            state.setView(view);
        }

        logger.fine("displaying HUD view");
        view.setOrtho(true, false);
        view.setVisibleApp(true);
        view.setVisibleUser(true, false);
        view.setLocationOrtho(new Vector2f(component.getX(), component.getY()), false);
        view.update();
    }

    private void componentInvisible(HUDComponent2D component) {
        logger.info("hiding HUD component on HUD: " + component);

        HUDComponentState state = (HUDComponentState) HUDStateMap.get(component);

        if (!state.isVisible()) {
            return;
        }

        HUDView2D view = state.getView();

        if (view != null) {
            logger.fine("hiding HUD view");
            view.setVisibleApp(false);
            view.setVisibleUser(false, false);
            view.update();
        } else {
            logger.warning("attempt to set HUD invisible with no HUD view");
        }
    }

    private void componentWorldVisible(HUDComponent2D component) {
        logger.info("showing HUD component in world: " + component);

        HUDComponentState state = (HUDComponentState) HUDStateMap.get(component);

        if (state.isWorldVisible()) {
            return;
        }

        Cell cell = component.getCell();
        if (cell != null) {
            // can only create world views of HUD components that are
            // associated with a cell
            HUDView3D worldView = state.getWorldView();

            if (worldView == null) {
                if (worldDisplayer == null) {
                    logger.fine("creating new world displayer");
                    worldDisplayer = new HUDView3DDisplayer(cell);
                }

                logger.fine("creating new in-world view");
                worldView = worldDisplayer.createView(state.getWindow());
                worldView.setPixelScale(new Vector2f(0.02f, 0.02f));
                worldView.setTranslationUser(new Vector3f(0.0f, -5.9f, 0.05f));
                //worldView.setOffset(new Point(400, 0));
                state.setWorldView(worldView);
            }

            logger.fine("displaying in-world view");
            worldView.setOrtho(false, false);
            worldView.setVisibleApp(true);
            worldView.setVisibleUser(true, false);
            worldView.update();
        }
    }

    private void componentWorldInvisible(HUDComponent2D component) {
        logger.info("hiding HUD component in world: " + component);

        HUDComponentState state = (HUDComponentState) HUDStateMap.get(component);

        if (!state.isWorldVisible()) {
            return;
        }

        HUDView3D worldView = state.getWorldView();

        if (worldView != null) {
            logger.fine("hiding in-world view");
            worldView.setVisibleApp(false);
            worldView.setVisibleUser(false, false);
            worldView.update();
        } else {
            logger.warning("attempt to set world invisible with no world view");
        }
    }

    private void componentMoved(HUDComponent2D component) {
        logger.info("moving HUD component: " + component);

        HUDComponentState state = (HUDComponentState) HUDStateMap.get(component);
        HUDView2D view = state.getView();
        if (view != null) {
            view.setLocationOrtho(new Vector2f(component.getX(), component.getY()));
        }
    }

    private void componentMovedWorld(HUDComponent2D component) {
        logger.info("moving HUD component in world: " + component);
    }

    private void componentViewChanged(HUDComponent2D component) {
        logger.info("changing HUD component view: " + component);

        HUDComponentState state = (HUDComponentState) HUDStateMap.get(component);
        HUDView2D view = state.getView();

        if (component.getDisplayMode().equals(DisplayMode.HUD)) {
            // moving to HUD
            view.setLocationOrtho(new Vector2f(component.getX(), component.getY()), false);
            view.setOrtho(true);
        } else {
            // moving to world
            view.setTranslationUser(component.getWorldLocation());
            view.setOrtho(false);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void HUDComponentChanged(HUDComponentEvent event) {
        HUDComponent2D comp = (HUDComponent2D) event.getComponent();
        HUDComponentState state = (HUDComponentState) HUDStateMap.get(comp);

        logger.fine("HUD component changed: " + event);

        switch (event.getEventType()) {
            case APPEARED:
                componentVisible(comp);
                break;
            case DISAPPEARED:
                componentInvisible(comp);
                break;
            case APPEARED_WORLD:
                componentWorldVisible(comp);
                break;
            case DISAPPEARED_WORLD:
                componentWorldInvisible(comp);
                break;
            case MOVED:
                componentMoved(comp);
                break;
            case MOVED_WORLD:
                componentMovedWorld(comp);
                break;
            case CHANGED_MODE:
                componentViewChanged(comp);
                break;
            case CREATED:
            case RESIZED:
            case MINIMIZED:
            case MAXIMIZED:
            case ICONIFIED:
            case ENABLED:
            case DISABLED:
                logger.info("TODO: handle HUD component event type: " + event.getEventType());
                break;
        }
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
    public void setVisible(HUDComponent component, boolean visible) {
        HUDComponentState state = (HUDComponentState) HUDStateMap.get(component);
        if (state != null) {
            component.setVisible(visible);
        }
    }

    /**
     * {@inheritDoc}
     */
    public boolean isVisible(HUDComponent component) {
        boolean visible = false;
        HUDComponentState state = (HUDComponentState) HUDStateMap.get(component);
        if (state != null) {
            visible = state.isVisible();
        }
        return visible;
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
