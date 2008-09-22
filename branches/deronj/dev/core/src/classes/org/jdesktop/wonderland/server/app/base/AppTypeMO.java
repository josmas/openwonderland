/**
 * Project Wonderland
 *
 * Copyright (c) 2004-2008, Sun Microsystems, Inc., All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * $Revision$
 * $Date$
 * $State$
 */
package org.jdesktop.wonderland.server.app.base;

import java.io.Serializable;
import org.jdesktop.wonderland.common.app.base.AppLaunchMethods;
import org.jdesktop.wonderland.common.app.base.AppLaunchMethodsDefault;
import org.jdesktop.wonderland.common.ExperimentalAPI;

/**
 * The server-side class which describes a unique type of Wonderland application. For example, a PDF 
 * viewer, a 3D math simulation app, or an X application. The AppType provides all of the information 
 * that the Wonderland server needs to know in order to load the app type's code and create new instances of its apps.
 *
 * @author deronj
 */

@ExperimentalAPI
public abstract class AppTypeMO implements Serializable {

    /** The default launch methods */
    protected static AppLaunchMethods defaultLaunchMethods;

    /**
     * Returns the name of the app type. The name must be unique within the server and group of connected clients.
     */
    public abstract String getName ();

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