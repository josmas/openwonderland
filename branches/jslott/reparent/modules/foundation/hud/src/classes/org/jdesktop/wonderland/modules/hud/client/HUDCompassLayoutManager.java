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
import org.jdesktop.wonderland.client.hud.CompassLayout.Layout;
import org.jdesktop.wonderland.client.hud.HUDComponent;
import org.jdesktop.wonderland.client.hud.HUDLayoutManager;

/**
 *
 * @author nsimpson
 */
public class HUDCompassLayoutManager implements HUDLayoutManager {

    private static final Logger logger = Logger.getLogger(HUDAbsoluteLayoutManager.class.getName());
    private int hudWidth;
    private int hudHeight;

    public HUDCompassLayoutManager(int hudWidth, int hudHeight) {
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
        Point location = new Point();

        if (component.getPreferredLocation() != Layout.NONE) {
            int centerX = hudWidth / 2;
            int centerY = hudHeight / 2;
            int compCenterX = component.getWidth() / 2;
            int compCenterY = component.getHeight() / 2;

            switch (component.getPreferredLocation()) {
                case NORTH:
                    location.setLocation(centerX, hudHeight - compCenterY - 20);
                    break;
                case SOUTH:
                    location.setLocation(centerX, 20 + compCenterY);
                    break;
                case WEST:
                    location.setLocation(20 + compCenterX, centerY);
                    break;
                case EAST:
                    location.setLocation(hudWidth - 20 - compCenterX, centerY);
                    break;
                case CENTER:
                    location.setLocation(centerX, centerY);
                    break;
                case NORTHWEST:
                    location.setLocation(20 + compCenterX, hudHeight - compCenterY - 20);
                    break;
                case NORTHEAST:
                    location.setLocation(hudWidth - 20 - compCenterX, hudHeight - compCenterY - 20);
                    break;
                case SOUTHWEST:
                    location.setLocation(20 + compCenterX, 20 + compCenterY);
                    break;
                case SOUTHEAST:
                    location.setLocation(hudWidth - 20 - compCenterX, 20 + compCenterY);
                    break;
                default:
                    logger.warning("unhandled layout type: " + component.getPreferredLocation());
                    break;
            }
        } else {
            location = component.getLocation();
        }
        return location;
    }
}
