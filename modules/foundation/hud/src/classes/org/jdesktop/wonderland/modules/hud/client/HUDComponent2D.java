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

import com.jme.math.Vector3f;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;
import javax.swing.JComponent;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.hud.HUDComponent;
import org.jdesktop.wonderland.client.hud.HUDComponentEvent;
import org.jdesktop.wonderland.client.hud.HUDComponentEvent.ComponentEventType;
import org.jdesktop.wonderland.client.hud.HUDComponentListener;
import org.jdesktop.wonderland.client.jme.JmeClientMain;

/**
 * A HUDComponent2D is a 2D object that can be displayed on the HUD.
 * It has a position (x, y), a width and a height.
 *
 * A HUDComponent2D can be visible or invisible. It can also be enabled,
 * in which case it responds to mouse and keyboard events, or disabled.
 *
 * @author nsimpson
 */
public class HUDComponent2D implements HUDComponent {

    private static final Logger logger = Logger.getLogger(HUDComponent2D.class.getName());
    protected List<HUDComponentListener> listeners;
    protected Cell cell;
    protected JComponent component;
    protected HUDComponentEvent event;
    protected Rectangle2D bounds;     // on-HUD location
    protected Vector3f worldLocation; // in-world location
    protected boolean visible = false;
    protected boolean worldVisible = false;
    protected boolean enabled = false;
    protected DisplayMode mode = DisplayMode.HUD;

    public HUDComponent2D() {
        listeners = Collections.synchronizedList(new ArrayList());
        event = new HUDComponentEvent(this);
        bounds = new Rectangle2D.Double();
    }

    public HUDComponent2D(JComponent component) {
        this();
        this.component = component;
        Dimension size = component.getPreferredSize();
        setBounds(0, 0, size.width, size.height);
        JmeClientMain.getFrame().getCanvas3DPanel().add(component);
        event.setComponent(this);
    }

    public HUDComponent2D(JComponent component, Cell cell) {
        this(component);
        this.cell = cell;
    }

    public void setComponent(JComponent component) {
        this.component = component;
    }
    
    public JComponent getComponent() {
        return component;
    }

    /**
     * Associates a cell with this HUD component for in-world display
     * @param cell the cell to associate with this HUD component
     */
    public void setCell(Cell cell) {
        this.cell = cell;
    }

    /**
     * Gets the cell associated with this HUD component
     * @return the associated cell
     */
    public Cell getCell() {
        return cell;
    }
    
    /**
     * {@inheritDoc}
     */
    public void setBounds(Rectangle bounds) {
        this.bounds = bounds;

        notifyListeners(ComponentEventType.RESIZED);
    }

    /**
     * {@inheritDoc}
     */
    public void setBounds(int x, int y, int width, int height) {
        setBounds(new Rectangle(x, y, width, height));
    }

    /**
     * {@inheritDoc}
     */
    public void setLocation(int x, int y) {
        bounds.setRect(x, y, bounds.getWidth(), bounds.getHeight());

        notifyListeners(ComponentEventType.MOVED);
    }

    /**
     * {@inheritDoc}
     */
    public void setLocation(Point p) {
        setLocation(p.x, p.y);
    }

    /**
     * {@inheritDoc}
     */
    public Point getLocation() {
        return new Point((int) bounds.getX(), (int) bounds.getY());
    }

    /**
     * Sets the in-world location of the component
     * @param location the 3D location of the component in-world
     */
    public void setWorldLocation(Vector3f location) {
        this.worldLocation = location;

        notifyListeners(ComponentEventType.MOVED_WORLD);
    }

    /**
     * Gets the in-world location of the component
     * @return the 3D location of the component in-world
     */
    public Vector3f getWorldLocation() {
        return worldLocation;
    }

    /**
     * {@inheritDoc}
     */
    public void setX(int x) {
        bounds.setRect(x, bounds.getY(), bounds.getWidth(), bounds.getHeight());

        notifyListeners(ComponentEventType.MOVED);
    }

    /**
     * {@inheritDoc}
     */
    public int getX() {
        return (int) bounds.getX();
    }

    /**
     * {@inheritDoc}
     */
    public void setY(int y) {
        bounds.setRect(bounds.getX(), y, bounds.getWidth(), bounds.getHeight());

        notifyListeners(ComponentEventType.MOVED);
    }

    /**
     * {@inheritDoc}
     */
    public int getY() {
        return (int) bounds.getY();
    }

    /**
     * {@inheritDoc}
     */
    public void setWidth(int width) {
        bounds.setRect(bounds.getX(), bounds.getY(), width, bounds.getHeight());

        notifyListeners(ComponentEventType.RESIZED);
    }

    /**
     * {@inheritDoc}
     */
    public int getWidth() {
        return (int) bounds.getWidth();
    }

    /**
     * {@inheritDoc}
     */
    public void setHeight(int height) {
        bounds.setRect(bounds.getX(), bounds.getY(), bounds.getWidth(), height);

        notifyListeners(ComponentEventType.RESIZED);
    }

    /**
     * {@inheritDoc}
     */
    public int getHeight() {
        return (int) bounds.getHeight();
    }

    /**
     * {@inheritDoc}
     */
    public void setSize(int width, int height) {
        bounds.setRect(bounds.getX(), bounds.getY(), width, height);

        notifyListeners(ComponentEventType.RESIZED);
    }

    /**
     * {@inheritDoc}
     */
    public void setSize(Dimension dimension) {
        bounds.setRect(bounds.getX(), bounds.getY(), dimension.getWidth(), dimension.getHeight());

        notifyListeners(ComponentEventType.RESIZED);
    }

    /**
     * {@inheritDoc}
     */
    public Dimension getSize() {
        return new Dimension((int) bounds.getWidth(), (int) bounds.getHeight());
    }

    /**
     * {@inheritDoc}
     */
    public void setVisible(boolean visible) {
        if (this.visible == visible) {
            return;
        }
        this.visible = visible;

        notifyListeners((visible == true) ? ComponentEventType.APPEARED
                : ComponentEventType.DISAPPEARED);
    }

    /**
     * {@inheritDoc}
     */
    public boolean isVisible() {
        return visible;
    }

    /**
     * {@inheritDoc}
     */
    public void setWorldVisible(boolean worldVisible) {
        if (this.worldVisible == worldVisible) {
            return;
        }
        this.worldVisible = worldVisible;

        notifyListeners((worldVisible == true) ? ComponentEventType.APPEARED_WORLD
                : ComponentEventType.DISAPPEARED_WORLD);
    }

    /**
     * {@inheritDoc}
     */
    public boolean isWorldVisible() {
        return worldVisible;
    }

    /**
     * {@inheritDoc}
     */
    public void setEnabled(boolean enabled) {
        if (this.enabled == enabled) {
            return;
        }
        this.enabled = enabled;

        notifyListeners((enabled == true) ? ComponentEventType.ENABLED
                : ComponentEventType.DISABLED);
    }

    /**
     * {@inheritDoc}
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Sets the display mode, either in-world or on-HUD
     * @param mode the new mode
     */
    public void setDisplayMode(DisplayMode mode) {
        this.mode = mode;

        notifyListeners(ComponentEventType.CHANGED_MODE);
    }

    /**
     * Gets the display mode
     * @return the display mode: in-world or on-HUD
     */
    public DisplayMode getDisplayMode() {
        return mode;
    }

    /**
     * {@inheritDoc}
     */
    public void addComponentListener(HUDComponentListener listener) {
        listeners.add(listener);
    // TODO: notify the new listener that the component was created?
    }

    /**
     * {@inheritDoc}
     */
    public void removeComponentListener(HUDComponentListener listener) {
        listeners.remove(listener);
    }

    /**
     * {@inheritDoc}
     */
    public List<HUDComponentListener> getComponentListeners() {
        return listeners;
    }

    /**
     * {@inheritDoc}
     */
    public void notifyListeners(HUDComponentEvent event) {
        List<HUDComponentListener> notifiees = getComponentListeners();
        if (notifiees != null) {
            Iterator<HUDComponentListener> iter = notifiees.iterator();
            while (iter.hasNext()) {
                HUDComponentListener notifiee = iter.next();
                notifiee.HUDComponentChanged(event);
            }
        }
    }

    /**
     * Convenience methods for notifying listeners
     * @param eventType the type of the notification event
     */
    private void notifyListeners(ComponentEventType eventType) {
        event.setComponent(this);
        event.setEventType(eventType);
        event.setEventTime(new Date());
        notifyListeners(event);
    }

    @Override
    public String toString() {
        return "HUDComponent2D: " + component.getClass().getName() +
                ", bounds: " + bounds +
                ", visible: " + visible +
                ", world visible: " + worldVisible +
                ", enabled: " + enabled;
    }
}
