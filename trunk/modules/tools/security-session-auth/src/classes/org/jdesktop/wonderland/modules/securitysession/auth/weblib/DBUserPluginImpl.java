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
package org.jdesktop.wonderland.modules.securitysession.auth.weblib;

import java.util.Properties;
import javax.naming.directory.BasicAttribute;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import org.jdesktop.wonderland.modules.securitysession.auth.weblib.AuthSessionManagerImpl.TokenGenerator;
import org.jdesktop.wonderland.modules.securitysession.auth.weblib.db.UserDAO;
import org.jdesktop.wonderland.modules.securitysession.auth.weblib.db.UserEntity;
import org.jdesktop.wonderland.modules.securitysession.weblib.UserRecord;

/**
 * Database implementation of a user plugin
 * @author jkaplan
 */
public class DBUserPluginImpl implements UserPlugin {
    private static final String PERSISTENCE_UNIT_NAME_PROP =
            DBUserPluginImpl.class.getSimpleName() + ".PersistenceUnitName";
    private static final String PERSISTENCE_UNIT_NAME_DEFAULT =
            "WonderlandUserPU";

    private EntityManagerFactory emf;

    public void configure(Properties props) {
        String puName = props.getProperty(PERSISTENCE_UNIT_NAME_PROP,
                                          PERSISTENCE_UNIT_NAME_DEFAULT);

        emf = Persistence.createEntityManagerFactory(puName);

        if (emf == null) {
            throw new IllegalStateException("Failed to initialize " +
                    "EntityManagerFactory for WonderlandGroupPU");
        }
    }

    public PasswordResult passwordMatches(String userId, char[] password) {
        // construct an object to query the users database
        UserDAO users = new UserDAO(emf);

        if (users.passwordMatches(userId, String.valueOf(password))) {
            // username and password match
            return PasswordResult.MATCH;
        } else if (users.getUser(userId) != null) {
            // user exists, but password doesn't match
            return PasswordResult.NO_MATCH;
        }

        // if we got here, we don't know about the user
        return PasswordResult.UNKNOWN_USER;
    }

    public UserRecord getUserRecord(String userId, TokenGenerator generator) {
        // construct an object to query the users database
        UserDAO users = new UserDAO(emf);

        // get a record for this user in the data store
        UserEntity ue = users.getUser(userId);
        if (ue == null) {
            return null;
        }

        // create the userrecord
        UserRecord rec = new UserRecord(userId, generator.generateToken(userId));

        // set values in the record
        rec.getAttributes().put(new BasicAttribute("uid", userId));
        rec.getAttributes().put(new BasicAttribute("cn", ue.getFullname()));
        rec.getAttributes().put(new BasicAttribute("mail", ue.getEmail()));

        return rec;
    }

}
