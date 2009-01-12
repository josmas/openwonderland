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

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import org.jdesktop.wonderland.modules.appbase.common.AppLaunchMethods;
import org.jdesktop.wonderland.modules.appbase.common.AppLaunchMethodsDefault;
import org.jdesktop.wonderland.common.ExperimentalAPI;

/**
 * The client-side class which describes a unique type of Wonderland application. For example, a PDF 
 * viewer, a 3D math simulation app, or an X application. The AppType provides all of the information 
 * that a Wonderland client needs to know in order to load the app type's code and create new instances of its apps.
 *
 * @author deronj
 */

@ExperimentalAPI
public abstract class AppType {

    /** A list of all apps of this app type in the client session */
    protected LinkedList<App> apps = new LinkedList<App>();

    /** The default launch methods (returned by getLaunchMethods if this method is not overridden by the subclass */
    protected AppLaunchMethods defaultLaunchMethods;
    
    /**
     * Returns the name of the app type. The name must be unique within the server and group of connected clients.
     */
    public abstract String getName ();

    /**
     * Returns the factory object which creates user-visible objects for this type of app.
     */
    public abstract GuiFactory getGuiFactory ();

    /**
     * Used by an app to add itself app to this app type's list of apps.
     *
     * @param app The app to add to the list.
     */
    void appAdd (App app) {
	apps.add(app);
    }

    /**
     * Used by an app remove itself from this app type's list of apps.
     *
     * @param app The app to add to the list.
     */
    void appRemove (App app) {
	apps.remove(app);
    }

    /**
     * Returns an iterator over all app instances of this type.
     */
    public Iterator<App> getAppIterator () {
	return apps.iterator();
    }

    /**
     * Returns the supported launch methods. Subclasses should override to return
     * the launch methods it supports. The default launch methods contain a single
     * launch method which is World launch of a Wonderland app. Therefore if 
     * the subclass app type is a Conventional app it MUST override this method.
     */
    public AppLaunchMethods getLaunchMethods () {
	if (defaultLaunchMethods == null) {
	    defaultLaunchMethods = new AppLaunchMethodsDefault();
	}
	return defaultLaunchMethods;
    }
}
