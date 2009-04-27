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
package org.jdesktop.wonderland.modules.appbase.client.cell;

import org.jdesktop.wonderland.common.annotation.Plugin;
import org.jdesktop.wonderland.client.ClientPlugin;
import org.jdesktop.wonderland.client.login.ServerSessionManager;
import org.jdesktop.wonderland.common.ExperimentalAPI;

/**
 * An object which is created during the user client login process in order to initialize the 
 * app base for that user client.
 */
@ExperimentalAPI
@Plugin
public class AppConventionalCellClientPlugin implements ClientPlugin {

    /** All client plugins must have a no-arg constructor. */
    public AppConventionalCellClientPlugin () {}

    /**
     * This is executed at the start up of all user clients. 
     * <br><br>
     * Note: it is *NOT* executed by the SAS provider client because this client is set up to 
     * ignore all client plugins.
     */
    public void initialize(ServerSessionManager loginInfo) {
        AppConventionalCell.initialize(loginInfo);
    }

    /**
     * Clean up this plugin
     */
    public void cleanup() {
        // nothing to clean up -- since AppConventionalCell is defined
        // in the plugin classloader for this plugin, all changes made
        // in initialize are only made in the context of this plugin
    }
}
