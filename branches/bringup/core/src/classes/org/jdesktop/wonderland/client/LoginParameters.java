/**
 * Project Wonderland
 *
 * $RCSfile: LogControl.java,v $
 *
 * Copyright (c) 2004-2007, Sun Microsystems, Inc., All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * $Revision: 1.3 $
 * $Date: 2007/10/23 18:27:41 $
 * $State: Exp $
 */
package org.jdesktop.wonderland.client;

/**
 * Data required to log user into a server.
 * 
 * @author paulby
 */
class LoginParameters {

    private String userName;
    private char[] password;

    public LoginParameters(String userName, char[] password) {
        this.userName = userName;
        this.password = password;
    }
    
    /**
     * return the userName
     * @return the userName
     */
    public String getUserName() {
        return userName;
    }

    void setUserName(String userName) {
        this.userName = userName;
    }

    /**
     * Return the users password
     * 
     * @return
     */
    public char[] getPassword() {
        return password;
    }

    void setPassword(char[] password) {
        this.password = password;
    }
}
