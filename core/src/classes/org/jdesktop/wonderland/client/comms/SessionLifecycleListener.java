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
 * Listen for new Wonderland sessions being created, and changes to the
 * connection status of Wonderland sessions.
 * @author kaplanj
 */
public interface SessionLifecycleListener {
    /**
     * Called when a new WonderlandSession is created.  When this method is
     * called, the session has been initialized, but not logged in.
     * @param session the session that was created
     */
    public void sessionCreated(WonderlandSession session);
    
    
    /**
     * Called when a session changes state, to one of the
     * CONNECTED, CONNECTING, or DISCONNECTED states.
     */
    public void clientStatusChanged(WonderlandSession session, 
                                    WonderlandSession.Status status);
}
