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
package org.jdesktop.wonderland.modules.security.common;

import java.io.Serializable;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 * Stores a set of permissions
 * @author jkaplan
 */
@XmlRootElement(name="permissions-set")
public class PermissionsSet implements Serializable {
    private Set<Permissions> permissions      = new LinkedHashSet<Permissions>();
    private Set<Principal> owners             = new LinkedHashSet<Principal>();
    private Set<ActionDTO> allPermissions     = new LinkedHashSet<ActionDTO>();
    private Set<ActionDTO> defaultPermissions = new LinkedHashSet<ActionDTO>();

    public PermissionsSet() {
    }

    @XmlTransient
    public Set<Permissions> getPermissions() {
        return permissions;
    }

    @XmlTransient
    public Set<Principal> getOwners() {
        return owners;
    }

    @XmlTransient
    public Set<ActionDTO> getAllPermissions() {
        return allPermissions;
    }

    @XmlTransient
    public Set<ActionDTO> getDefaultPermissions() {
        return defaultPermissions;
    }

    @XmlElement
    public Permissions[] getPermissionsInternal() {
        return permissions.toArray(new Permissions[0]);
    }

    public void setPermissionsInternal(Permissions[] permissions) {
        this.permissions = new LinkedHashSet<Permissions>();
        this.permissions.addAll(Arrays.asList(permissions));
    }

    @XmlElement
    public Principal[] getOwnersInternal() {
        return owners.toArray(new Principal[0]);
    }

    public void setOwnersInternal(Principal[] owners) {
        this.owners = new LinkedHashSet<Principal>();
        this.owners.addAll(Arrays.asList(owners));
    }

    @XmlElement
    public ActionDTO[] getAllPermissionsInternal() {
        return allPermissions.toArray(new ActionDTO[0]);
    }

    public void setAllPermissionsInternal(ActionDTO[] defaultPermissions) {
        this.allPermissions = new LinkedHashSet<ActionDTO>();
        this.allPermissions.addAll(Arrays.asList(defaultPermissions));
    }

    @XmlElement
    public ActionDTO[] getDefaultPermissionsInternal() {
        return defaultPermissions.toArray(new ActionDTO[0]);
    }

    public void setDefaultPermissionsInternal(ActionDTO[] defaultPermissions) {
        this.defaultPermissions = new LinkedHashSet<ActionDTO>();
        this.defaultPermissions.addAll(Arrays.asList(defaultPermissions));
    }
}
