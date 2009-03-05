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
 * A principal that can be added to the security component.
 * @author jkaplan
 */
@XmlRootElement(name="permissions")
public class Permissions implements Serializable {
    private Principal principal;
    private Set<ActionDTO> actions;

    public Permissions() {
        this (null, new LinkedHashSet<ActionDTO>());
    }

    public Permissions(Principal principal, Set<ActionDTO> actions) {
        this.principal = principal;
        this.actions = actions;
    }

    @XmlElement
    public Principal getPrincipal() {
        return principal;
    }

    public void setPrincipal(Principal principal) {
        this.principal = principal;
    }
    
    @XmlTransient
    public Set<ActionDTO> getActions() {
        return actions;
    }

    @XmlElement
    public ActionDTO[] getActionsInternal() {
        return actions.toArray(new ActionDTO[0]);
    }

    public void setActionsInternal(ActionDTO[] actions) {
        this.actions = new LinkedHashSet<ActionDTO>();
        this.actions.addAll(Arrays.asList(actions));
    }
}
