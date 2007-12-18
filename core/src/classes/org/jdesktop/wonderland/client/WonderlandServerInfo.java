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
 * All data to uniquely identify a servre
 * 
 * @author paulby
 */
public class WonderlandServerInfo {

    private String hostname;
    private int sgsPort;
    
    public WonderlandServerInfo(String hostname, int sgsPort) {
        this.hostname = hostname;
        this.sgsPort = sgsPort;
    }

    /**
     * Return the hostname for this server
     * 
     * @return
     */
    public String getHostname() {
        return hostname;
    }

    void setHostname(String hostname) {
        this.hostname = hostname;
    }

    /**
     * Return the darkstar port for this server
     * 
     * @return
     */
    public int getSgsPort() {
        return sgsPort;
    }

    void setSgsPort(int sgsPort) {
        this.sgsPort = sgsPort;
    }
    
}
