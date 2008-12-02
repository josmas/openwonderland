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
package org.jdesktop.wonderland.client.login;

import java.net.URL;
import org.jdesktop.wonderland.client.ClientPlugin;

/**
 * Interface for selecting what plugins get loaded by a ServerSessionManager.
 * @author jkaplan
 */
public interface PluginFilter {
    /**
     * Return true to load the given plugin jar file, or false not to.  This
     * prevents unwanted jar files from being downloaded.
     * @param sessionManager the sessionManager that wants to load plugins
     * @param url the jar URL to download
     * @return true to download the jar, or false not to
     */
    public boolean shouldDownload(ServerSessionManager sessionManager,
                                  URL jarURL);

    /**
     * Return true to intiialize the given plugin, or false not to.
     * @param sessionManager the session manager that wants to load plugins
     * @param plugin the plugin to load
     * @return true to initialize the plugin, or false not to
     */
    public boolean shouldInitialize(ServerSessionManager sessionManager,
                                    ClientPlugin plugin);

    /**
     * Filter that always loads all plugins
     */
    public class DefaultPluginFilter implements PluginFilter {
        public boolean shouldDownload(ServerSessionManager sessionManager,
                                      URL jarURL)
        {
            return true;
        }

        public boolean shouldInitialize(ServerSessionManager sessionManager,
                                        ClientPlugin plugin)
        {
            return true;
        }
    }

    /**
     * Filter that never loads plugins
     */
    public class NoPluginFilter implements PluginFilter {
        public boolean shouldDownload(ServerSessionManager sessionManager,
                                      URL jarURL)
        {
            return false;
        }

        public boolean shouldInitialize(ServerSessionManager sessionManager,
                                        ClientPlugin plugin)
        {
            return false;
        }
    }
}
