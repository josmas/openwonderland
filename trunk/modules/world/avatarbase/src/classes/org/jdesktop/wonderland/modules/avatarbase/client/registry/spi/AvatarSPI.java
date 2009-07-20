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
package org.jdesktop.wonderland.modules.avatarbase.client.registry.spi;

import java.net.URL;
import org.jdesktop.wonderland.client.login.ServerSessionManager;
import org.jdesktop.wonderland.common.ExperimentalAPI;

/**
 * The AvatarSPI interface represents a type of avatar that is registered in
 * the system and can (optionally) be configured.
 * 
 * @author Jordan Slott <jslott@dev.java.net>
 */
@ExperimentalAPI
public interface AvatarSPI {

    /**
     * Return the unique name of the avatar.
     *
     * @return The unique name of the avatar
     */
    public String getName();

    /**
     * Returns whether the avatar can be delete by the user.
     *
     * @return True if the avatar configuration can be deleted
     */
    public boolean canDelete();

    /**
     * Deletes the avatar configuration. If deletion is not permitted, this
     * method does nothing.
     */
    public void delete();

    /**
     * Returns whether the avatar can be configured by the user.
     *
     * @return True if the avatar can be configured by the user
     */
    public boolean canConfigure();

    /**
     * Configures the avatar. If configuration is not permitted, this method
     * does nothing.
     */
    public void configure();

    /**
     * Return the URL that representings the system-wide configuration XML
     * file that any client can use to render the avatar.
     *
     * @param session The current primary session
     * @return A URL
     */
    public URL getAvatarURL(ServerSessionManager session);
}
