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

import java.awt.Point;
import java.awt.Rectangle;
import java.util.Iterator;
import javax.swing.JComponent;
import org.jdesktop.wonderland.client.cell.Cell;

/**
 * A HUD is a 2D region of the Wonderland client window on which HUD components
 * can be displayed.
 * 
 * A client may have multiple HUDs. For example, a Status HUD which displays
 * status information about the user's session, and an Audio HUD for audio
 * controls.
 * 
 * A HUD contains HUD xomponents which are visual objects such as a 2D control
 * panel or a representation of a 3D object. HUD components are laid out within
 * a HUD by a HUDLayoutManager.
 *
 *
 * @author nsimpson
 */
public interface HUD {

    /**
     * Creates a new HUD component
     * @param component a Swing component to display in this HUD component
     */
    public HUDComponent createComponent(JComponent component);

    /**
     * Creates a new HUD component with a Cell association
     * @param component a Swing component to display in this HUD component
     * @param cell the cell associated with this HUD component
     */
    public HUDComponent createComponent(JComponent component, Cell cell);

    public HUDComponent createMessage(String message);

    /**
     * Adds a HUD component to the HUD
     * @param component the component to add
     */
    public void addComponent(HUDComponent component);

    /**
     * Removes a component from the HUD
     * @param component the component to remove
     */
    public void removeComponent(HUDComponent component);

    /**
     * Gets an iterator that will iterate over the HUD's components
     * @return an iterator for HUD components
     */
    public Iterator<HUDComponent> getComponents();

    /**
     * Gets whether this HUD has one or more HUD components
     * @return true if the HUD has HUD components, false otherwise
     */
    public boolean hasComponents();

    /**
     * Sets the manager of HUD components within this HUD
     * @param manager the manager of HUD components
     */
    public void setComponentManager(HUDComponentManager manager);

    /**
     * Gets the component manager associated with this HUD
     * @return the component manager
     */
    public HUDComponentManager getComponentManager();

    /**
     * Assigns a name to this HUD
     * @param name the name to assign to this HUD
     */
    public void setName(String name);

    /**
     * Gets the name assigned to this HUD
     * @return the name of the HUD
     */
    public String getName();

    /**
     * Sets the width of the HUD in pixels
     * @param width the width of the HUD (pixels)
     */
    public void setWidth(int width);

    /**
     * Gets the width of the HUD in pixels
     * @return the width of the HUD in pixels
     */
    public int getWidth();

    /**
     * Sets the height of the HUD in pixels
     * @param height the height of the HUD (pixels)
     */
    public void setHeight(int height);

    /**
     * Gets the height of the HUD in pixels
     * @return the height of the HUD in pixels
     */
    public int getHeight();

    /**
     * Sets the bounds (x, y position, width, height) of the HUD
     * @param bounds the bounds of the HUD
     */
    public void setBounds(Rectangle bounds);

    /**
     * Gets the bounds of the HUD
     * @return the bounds of the HUD
     */
    public Rectangle getBounds();

    /**
     * Moves the HUD to a new location
     * @param p the new position of the HUD
     */
    public void setLocation(Point p);

    /**
     * Gets the location of this HUD in the form of a point specifying
     * the component's origin
     * @return a Point representing the origin of the HUD
     */
    public Point getLocation();

    /**
     * Make the HUD visible
     */
    public void show();

    /**
     * Make the HUD invisible
     */
    public void hide();

    /**
     * Gets whether the HUD is visible
     * @return true if the HUD is visible, false otherwise
     */
    public boolean isShowing();

    /**
     * Sets the transparency of the HUD on a scale of 0.0f to 1.0f, where
     * 0.0f means that the HUD is completely opaque, and 1.0f means the HUD
     * is completely transparent.
     * Note that transparency is independent of whether the HUD is visible or not
     * @param transparency the transparency of the HUD
     */
    public void setTransparency(float transparency);

    /**
     * Gets the transparency of the HUD
     * @return the transparency of the HUD on the scale of 0.0f to 1.0f
     */
    public float getTransparency();
}
