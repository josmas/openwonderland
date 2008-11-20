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
package org.jdesktop.wonderland.modules.swingmenutest.common;

import org.jdesktop.wonderland.common.ExperimentalAPI;
import org.jdesktop.wonderland.modules.appbase.common.AppLaunchMethods;
import org.jdesktop.wonderland.modules.appbase.common.AppLaunchMethodsWonderland;

/**
 * The Swing menu test is a Wonderland app. It currently is only launched from the world. 
 *
 * @author deronj
 */

@ExperimentalAPI
public class SwingMenuTestLaunchMethods extends AppLaunchMethodsWonderland {

    /** Create an instance of SwingMenuTestAppLaunchMethods */
    public SwingMenuTestLaunchMethods () {
	addLauncher(AppLaunchMethods.Launcher.WORLD);
    }
}
