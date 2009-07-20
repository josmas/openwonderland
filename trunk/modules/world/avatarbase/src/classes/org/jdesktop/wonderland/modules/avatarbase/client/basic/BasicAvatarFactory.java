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
package org.jdesktop.wonderland.modules.avatarbase.client.basic;

import org.jdesktop.wonderland.client.login.ServerSessionManager;
import org.jdesktop.wonderland.modules.avatarbase.client.registry.AvatarRegistry;
import org.jdesktop.wonderland.modules.avatarbase.client.registry.annotation.AvatarFactory;
import org.jdesktop.wonderland.modules.avatarbase.client.registry.spi.AvatarFactorySPI;

/**
 * Basic avatar factory generates the most basic (default) avatar.
 *
 * @author Jordan Slott <jslott@dev.java.net>
 */
@AvatarFactory
public class BasicAvatarFactory implements AvatarFactorySPI {

    /**
     * {@inheritDoc}
     */
    public void registerAvatars(ServerSessionManager session) {
        // Register the basic avatar as the default
        AvatarRegistry registry = AvatarRegistry.getAvatarRegistry();
        registry.registerAvatar(new BasicAvatar(), true);
    }

    /**
     * {@inheritDoc}
     */
    public void unregisterAvatars(ServerSessionManager session) {
        // Do nothing
    }
}
