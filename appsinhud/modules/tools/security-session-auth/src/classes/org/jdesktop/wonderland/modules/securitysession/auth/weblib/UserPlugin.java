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
import org.jdesktop.wonderland.modules.securitysession.auth.weblib.AuthSessionManagerImpl.TokenGenerator;
import org.jdesktop.wonderland.modules.securitysession.weblib.UserRecord;

/**
 * An extension to the web server which provides user information from
 * a data source such as LDAP.  The AuthSessionManager will use configured
 * plugins to authenticate the user in Wonderland.
 * @author jkaplan
 */
public interface UserPlugin {
    /** The result from doing a password lookup */
    public enum PasswordResult { MATCH, NO_MATCH, UNKNOWN_USER };

    /**
     * Configure this plugin with the given properties.  Guaranteed to
     * be called before any other methods.
     * @param properties the properties to configure this plugin with.
     */
    public void configure(Properties props);

    /**
     * Determine if the given username and password are valid.
     * @param userId the id of the user to verify
     * @param password the password to verify for the user
     * @return MATCH if the userId and password match, NO_MATCH if they
     * don't match, or UNKNOWN_USER to skip to the next registered user
     * plugin
     */
    public PasswordResult passwordMatches(String userId, char[] password);

    /**
     * Get user information for the user with the given userId.  Typically
     * this will be called after passwordMatches, but this is not
     * guaranteed.  The method can return null if the user cannot be
     * found.
     * @param userId the user to get information for
     * @param generator a token generator to use for generating new
     * tokens for the user record
     * @return a user record for the given user
     */
    public UserRecord getUserRecord(String userId, TokenGenerator generator);
}
