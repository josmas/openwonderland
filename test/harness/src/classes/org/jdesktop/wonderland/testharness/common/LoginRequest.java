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
package org.jdesktop.wonderland.testharness.common;

/**
 *
 * @author paulby
 */
public class LoginRequest extends TestRequest {

    private String sgsServerName;
    private int sgsServerPort;
    private char[] passwd;
    
    public LoginRequest(String sgsServerName, 
                        int sgsServerPort,
                        String username,
                        char[] passwd,
                        float x, float y, float z) {
        super(username);
        this.sgsServerName = sgsServerName;
        this.sgsServerPort = sgsServerPort;
        this.passwd = passwd;
    }

    public String getSgsServerName() {
        return sgsServerName;
    }

    public int getSgsServerPort() {
        return sgsServerPort;
    }


    public char[] getPasswd() {
        return passwd;
    }
    
    
}
