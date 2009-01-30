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
package org.jdesktop.wonderland.modules.appbase.common;

import java.util.HashSet;
import org.jdesktop.wonderland.common.ExperimentalAPI;

/**
 * When you are implementing an app module you must implement the <code>getLaunchMethods</code> methods
 * of the app's <code>AppTypeGLO</code> and <code>AppType</code> to tell the system the different ways apps of that 
 * type are allowed to be launched. <code>getLaunchMethods</code> returns an instance of this class. This 
 * class is used to specify the various ways an app is allowed to be launched. Since this 
 * class is abstract, the actual class you need to return from <code>getLaunchMethods</code> is a subclass,
 * such as <code>AppLaunchMethodsWonderland</code> or <code>AppLaunchMethodsConventional</code>.
 *
 * @author deronj
 */

@ExperimentalAPI
public abstract class AppLaunchMethods {

    /** 
     * Defines the style of app.
     * <br><br>
     * A <code>WONDERLAND</code> app is a 2D or 3D share-aware Java app which uses Wonderland sharing APIs.
     * <br><br>
     * A <code>CONVENTIONAL</code> app is a 2D app type which was written for a traditional 2D window system.
     */
    public static enum Style { WONDERLAND, CONVENTIONAL };
    
    /**
     * Defines who is allowed to launch apps of this type. 
     * <br><br>
     * <code>WORLD</code> means that app instances can be defined in WFS. Such WFS definitions will create an cell 
     * for the app in the 3D world.
     * <br><br>
     * <code>USER</code> means that user clients can launch the app. A GUI component will be created in every
     * user client which allows the user to launch the app.
     * <br><br>
     * An app type may support both Launcher types or just one.
     */
    public static enum Launcher { WORLD, USER };

    /** The supported launchers */
    protected HashSet<Launcher> launchers = new HashSet<Launcher>();

    /** Returns the style of the app */
    public abstract Style getStyle ();

    /** 
     * Specify that this type of launcher is supported.
     *
     * @param launcher The launcher that is supported.
     */
    public void addLauncher (Launcher launcher) {
	launchers.add(launcher);
    }

    /** 
     * Is the specified launcher type supported? 
     *
     * @param launcher The launcher to check for whether it is supported.
     */
    public boolean containsLauncher (Launcher launcher) {
	return launchers.contains(launcher);
    }
}
