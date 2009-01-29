// TODO: obsolete?
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
package org.jdesktop.wonderland.modules.xremwin.common;

import org.jdesktop.wonderland.common.ExperimentalAPI;
import org.jdesktop.wonderland.modules.appbase.common.AppLaunchMethods;
import org.jdesktop.wonderland.modules.appbase.common.AppLaunchMethodsConventional;

/**
 * The Xremwin app type supports conventional apps. They may be launched
 * from both WFS and user clients. Coourrently, local and server execution
 * sites are supported.
 *
 * @author deronj
 */

@ExperimentalAPI
public class AppLaunchMethodsXrw extends AppLaunchMethodsConventional {

    /** 
     * Create a new instance of AppLaunchMethodsXrw.
     */
    public AppLaunchMethodsXrw () {
	addLauncher(AppLaunchMethods.Launcher.WORLD);
	addLauncher(AppLaunchMethods.Launcher.USER);

	addExecutionSite(AppLaunchMethodsConventional.ExecutionSite.SERVER);
	addExecutionSite(AppLaunchMethodsConventional.ExecutionSite.LOCAL);
	// TODO: addExecutionSite(AppLaunchMethods.ExecutionSite.LOCAL_SERVER);

	setExecutionCapability("Xremwin");
    }
}
