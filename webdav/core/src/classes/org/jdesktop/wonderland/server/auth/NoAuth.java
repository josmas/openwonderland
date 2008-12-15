/**
 * Project Wonderland
 *
 * Copyright (c) 2004-2008, Sun Microsystems, Inc., All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * $Revision$
 * $Date$
 * $State$
 */
package org.jdesktop.wonderland.server.auth;

import com.sun.sgs.auth.Identity;
import com.sun.sgs.auth.IdentityAuthenticator;
import com.sun.sgs.auth.IdentityCredentials;
import com.sun.sgs.impl.auth.NamePasswordCredentials;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;
import javax.security.auth.login.CredentialException;
import javax.security.auth.login.LoginException;
import org.jdesktop.wonderland.common.auth.WonderlandIdentity;

/**
 * An authenticator that reads all data from what the user passes in in
 * the username field.  No attempt is made to authenticate the user,
 * but the user's name is filled in based on the passed in data.
 * @author jkaplan
 */
public class NoAuth implements IdentityAuthenticator {
    // logger
    private static final Logger logger =
            Logger.getLogger(NoAuth.class.getName());
   
    
    public NoAuth(Properties prop) {
        logger.info("Loading NoAuth authenticator");
    }

    public String[] getSupportedCredentialTypes() {
        return new String[] { "NameAndPasswordCredentials" };
    }
    
    public Identity authenticateIdentity(IdentityCredentials credentials) 
        throws LoginException 
    {
        logger.warning("Auth: " + credentials);

        if (!(credentials instanceof NamePasswordCredentials)) {
            throw new CredentialException("Wrong credential class: " +
                    credentials.getClass().getName());
        }
        
        NamePasswordCredentials npc = (NamePasswordCredentials) credentials;
        
        // make sure the name is specified
        if (npc.getName() == null) {
            logger.warning("No name specified");
            throw new CredentialException("Invalid username");
        }

        logger.warning("Auth: " + npc.getName());

        // unpack the components of the id
        Map<String, String> unpacked = unpack(npc.getName());
        String username = unpacked.get("un");
        String fullname = unpacked.get("fn");
        String email =    unpacked.get("m");

        logger.warning("un: " + username + " fn: " + fullname + " m: " + email);

        // make sure at least a username was specified
        if (username == null || username.trim().length() == 0) {
            logger.warning("Unable to find username");
            throw new CredentialException("No username specified");
        }

        // construct a new WonderlandServerIdentity to return
        return new WonderlandServerIdentity
                (new WonderlandIdentity(username, fullname, email));
    }

    /**
     * Unpack a set of key=value pairs into a Map from key to value
     * @param packed the packed String
     * @return a Map from keys to values
     */
    protected Map<String, String> unpack(String packed) {
        Map<String, String> out = new HashMap<String, String>();

        for (String pair : packed.split(";")) {
            String[] vals = pair.split("=");
            if (vals.length == 2) {
                out.put(vals[0], vals[1]);
            }
        }

        return out;
    }
}
