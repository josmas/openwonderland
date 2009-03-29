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
import com.jme.math.Vector3f;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.wonderland.client.hud.HUDComponent;
import org.jdesktop.wonderland.client.hud.HUDComponentEvent;
import org.jdesktop.wonderland.client.hud.HUDComponentEvent.ComponentEventType;
import org.jdesktop.wonderland.client.hud.HUDComponentListener;
import org.jdesktop.wonderland.modules.appbase.client.view.View2DEntity;

/**
 * A HUDComponent2D is a 2D object that can be displayed on the HUD.
 * It has a 2D position, and width and height.
 *
 * A HUDComponent2D can be visible or invisible. It can also be enabled,
 * in which case it responds to mouse and keyboard events, or disabled.
 *
 * @author nsimpson
 */
public class HUDComponent2D implements HUDComponent {

    private static final Logger logger = Logger.getLogger(HUDComponent2D.class.getName());
    protected boolean enabled = true;
    protected List<HUDComponentListener> listeners;
    protected View2DEntity view;
    protected HUDComponentEvent event;

    public HUDComponent2D() {
        listeners = Collections.synchronizedList(new ArrayList());
        event = new HUDComponentEvent(this);
    }

    public HUDComponent2D(View2DEntity view) {
        this();
        this.view = view;
        logger.log(Level.INFO, "view HUD size: " + view.getDisplayerLocalWidth() + "x" + view.getDisplayerLocalHeight() + " at " +
                view.getTranslationUser().x + ", " + view.getTranslationUser().y);
    }

    /**
     * {@inheritDoc}
     */
    public void setBounds(Rectangle bounds) {
        setX(bounds.x);
        setY(bounds.y);
        setWidth(bounds.width);
        setHeight(bounds.height);
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
        view.setLocationOrtho(new Vector2f((float) x, (float) y));
        notifyListeners(ComponentEventType.MOVED);
    }

    /**
     * {@inheritDoc}
     */
    public void setLocation(Point p) {
        view.setLocationOrtho(new Vector2f(p.x, p.y));
        notifyListeners(ComponentEventType.MOVED);
    }

    /**
     * {@inheritDoc}
     */
    public Point getLocation() {
        return new Point((int) view.getTranslationUser().x, (int) view.getTranslationUser().y);
    }

    /**
     * {@inheritDoc}
     */
    public void setX(int x) {
        Vector3f transl = view.getTranslationUser();
        view.setTranslationUser(new Vector3f((float) x, transl.y, 0f));
        notifyListeners(ComponentEventType.MOVED);
    }

    /**
     * {@inheritDoc}
     */
    public int getX() {
        return (int) view.getTranslationUser().x;
    }

    /**
     * {@inheritDoc}
     */
    public void setY(int y) {
        Vector3f transl = view.getTranslationUser();
        view.setTranslationUser(new Vector3f(transl.x, (float) y, 0f));
        notifyListeners(ComponentEventType.MOVED);
    }

    /**
     * {@inheritDoc}
     */
    public int getY() {
        return (int) view.getTranslationUser().y;
    }

    /**
     * {@inheritDoc}
     */
    public void setWidth(int width) {
        view.setSizeApp(new Dimension(width, getHeight()));
        notifyListeners(ComponentEventType.RESIZED);
    }

    /**
     * {@inheritDoc}
     */
    public int getWidth() {
        return (int) view.getSizeApp().getWidth();
    }

    /**
     * {@inheritDoc}
     */
    public void setHeight(int height) {
        view.setSizeApp(new Dimension(getWidth(), height));
        notifyListeners(ComponentEventType.RESIZED);
    }

    /**
     * {@inheritDoc}
     */
    public int getHeight() {
        return (int) view.getSizeApp().getHeight();
    }

    /**
     * {@inheritDoc}
     */
    public void setSize(int width, int height) {
        view.setSizeApp(new Dimension(width, height));
        notifyListeners(ComponentEventType.RESIZED);
    }

    /**
     * {@inheritDoc}
     */
    public void setSize(Dimension dimension) {
        view.setSizeApp(dimension);
        notifyListeners(ComponentEventType.RESIZED);
    }

    /**
     * {@inheritDoc}
     */
    public Dimension getSize() {
        Dimension size = new Dimension();

        if (view != null) {
            size = view.getSizeApp();
        }
        return size;
    }

    /**
     * {@inheritDoc}
     */
    public void setVisible(boolean visible) {
        view.setOrtho(visible);
        view.setVisibleUser(visible);
        view.update();
        notifyListeners((visible == true) ? ComponentEventType.APPEARED
                : ComponentEventType.DISAPPEARED);
    }

    /**
     * {@inheritDoc}
     */
    public boolean isVisible() {
        return view.isVisibleUser();
    }

    /**
     * {@inheritDoc}
     */
    public void setEnabled(boolean enabled) {
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
        event.setEventType(eventType);
        event.setEventTime(new Date());
        notifyListeners(event);
    }
}
