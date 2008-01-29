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

import org.jdesktop.wonderland.ExperimentalAPI;

/**
 * A basic WonderlandClient that can be extended to develop real clients.
 * @author jkaplan
 */
@ExperimentalAPI
public abstract class BaseClient implements WonderlandClient {
    /** the current status */
    private Status status = Status.DETACHED;
    
    /** the session we are attached to, or null if we are not attached to
     * any sessions. */
    private WonderlandSession session;
    
    public WonderlandSession getSession() {
        return session;
    }
    
    public Status getStatus() {
        return status;
    }
    
    /**
     * Set the status
     * @param status the new status
     */
    protected synchronized void setStatus(Status status) {
        this.status = status;
    }

    /**
     * Attach this client to the given session
     * @param session the session to attach to
     * @throws AttachFailureException if there is a problem attaching
     */
    public void attach(WonderlandSession session) 
            throws AttachFailureException
    {
        session.attach(this);
    }
    
    public synchronized void attached(WonderlandSession session) {
        this.session = session;
        
        setStatus(Status.ATTACHED);
    }

    /**
     * Detach from the current session
     */
    public void detach() {
        getSession().detach(this);
    }
    
    public void detached() {
        setStatus(status.DETACHED);
    }
    
    @Override
    public String toString() {
        return getClientType().toString();
    }
}
