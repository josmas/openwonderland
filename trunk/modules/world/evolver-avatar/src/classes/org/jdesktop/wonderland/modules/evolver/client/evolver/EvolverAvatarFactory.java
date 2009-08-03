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
package org.jdesktop.wonderland.modules.evolver.client.evolver;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.wonderland.client.login.ServerSessionManager;
import org.jdesktop.wonderland.modules.avatarbase.client.registry.annotation.AvatarFactory;
import org.jdesktop.wonderland.modules.avatarbase.client.registry.spi.AvatarFactorySPI;

/**
 * Evolver avatar factory generates the Evolver avatars and performs the initial
 * synchronization with the server.
 *
 * @author Jordan Slott <jslott@dev.java.net>
 */
@AvatarFactory
public class EvolverAvatarFactory implements AvatarFactorySPI {

    private static Logger logger = Logger.getLogger(EvolverAvatarFactory.class.getName());

    /**
     * {@inheritDoc}
     */
    public void registerAvatars(ServerSessionManager session) {
        // Add the server to the manager for all IMI avatars. This will wait
        // until the initial synchronization has been completed.
        EvolverAvatarConfigManager m = EvolverAvatarConfigManager.getEvolverAvatarConfigManager();
        try {
            m.addServerAndSync(session);
        } catch (InterruptedException excp) {
            logger.log(Level.WARNING, "Synchronization with server was " +
                    "interrupted for " + session.getServerURL(), excp);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void unregisterAvatars(ServerSessionManager session) {
        // Remove the server from the manager for all IMI avatars.
        EvolverAvatarConfigManager m = EvolverAvatarConfigManager.getEvolverAvatarConfigManager();
        m.removeServer(session);
    }
}
