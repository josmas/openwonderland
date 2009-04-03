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
package org.jdesktop.wonderland.modules.securitysession.noauth.weblib;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.LinkedHashMap;
import java.util.Map;
import org.apache.catalina.util.Base64;

/**
 *
 * @author jkaplan
 */
public class InternalSessionManagerImpl implements SessionManager {
    private final Map<String, UserRecord> byUserId = 
            new LinkedHashMap<String, UserRecord>();
    private final Map<String, UserRecord> byToken =
            new LinkedHashMap<String, UserRecord>();
    
    public UserRecord get(String userId) {
        return get(userId, false);
    }

    public synchronized UserRecord get(String userId, boolean create) {
        // get the existing value for this user
        UserRecord out = byUserId.get(userId);
        
        // if create is true and the record was not found, create a new
        // record
        if (create && out == null) {
            out = new UserRecord(userId, newToken(userId));

            // add to both maps
            byUserId.put(userId, out);
            byToken.put(out.getToken(), out);
        }

        return out;
    }

    public synchronized UserRecord getByToken(String token) {
        return byToken.get(token);
    }

    public synchronized UserRecord remove(String token) {
        UserRecord rec = byToken.remove(token);
        if (rec != null) {
            byUserId.remove(rec.getUserId());
        }

        return rec;
    }

    /**
     * Create a token for the given user id.  This will produce a unique
     * token for this user.  Tokens are non-deterministic, so calling this
     * method multiple times for the same token should result in different
     * tokens every time.
     * @param userId the user id to create a token for
     * @return a token for the user
     */
    private static String newToken(String userId) {
        MessageDigest md;

        try {
            md = MessageDigest.getInstance("SHA");
        } catch (NoSuchAlgorithmException nsae) {
            throw new IllegalStateException("Unable to find SHA", nsae);
        }

        md.update(userId.getBytes());

        // add some random data to the message to make it unique
        SecureRandom sr = new SecureRandom();
        byte[] buffer = new byte[128];
        sr.nextBytes(buffer);
        md.update(buffer);

        byte[] res = md.digest();
        return new String(Base64.encode(res));
    }
}
