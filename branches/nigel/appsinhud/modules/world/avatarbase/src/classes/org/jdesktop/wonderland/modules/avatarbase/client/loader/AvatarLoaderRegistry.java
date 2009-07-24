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
package org.jdesktop.wonderland.modules.avatarbase.client.loader;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.jdesktop.wonderland.client.login.LoginManager;
import org.jdesktop.wonderland.client.login.PrimaryServerListener;
import org.jdesktop.wonderland.client.login.ServerSessionManager;
import org.jdesktop.wonderland.common.utils.ScannedClassLoader;
import org.jdesktop.wonderland.modules.avatarbase.client.loader.annotation.AvatarLoaderFactory;
import org.jdesktop.wonderland.modules.avatarbase.client.loader.spi.AvatarLoaderFactorySPI;

/**
 * The avatar loader registry manages the set of loaders that knows how to load
 * avatars of different kinds in the system. Each loader factory implements the
 * AvatarLoaderFactorySPI and is annotated with AvatarLoaderFactory.
 *
 * @author Jordan Slott <jslott@dev.java.net>
 */
public class AvatarLoaderRegistry implements PrimaryServerListener {

    // A map of all avatar loader factories from the Class to the loader object
    private Map<String, AvatarLoaderFactorySPI> avatarLoaderMap = null;

    /** Default constructor */
    public AvatarLoaderRegistry() {
        avatarLoaderMap = new HashMap();

        // Listen for changes to the server. We will need to clear out the
        // avatar loaders whenever we connect to a new server.
        LoginManager.addPrimaryServerListener(this);
    }

    /**
     * Singleton to hold instance of AvatarRegistry. This holder class is loaded
     * on the first execution of AvatarRegistry.getAvatarRegistry().
     */
    private static class LoaderRegistryHolder {
        private final static AvatarLoaderRegistry registry = new AvatarLoaderRegistry();
    }

    /**
     * Returns a single instance of this class
     * <p>
     * @return Single instance of this class.
     */
    public static final AvatarLoaderRegistry getAvatarLoaderRegistry() {
        return LoaderRegistryHolder.registry;
    }

    /**
     * Returns the avatar loader factory given the class name, or null if none
     * exists.
     *
     * @param className The name of the avatar loader factory class
     * @return An avatar loader factory
     */
    public AvatarLoaderFactorySPI getAvatarLoaderFactory(String className) {
        return avatarLoaderMap.get(className);
    }
    
    /**
     * Notification that the primary server has changed. Update our maps/sets
     * accordingly.
     *
     * @param server the new primary server (may be null)
     */
    public void primaryServer(ServerSessionManager server) {
        // Clear out all of the existing loaders and fetch a new set
        avatarLoaderMap.clear();

        // Look for all factories and add them to the map
        if (server != null) {
            ScannedClassLoader cl = server.getClassloader();
            Iterator<AvatarLoaderFactorySPI> it =
                    cl.getAll(AvatarLoaderFactory.class, AvatarLoaderFactorySPI.class);
            while (it.hasNext() == true) {
                AvatarLoaderFactorySPI factory = it.next();
                avatarLoaderMap.put(factory.getClass().getName(), factory);
            }
        }
    }
}
