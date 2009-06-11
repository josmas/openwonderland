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

/**
 * A HUDLayoutManager lays out 2D objects in a 2D rectangular space.
 *
 * A HUDLayoutManager could be used by a HUDManager to layout HUDs on the
 * screen or by a HUDComponentManager to layout the HUDComponents it
 * manages.
 * 
 * @author nsimpson
 */
public interface HUDLayoutManager {

    /**
     * Add a HUD component to the list of components this layout manager
     * is managing.
     * @param component the component to manage
     */
    public void manageComponent(HUDComponent component);

    /**
     * Remove a HUD component from the list of components this layout manager
     * is managing.
     * @param component the component to stop managing
     */
    public void unmanageComponent(HUDComponent component);

    /**
     * Get the position of the given component according to the specified
     * layout.
     * @param component the component for which the position is needed
     */
    public Point getLocation(HUDComponent component);
}
