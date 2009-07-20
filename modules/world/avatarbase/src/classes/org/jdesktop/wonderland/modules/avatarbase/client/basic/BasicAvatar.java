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

import java.net.URL;
import org.jdesktop.wonderland.client.login.ServerSessionManager;
import org.jdesktop.wonderland.modules.avatarbase.client.jme.cellrenderer.WlAvatarCharacter;
import org.jdesktop.wonderland.modules.avatarbase.client.registry.spi.AvatarSPI;

/**
 * The most basic avatar available in the system, a simple COLLADA model that
 * cannot be deleted/configured and serves as the "default" avatar.
 *
 * @author Jordan Slott <jslott@dev.java.net>
 */
public class BasicAvatar implements AvatarSPI {

    /** Default constructor */
    public BasicAvatar() {
    }

    /**
     * {@inheritDoc}
     */
    public String getName() {
        return "Default";
    }

    /**
     * {@inheritDoc}
     */
    public boolean canDelete() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public void delete() {
        // Do nothing
    }

    /**
     * {@inheritDoc}
     */
    public boolean canConfigure() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public void configure() {
        // Do nothing
    }

    /**
     * {@inheritDoc}
     */
    public WlAvatarCharacter getAvatarCharacter() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * {@inheritDoc}
     */
    public URL getAvatarURL(ServerSessionManager session) {
        return null;
    }
}
