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
import com.jme.scene.Node;
import com.jme.scene.state.BlendState;
import com.jme.scene.state.RenderState;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Logger;
import javax.swing.JComponent;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.mtgame.RenderComponent;
import org.jdesktop.mtgame.WorldManager;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.hud.HUDComponent;
import org.jdesktop.wonderland.client.hud.HUDComponent.DisplayMode;
import org.jdesktop.wonderland.client.hud.HUDComponentEvent;
import org.jdesktop.wonderland.client.hud.HUDComponentListener;
import org.jdesktop.wonderland.client.hud.HUDComponentManager;
import org.jdesktop.wonderland.client.hud.HUDLayoutManager;
import org.jdesktop.wonderland.client.input.Event;
import org.jdesktop.wonderland.client.jme.ClientContextJME;
import org.jdesktop.wonderland.client.jme.input.MouseEnterExitEvent3D;
import org.jdesktop.wonderland.client.jme.input.test.EnterExitEvent3DLogger;
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
public class WonderlandHUDComponentManager implements HUDComponentManager,
        HUDComponentListener {

    private static final Logger logger = Logger.getLogger(WonderlandHUDComponentManager.class.getName());
    // a mapping between HUD components and their states
    private Map<HUDComponent, HUDComponentState> hudStateMap;
    // the layout manager for the HUD
    protected HUDLayoutManager layout;
    // displays HUD components on the glass
    protected HUDView2DDisplayer hudDisplayer;
    // displays HUD components in-world, associated with some cell
    protected HUDView3DDisplayer worldDisplayer;
    //
    protected HUDApp2D hudApp;
    protected Vector2f hudPixelScale = new Vector2f(0.75f, 0.75f);
    protected Vector2f worldPixelScale = new Vector2f(0.013f, 0.013f);
    protected Point dragStartPoint;
    protected Point dragOffset;
    protected Point lastPoint;

    public WonderlandHUDComponentManager() {
        hudStateMap = Collections.synchronizedMap(new HashMap());
    }

    public Window2D createWindow(HUDComponent component) {
        Window2D window = null;

        logger.fine("creating window for HUD component: " + component);

        //if (hudApp == null) {
        hudApp = new HUDApp2D("HUD", new ControlArbHUD(), worldPixelScale);
        //}
        try {
            // REMIND: pixel scale doesn't match
            window = hudApp.createWindow(component.getWidth(), component.getHeight(), Type.PRIMARY,
                    false, hudPixelScale, "HUD component");

            JComponent comp = ((HUDComponent2D) component).getComponent();
            ((WindowSwing) window).setComponent(comp);
        } catch (InstantiationException e) {
            logger.warning("failed to create window for HUD component: " + e);
        }

        return window;
    }

    /**
     * {@inheritDoc}
     */
    public void addComponent(final HUDComponent component) {
        logger.fine("adding HUD component to component manager: " + component);

        HUDComponentState state = new HUDComponentState(component);
        HUDComponent2D component2D = (HUDComponent2D)component;
        Window2D window;

        if (component2D.getWindow() != null) {
            window = component2D.getWindow();
        } else {
            window = createWindow(component);
            component2D.setWindow(window);
        }

        window.addEventListener(new EnterExitEvent3DLogger() {

            @Override
            public void commitEvent(Event event) {
                MouseEnterExitEvent3D mouseEvent = (MouseEnterExitEvent3D) event;
                switch (mouseEvent.getID()) {
                    case MouseEvent.MOUSE_ENTERED:
                        logger.fine("mouse entered component: " + component);
                        break;
                    case MouseEvent.MOUSE_EXITED:
                        logger.fine("mouse exited component: " + component);
                        break;
                    default:
                        break;
                }
            }
        });
        state.setWindow(window);

        component.addComponentListener(this);
        hudStateMap.put(component, state);
    }

    /**
     * {@inheritDoc}
     */
    public void removeComponent(HUDComponent component) {
        HUDComponentState state = (HUDComponentState) hudStateMap.get(component);

        if (state != null) {
            // remove on-HUD view
            HUDView2D view2D = state.getView();
            if (view2D != null) {
                view2D.cleanup();
                view2D = null;
            }

            // remove frame from on-HUD view
            HUDView2D frameView = state.getFrameView();
            if (frameView != null) {
                frameView.cleanup();
                frameView = null;
            }

            // remove in-world view
            HUDView3D view3D = state.getWorldView();
            if (view3D != null) {
                view3D.cleanup();
                view3D = null;
            }

            component.removeComponentListener(this);
            hudStateMap.remove(component);
            state = null;
        }
    }

    /**
     * {@inheritDoc}
     */
    public Iterator<HUDComponent> getComponents() {
        return hudStateMap.keySet().iterator();
    }

    public void createFrame(HUDComponent component) {
        logger.fine("adding frame to HUD component: " + component);
        HUDFrame2DImpl frameImpl = new HUDFrame2DImpl();
        HUDFrame2D frame = new HUDFrame2D(frameImpl, component);
        Window2D window = createWindow(frame);

        // register listeners for events on the frame
        frameImpl.addActionListener(frame);
        frameImpl.addMouseListener(frame);
        frameImpl.addMouseMotionListener(frame);

        HUDComponentState state = hudStateMap.get(component);
        state.setFrame(frame);
        state.setFrameWindow(window);
    }

    private void moveFrame(HUDComponent2D component) {
        HUDComponentState state = (HUDComponentState) hudStateMap.get(component);
        HUDView2D view = state.getView();
        HUDView2D frameView = state.getFrameView();

        // position the frame view on the HUD pane
        // a view's coordinates are at the center of the view, so the top edge
        // of a view is half the height
        Vector2f viewLocation = view.getLocationOrtho();
        float y = viewLocation.y + view.getDisplayerLocalHeight() / 2f +
                frameView.getDisplayerLocalHeight() / 2f;
        viewLocation.setY(y);
        frameView.setLocationOrtho(viewLocation);
    }

    private void resizeFrame(HUDComponent2D component) {
        HUDComponentState state = (HUDComponentState) hudStateMap.get(component);
        HUDView2D frameView = state.getFrameView();

        if (frameView != null) {
            Window2D componentWindow = state.getWindow();
            Window2D frameWindow = state.getFrameWindow();

            if (frameWindow.getWidth() != componentWindow.getWidth()) {
                state.getFrameWindow().setSize(state.getWindow().getWidth(),
                        state.getFrameWindow().getHeight());
            }
        }
    }

    public void decorateComponent(HUDComponent component, boolean decorate) {
        showFrame((HUDComponent2D) component, decorate);
    }

    private void showFrame(HUDComponent2D component, boolean visible) {
        if (component.getDecoratable() == true) {
            HUDComponentState state = (HUDComponentState) hudStateMap.get(component);
            HUDView2D view = state.getView();
            HUDView2D frameView = state.getFrameView();

            if ((visible == true) && (frameView == null)) {
                // component needs a frame
                if (hudDisplayer == null) {
                    logger.fine("creating new HUD displayer");
                    hudDisplayer = new HUDView2DDisplayer();
                }

                createFrame(component);

                logger.fine("creating new HUD frame view");
                frameView = hudDisplayer.createView(state.getFrameWindow());
                frameView.setOrtho(true, false);
                frameView.setPixelScaleOrtho(hudPixelScale, false);
                state.setFrameView(frameView);
            }
            if (frameView != null) {
                // adjust the frame window to fit the width of the HUD component
                resizeFrame(component);

                // position the frame view on the HUD pane
                moveFrame(component);

                // display/hide the frame view
                frameView.setVisibleApp(visible);
                frameView.setVisibleUser(visible);
            }
        }
    }

    private void componentVisible(HUDComponent2D component) {
        logger.info("showing HUD component on HUD: " + component);

        HUDComponentState state = (HUDComponentState) hudStateMap.get(component);

        if (state.isVisible()) {
            return;
        }

        HUDView2D view = state.getView();

        if (view == null) {
            if (hudDisplayer == null) {
                hudDisplayer = new HUDView2DDisplayer();
            }

            view = hudDisplayer.createView(state.getWindow());
            state.setView(view);
            if (layout != null) {
                layout.addView(component, view);
            }
        }

        // move the component to the screen
        view.setOrtho(true, false);
        view.setPixelScaleOrtho(hudPixelScale, false);

        // position the component on the screen
        Vector2f location = (layout != null) ? layout.getLocation(component) : new Vector2f(component.getX(), component.getY());
        view.setLocationOrtho(location, false);
        logger.fine("displaying HUD view at: " + location);

        // display the component
        view.setVisibleApp(true, false);
        view.setVisibleUser(true);

        // add a frame if this component wants to be decorated
        if (component.getDecoratable()) {
            showFrame(component, true);
        }
    }

    public void setTransparent(HUDView2D view) {
        Node node = view.getNode();
        Entity entity = view.getEntity();
        WorldManager wm = ClientContextJME.getWorldManager();
        BlendState as = (BlendState) wm.getRenderManager().createRendererState(RenderState.RS_BLEND);
        as.setEnabled(true);
        as.setBlendEnabled(true);
        as.setSourceFunction(BlendState.SourceFunction.SourceAlpha);
        as.setDestinationFunction(BlendState.DestinationFunction.OneMinusSourceAlpha);
        node.setRenderState(as);
        RenderComponent rc = entity.getComponent(RenderComponent.class);
    //AlphaProcessor proc = new AlphaProcessor("", wm, view.getGeometryNode(), 0.01f);
    //entity.addComponent(AlphaProcessor.class, proc);
    }

    private void componentInvisible(HUDComponent2D component) {
        logger.info("hiding HUD component on HUD: " + component);

        HUDComponentState state = (HUDComponentState) hudStateMap.get(component);

        if (!state.isVisible()) {
            return;
        }

        HUDView2D view = state.getView();

        if (view != null) {
            logger.fine("hiding HUD view");
            showFrame(component, false);
            view.setVisibleApp(false, false);
            view.setVisibleUser(false);
        } else {
            logger.warning("attempt to set HUD invisible with no HUD view");
        }
    }

    private void componentWorldVisible(HUDComponent2D component) {
        logger.info("showing HUD component in world: " + component);

        HUDComponentState state = (HUDComponentState) hudStateMap.get(component);

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
                worldView.setPixelScale(worldPixelScale);
                state.setWorldView(worldView);
            }

            logger.fine("displaying in-world view");
            worldView.setOrtho(false, false);
            worldView.setPixelScale(worldPixelScale);
            worldView.setVisibleApp(true);
            worldView.setVisibleUser(true, false);
            componentMovedWorld(component);
            worldView.update();
        }
    }

    private void componentWorldInvisible(HUDComponent2D component) {
        logger.info("hiding HUD component in world: " + component);

        HUDComponentState state = (HUDComponentState) hudStateMap.get(component);

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

        HUDComponentState state = (HUDComponentState) hudStateMap.get(component);
        HUDView2D view = state.getView();
        if (view != null) {
            Vector2f location = new Vector2f(component.getX() + view.getDisplayerLocalWidth() / 2f, 
                    component.getY() + view.getDisplayerLocalHeight() / 2f);
            view.setLocationOrtho(location);
            moveFrame(component);
        }
    }

    private void componentMovedWorld(HUDComponent2D component) {
        logger.info("moving HUD component in world: " + component);

        HUDComponentState state = (HUDComponentState) hudStateMap.get(component);
        HUDView3D view = state.getWorldView();
        if (view != null) {
            Vector3f worldOffset = component.getWorldLocation();
            // position HUD in x, y
            view.setOffset(new Vector2f(worldOffset.x, worldOffset.y));
        }
    }

    private void componentViewChanged(HUDComponent2D component) {
        logger.info("changing HUD component view: " + component);

        HUDComponentState state = (HUDComponentState) hudStateMap.get(component);
        HUDView2D view = state.getView();

        if (component.getDisplayMode().equals(DisplayMode.HUD)) {
            // moving to HUD
            view.setLocationOrtho(new Vector2f(component.getX(), component.getY()), false);
            view.setOrtho(true);
        } else {
            // moving to world
            view.applyDeltaTranslationUser(component.getWorldLocation());
            view.setOrtho(false);
        }
    }

    private void componentMinimized(HUDComponent2D component) {
        logger.info("minimizing HUD component: " + component);
    }

    /**
     * {@inheritDoc}
     */
    public void HUDComponentChanged(HUDComponentEvent event) {
        HUDComponent2D comp = (HUDComponent2D) event.getComponent();
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
            case MINIMIZED:
                componentMinimized(comp);
                break;
            case CREATED:
            case RESIZED:
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
        HUDComponentState state = (HUDComponentState) hudStateMap.get(component);
        if (state != null) {
            component.setVisible(visible);
        }
    }

    /**
     * {@inheritDoc}
     */
    public boolean isVisible(HUDComponent component) {
        boolean visible = false;
        HUDComponentState state = (HUDComponentState) hudStateMap.get(component);
        if (state != null) {
            visible = state.isVisible();
        }
        return visible;
    }

    /**
     * {@inheritDoc}
     */
    public void minimizeComponent(HUDComponent component) {
        HUDComponentState state = (HUDComponentState) hudStateMap.get(component);
        if (state != null) {
            state.setState(HUDComponentVisualState.MINIMIZED);
        // TODO: update display
        }
    }

    /**
     * {@inheritDoc}
     */
    public void maximizeComponent(HUDComponent component) {
        HUDComponentState state = (HUDComponentState) hudStateMap.get(component);
        if (state != null) {
            state.setState(HUDComponentVisualState.MAXIMIZED);
        // TODO: update display
        }
    }

    /**
     * {@inheritDoc}
     */
    public void raiseComponent(HUDComponent component) {
        HUDComponentState state = (HUDComponentState) hudStateMap.get(component);
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
        HUDComponentState state = (HUDComponentState) hudStateMap.get(component);
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
        HUDComponentState state = (HUDComponentState) hudStateMap.get(component);
        if (state != null) {
            zorder = state.getZOrder();

        }
        return zorder;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isComponentDecorated(HUDComponent component) {
        boolean decorated = false;
        HUDComponentState state = (HUDComponentState) hudStateMap.get(component);
        if (state != null) {
            decorated = state.getFrame() != null;
        }
        return decorated;
    }
}
