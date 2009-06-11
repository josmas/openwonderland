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
import java.util.logging.Logger;
import org.jdesktop.wonderland.client.hud.HUDComponent;
import org.jdesktop.wonderland.client.hud.HUDLayoutManager;

/**
 * A HUDAbsoluteLayoutManager lays out 2D objects in a 2D rectangular space
 * according to their specified coordinates. The management of the position
 * of objects in the space is therefore the responsibility of another
 * mechanism. For example, a HUDManager might divide up the screen into
 * non-overlapping rectangular regions for multiple HUDs.
 * 
 * @author nsimpson
 */
public class HUDAbsoluteLayoutManager implements HUDLayoutManager {

    private static final Logger logger = Logger.getLogger(HUDAbsoluteLayoutManager.class.getName());

    private int hudWidth;
    private int hudHeight;

    public HUDAbsoluteLayoutManager(int hudWidth, int hudHeight) {
        this.hudWidth = hudWidth;
        this.hudHeight = hudHeight;
    }

    /**
     * Add a HUD component to the list of components this layout manager
     * is managing.
     * @param component the component to manage
     */
    public void manageComponent(HUDComponent component) {
    }

    /**
     * Remove a HUD component from the list of components this layout manager
     * is managing.
     * @param component the component to stop managing
     */
    public void unmanageComponent(HUDComponent component) {
    }

    /**
     * Get the position of the given component according to the specified
     * layout.
     * @param component the component for which the position is needed
     */
    public Point getLocation(HUDComponent component) {
        return component.getLocation();
    }
}
