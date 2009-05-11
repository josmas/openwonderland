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
package org.jdesktop.wonderland.modules.avatarbase.client;

import java.io.Serializable;

/**
 * Early version of the AvatarConfig settings, this should probably be formalized
 * into a DTD at some point.
 *
 * @author paulby
 */
class AvatarConfigSettings implements Serializable {

    private String defaultAvatarConfig;     // Name of the default avatar config

    /**
     * @return the defaultAvatarConfig
     */
    public String getDefaultAvatarConfig() {
        return defaultAvatarConfig;
    }

    /**
     * @param defaultAvatarConfig the defaultAvatarConfig to set
     */
    public void setDefaultAvatarConfig(String defaultAvatarConfig) {
        this.defaultAvatarConfig = defaultAvatarConfig;
    }

}
