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
package org.jdesktop.wonderland.client.comms;

/**
 * The result of an attempt to login
 * @author jkaplan
 */
public class LoginResult {

    public enum Status {
        CONNECTING, SUCCESS, BAD_AUTH, BAD_SERVER
    }
    
    // the status of the login
    private Status status;
    
    // the reason for the given status
    private String reason;
    
    /**
     * Create a new login result
     */
    public LoginResult() {
        status = Status.CONNECTING;
    }
    
    /**
     * Get the status of logging in
     * @return the status
     */
    public synchronized Status getStatus() {
        return status;
    }

    /**
     * Set the status from logging in
     * @param status the status
     */
    public synchronized void setStatus(Status status) {
        this.status = status;
    }

    /**
     * Get the reason for the given status
     * @return the reason
     */
    public synchronized String getReason() {
        return reason;
    }

    /**
     * Set the reason for the given status
     * @param reason the reason
     */
    public synchronized void setReason(String reason) {
        this.reason = reason;
    }
}
