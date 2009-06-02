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

import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;
import javax.swing.JComponent;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.hud.HUD;
import org.jdesktop.wonderland.client.hud.HUDComponent;
import org.jdesktop.wonderland.client.hud.HUDComponentManager;
import org.jdesktop.wonderland.client.hud.HUDLayoutManager;

/**
 * A WonderlandHUD is a 2D region of the Wonderland client window on which HUDComponents
 * can be displayed.
 * 
 * A client may have multiple HUDs. For example, a Status HUD which displays
 * status information about the user's session, and an Audio HUD for audio
 * controls.
 * 
 * A HUD contains HUDComponents which are visual objects such as a 2D control
 * panel or a representation of a 3D object. HUDComponents are laid out within
 * a HUD by a HUDLayoutManager.
 *
 *
 * @author nsimpson
 */
public class WonderlandHUD implements HUD {

    private static final Logger logger = Logger.getLogger(WonderlandHUD.class.getName());
    protected List components;
    protected HUDComponentManager componentManager;
    protected String name;
    protected Rectangle bounds;
    protected boolean visible = false;
    protected float transparency = 1.0f;
    protected HUDLayoutManager layout;
    private static final int HUD_DEFAULT_X = 0;
    private static final int HUD_DEFAULT_Y = 0;
    private static final int HUD_DEFAULT_WIDTH = 800;
    private static final int HUD_DEFAULT_HEIGHT = 600;

    public WonderlandHUD() {
        components = Collections.synchronizedList(new ArrayList());
        bounds = new Rectangle(HUD_DEFAULT_X, HUD_DEFAULT_Y,
                HUD_DEFAULT_WIDTH, HUD_DEFAULT_HEIGHT);
        layout = new HUDAbsoluteLayoutManager();
    }

    public WonderlandHUD(Rectangle bounds) {
        this();
        this.bounds = bounds;
    }

    public WonderlandHUD(int x, int y, int width, int height) {
        this(new Rectangle(x, y, width, height));
    }

    /**
     * {@inheritDoc}
     */
    public HUDComponent createComponent(JComponent component) {
        return new HUDComponent2D(component);
    }

    /**
     * {@inheritDoc}
     */
    public HUDComponent createComponent(JComponent component, Cell cell) {
        return new HUDComponent2D(component, cell);
    }

    /**
     * {@inheritDoc}
     */
    public void addComponent(HUDComponent component) {
        components.add(component);

        if (componentManager != null) {
            componentManager.addComponent(component);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void removeComponent(HUDComponent component) {
        if (componentManager != null) {
            componentManager.removeComponent(component);
        }
        components.remove(component);
    }

    /**
     * {@inheritDoc}
     */
    public Iterator<HUDComponent> getComponents() {
        return components.iterator();
    }

    /**
     * {@inheritDoc}
     */
    public boolean hasComponents() {
        return !components.isEmpty();
    }

    /**
     * {@inheritDoc}
     */
    public void setComponentManager(HUDComponentManager componentManager) {
        if (components.size() > 0) {
            HUDComponent comps[] = (HUDComponent[]) components.toArray();

            if (this.componentManager != null) {
                // already have a component manager, so remove the components
                // it's managing in this HUD
                for (HUDComponent component : comps) {
                    this.componentManager.removeComponent(component);
                }
            }
            if (componentManager != null) {
                // add the components in this HUD to the new component manager
                for (HUDComponent component : comps) {
                    component.addComponentListener((WonderlandHUDComponentManager) componentManager);
                }
            }
        }

        this.componentManager = componentManager;
    }

    /**
     * {@inheritDoc}
     */
    public HUDComponentManager getComponentManager() {
        return componentManager;
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
    public void setName(String name) {
        this.name = name;
    }

    /**
     * {@inheritDoc}
     */
    public String getName() {
        return name;
    }

    /**
     * {@inheritDoc}
     */
    public void setWidth(int width) {
        bounds.width = width;
    }

    /**
     * {@inheritDoc}
     */
    public int getWidth() {
        return bounds.width;
    }

    /**
     * {@inheritDoc}
     */
    public void setHeight(int height) {
        bounds.height = height;
    }

    /**
     * {@inheritDoc}
     */
    public int getHeight() {
        return bounds.height;
    }

    /**
     * {@inheritDoc}
     */
    public void setBounds(Rectangle bounds) {
        bounds.setBounds(bounds);
    }

    /**
     * {@inheritDoc}
     */
    public Rectangle getBounds() {
        return bounds;
    }

    /**
     * {@inheritDoc}
     */
    public void setLocation(Point p) {
        bounds.setLocation(p);
    }

    /**
     * {@inheritDoc}
     */
    public Point getLocation() {
        return bounds.getLocation();
    }

    /**
     * {@inheritDoc}
     */
    public void show() {
        this.visible = true;
    }

    /**
     * {@inheritDoc}
     */
    public void hide() {
        this.visible = false;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isShowing() {
        return visible;
    }

    /**
     * {@inheritDoc}
     */
    public void setTransparency(float transparency) {
        this.transparency = transparency;
    }

    /**
     * {@inheritDoc}
     */
    public float getTransparency() {
        return transparency;
    }
}
