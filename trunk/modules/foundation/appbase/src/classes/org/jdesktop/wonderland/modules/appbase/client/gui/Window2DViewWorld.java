/**
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
package org.jdesktop.wonderland.modules.appbase.client.gui;

import com.jme.math.Vector3f;
import java.awt.Point;
import org.jdesktop.mtgame.EntityComponent;
import org.jdesktop.wonderland.client.input.EventListener;

/**
 * An interface used by view world objects provided by the gui factory.
 *
 * @author deronj
 */
public interface Window2DViewWorld {

    /**
     * Returns the depth offset above a base window that popup windows should be positioned.
     */
    public float getPopupDepthOffset();

    /**
     * Sets the translation of the view. Don't forget to also call update(CHANGED_TRANSFORM) afterward.
     *
     * @param translation The new translation of the window.
     */
    public void setTranslation(Vector3f translation);

    /**
     * Returns the translation of the view.
     */
    public Vector3f getTranslation();

    /**
     * Sets the dimensions of the position of the view. Don't forget to 
     * call update(CHANGED_SIZE) afterward.
     *
     * @param width The new width of the window.
     * @param height The new height of the window.
     */
    public void setSize(float width, float height);

    /**
     * Returns the width of the window.
     */
    public float getWidth();

    /**
     * Returns the height of the window.
     */
    public float getHeight();

    /**
     * Specify whether the view is top-level. Don't forget to also
     * call update(CHANGED_TOP_LEVEL) afterward.  
     *
     * @param topLevel True if the window should be a topLevel window.
     */
    public void setTopLevel(boolean topLevel);

    /**
     * Returns whether the window of the view is top-level.
     */
    public boolean getTopLevel();

    /**
     * Sets the visibility of the view (independent of the window).
     * Don't forget to call update(CHANGED_VISIBILITY) afterward.
     *
     * @param visible True if the window should be made visible.
     */
    public void setVisible(boolean visible);

    /**
     * Returns the visibility of the view (independent of the window)
     */
    public boolean getVisible();

    /**
     * Transform the given 3D point in local coordinates into the corresponding point
     * in the pixel space of the window image. The given point must be in the plane of the window.
     * @param point The point to transform.
     * @param clamp If true return the last position if the argument point is null or the resulting
     * position is outside of the geometry's rectangle. Otherwise, return null if these conditions hold.
     * @return the 2D position of the pixel space the window's image.
     */
    public Point calcPositionInPixelCoordinates(Vector3f point, boolean clamp);

    /**
     * Given a point in the pixel space of the Wonderland canvas calculates 
     * the texel coordinates of the point on the geometry  where a
     * ray starting from the current eye position intersects the geometry.
     */
    public Point calcIntersectionPixelOfEyeRay(int x, int y);

    /**
     * Add an event listener to this view.
     * @param listener The listener to add.
     */
    public void addEventListener(EventListener listener);

    /**
     * Remove an event listener from this view.
     * @param listener The listener to remove.
     */
    public void removeEventListener(EventListener listener);

    /**
     * Does this view have the given listener attached to it?
     * @param listener The listener to check.
     */
    public boolean hasEventListener(EventListener listener);

    /**
     * Add the given entity component to the view's entity.
     */
    public void addEntityComponent(Class clazz, EntityComponent comp);

    /**
     * Remove the given entity component from the view's entity.
     */
    public void removeEntityComponent(Class clazz);

    /**
     * Returns the given component of the view's entity.
     */
    public EntityComponent getEntityComponent(Class clazz);
}
