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
import java.io.Serializable;

/**
 * An identity for the Wonderland system
 * @author jkaplan
 */
public class WonderlandIdentity implements Identity, Serializable {

    private String username;
    private String fullname;
    private String email;
    
    public WonderlandIdentity(String username, String fullname, String email) {
        this.username = username;
        this.fullname = fullname;
        this.email = email;
    }

    public String getName() {
        return username;
    }
    
    public String getFullName() {
        return fullname;
    }
    
    public String getEmail() {
        return email;
    }

    public void notifyLoggedIn() {
        // do nothing
    }

    public void notifyLoggedOut() {
        // do nothing
    }
}
