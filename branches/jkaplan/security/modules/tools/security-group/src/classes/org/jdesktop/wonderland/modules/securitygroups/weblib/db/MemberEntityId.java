/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jdesktop.wonderland.modules.securitygroups.weblib.db;

import java.io.Serializable;

/**
 *
 * @author jkaplan
 */
public class MemberEntityId implements Serializable {
    private String groupId;
    private String memberId;

    public MemberEntityId() {
    }

    public MemberEntityId(String groupId, String memberId) {
        this.groupId = groupId;
        this.memberId = memberId;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getMemberId() {
        return memberId;
    }

    public void setMemberId(String memberId) {
        this.memberId = memberId;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final MemberEntityId other = (MemberEntityId) obj;
        if ((this.groupId == null) ? (other.groupId != null) : !this.groupId.equals(other.groupId)) {
            return false;
        }
        if ((this.memberId == null) ? (other.memberId != null) : !this.memberId.equals(other.memberId)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 61 * hash + (this.groupId != null ? this.groupId.hashCode() : 0);
        hash = 61 * hash + (this.memberId != null ? this.memberId.hashCode() : 0);
        return hash;
    }
}
