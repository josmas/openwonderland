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
package org.jdesktop.wonderland.modules.appbase.client;

import com.jme.math.Vector2f;
import java.util.logging.Logger;
import org.jdesktop.wonderland.common.ExperimentalAPI;

/**
 * An abstract 2D conventional application.
 *
 * @author deronj
 */
@ExperimentalAPI
public abstract class AppConventional extends App2D {

    private static final Logger logger = Logger.getLogger(AppConventional.class.getName());

    /** Should the first window made visible be moved to the best view position? (Master only) */
    private boolean initInBestView;
    
    /**
     * Create a new instance of AppConventional with a default name.
     *
     * @param controlArb The control arbiter to use. null means that all users can control at the same time.
     * @param pixelScale The size of the window pixels in world coordinates.
     */
    public AppConventional(ControlArb controlArb, Vector2f pixelScale) {
        super(controlArb, pixelScale);
    }

    /**
     * Create a new instance of AppConventional with the given name.
     *
     * @param name The name of the app.
     * @param controlArb The control arbiter to use. null means that all users can control at the same time.
     * @param pixelScale The size of the window pixels in world coordinates.
     */
    public AppConventional(String name, ControlArb controlArb, Vector2f pixelScale) {
        super(name, controlArb, pixelScale);
    }

    /**
     * Specify whether, when the app is made visible, it should be moved to approximately the best view 
     * based on the current user position.
     *
     * @param initInBestView Whether the app should be moved to approximately the best view.
     */
    public void setInitInBestView(boolean initInBestView) {
        this.initInBestView = initInBestView;
    }

    /**
     * Returns the initInBestView property.
     */
    public boolean getInitInBestView() {
        return initInBestView;
    }
}
