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

import com.jme.math.Vector3f;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.List;
import org.jdesktop.wonderland.client.hud.CompassLayout.Layout;

/**
 * A HUDComponent is an abstraction for an underlying visual element (2D or 3D)
 * that can be displayed on the HUD. 
 * 
 * A HUDComponent has a 2D position, and width and height. It can be visible
 * or invisible. It can also be enabled, in which case it responds to mouse
 * and keyboard events, or disabled.
 *
 * @author nsimpson
 */
public interface HUDComponent {

    /**
     * Defines a type of visual component that can be displayed on a HUD
     */
    public enum DisplayMode {

        /**
         * Display in world
         */
        WORLD,
        /**
         * Display on screen
         */
        HUD
    };

    /**
     * Sets the position and size of the component
     * @param bounds a rectangle which defines the position and size of the
     * component
     */
    public void setBounds(Rectangle bounds);

    /**
     * Sets the position and size of the component
     * @param x the new x-coordinate of this component
     * @param y the new y-coordinate of this component
     * @param width the new width of this component
     * @param height the new height of this component
     */
    public void setBounds(int x, int y, int width, int height);

    /**
     * Moves the component to a new location
     * @param x the new x-coordinate of this component
     * @param y the new y-coordinate of this component
     */
    public void setLocation(int x, int y);

    /**
     * Moves the component to a new location
     * @param x the new x-coordinate of this component
     * @param y the new y-coordinate of this component
     * @param notify whether to notify component listeners
     */
    public void setLocation(int x, int y, boolean notify);

    /**
     * Moves the component to a new location
     * @param p the new position of the component
     */
    public void setLocation(Point p);

    /**
     * Sets the preferred location as a compass point
     * @param compassPoint the compass point location
     */
    public void setPreferredLocation(Layout compassPoint);

    /**
     * Gets the preferred compass point location
     * @return the preferred location as a compass point
     */
    public Layout getPreferredLocation();

    /**
     * Gets the location of this component in the form of a point specifying
     * the component's origin
     * @return a Point representing the origin of the component
     */
    public Point getLocation();

    /**
     * Sets the in-world location of the component
     * @param location the 3D location of the component in-world
     */
    public void setWorldLocation(Vector3f location);

    /**
     * Gets the in-world location of the component
     * @return the 3D location of the component in-world
     */
    public Vector3f getWorldLocation();

    /**
     * Sets the x-coordinate of the component's origin
     * @param x the x-coordinate of the component's origin
     */
    public void setX(int x);

    /**
     * Gets the x-coordinate of the component's origin
     * @return the x-coordinate of the component's origin
     */
    public int getX();

    /**
     * Sets the y-coordinate of the component's origin
     * @param y the y-coordinate of the component's origin
     */
    public void setY(int y);

    /**
     * Gets the y-coordinate of the component's origin
     * @return the y-coordinate of the component's origin
     */
    public int getY();

    /**
     * Sets the component's width in pixels
     * @param width the width of the component
     */
    public void setWidth(int width);

    /**
     * Gets the component's width in pixels
     * @return the width of the component
     */
    public int getWidth();

    /**
     * Sets the component's height in pixels
     * @param height the height of the component
     */
    public void setHeight(int height);

    /**
     * Gets the component's height in pixels
     * @return the height of the component
     */
    public int getHeight();

    /**
     * Sets the width and height of the component
     * @param width the new width in pixels
     * @param height the new height in pixels
     */
    public void setSize(int width, int height);

    /**
     * Sets the width and height of the component
     * @param dimension the new width and height of the component
     */
    public void setSize(Dimension dimension);

    /**
     * Gets the size of the component
     * @return the component's size
     */
    public Dimension getSize();

    /**
     * Sets the visibility of the component
     * @param visible if true, shows the component, otherwise hides the
     * component
     */
    public void setVisible(boolean visible);

    /**
     * Gets whether the component is visible on the HUD
     * @return true if the component should be visible, false otherwise
     */
    public boolean isVisible();

    /**
     * Sets whether the component is visible in world
     * @param visible if true, shows the component in world at the specified
     * 3D location, otherwise hides the component
     */
    public void setWorldVisible(boolean visible);

    /**
     * Gets whether the component is visible in world
     * @return true if the component is visible in world, false otherwise
     */
    public boolean isWorldVisible();

    /**
     * Sets whether the component is responsive to mouse and keyboard events
     * @param enabled true if the component is to be enabled, false otherwise
     */
    public void setEnabled(boolean enabled);

    /**
     * Sets whether this component should be decorated
     * @param decoratable true if the component should be decorated (default),
     * false otherwise
     */
    public void setDecoratable(boolean decoratable);

    /**
     * Gets whether the component should be decorated
     * @return true if the component should be decorated (default), false
     * otherwise
     */
    public boolean getDecoratable();

    /**
     * Gets whether the component is enabled
     * @return true if the component is enabled, false otherwise
     */
    public boolean isEnabled();

    /**
     * Add a listener for events on this component
     * @param listener the listener to add
     */
    public void addComponentListener(HUDComponentListener listener);

    /**
     * Remove a listener for events on this component
     * @param listener the listener to remove
     */
    public void removeComponentListener(HUDComponentListener listener);

    /**
     * Gets the list of listeners for this HUD component
     * @return an array of HUD component listeners
     */
    public List<HUDComponentListener> getComponentListeners();
}
