/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.modules.security.weblib;

import java.security.Principal;

/**
 *
 * @author jkaplan
 */
public class UserGroupPrincipal implements Principal {
    private String name;
    private String[] groups;

    public UserGroupPrincipal(String name, String[] groups) {
        this.name = name;
        this.groups = groups;
    }

    public String getName() {
        return name;
    }

    public String[] getGroups() {
        return groups;
    }

    public boolean isMemberOfGroup(String groupName) {
        for (String g : groups) {
            if (g.equals(groupName)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final UserGroupPrincipal other = (UserGroupPrincipal) obj;
        if ((this.name == null) ? (other.name != null) :
            !this.name.equals(other.name))
        {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 29 * hash + (this.name != null ? this.name.hashCode() : 0);
        return hash;
    }
}
