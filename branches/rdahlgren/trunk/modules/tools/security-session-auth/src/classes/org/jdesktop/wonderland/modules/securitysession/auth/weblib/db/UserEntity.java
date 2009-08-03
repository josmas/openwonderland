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
package org.jdesktop.wonderland.modules.securitysession.auth.weblib.db;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Transient;

/**
 *
 * @author jkaplan
 */
@Entity
@NamedQueries({
    @NamedQuery(name="userCount",
                query="SELECT count(u) FROM UserEntity u"),
    @NamedQuery(name="allUsers",
                query="SELECT u FROM UserEntity u"),
    @NamedQuery(name="findUsersById",
                query="SELECT u FROM UserEntity u WHERE u.id like :id"),
    @NamedQuery(name="findUsersByName",
                query="SELECT u FROM UserEntity u WHERE u.fullname like :fullname"),
    @NamedQuery(name="findUsersByEmail",
                query="SELECT u FROM UserEntity u WHERE u.email like :email")
})
public class UserEntity {
    private String id;
    private String fullname;
    private String email;
    private String password;
    private String passwordHash;

    public UserEntity() {
    }

    public UserEntity(String id) {
        this.id = id;
    }

    @Id
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * The password will automatically be replaced by a password hash by
     * the UserDAO.  When creating or updating a user, any non-null password
     * will be turned into a hash.
     * @return the password for this user
     */
    @Transient
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }
}
