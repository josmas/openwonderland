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
package org.jdesktop.wonderland.server;

import com.sun.sgs.app.ManagedReference;
import com.sun.sgs.app.Task;
import java.io.Serializable;
import org.jdesktop.wonderland.server.comms.WonderlandClientID;

/**
 *
 * @author paulby
 */
public class UserListenerNotifier implements Task, Serializable {

    private ManagedReference<UserListener> listenerRef;
    private WonderlandClientID clientID;

    public UserListenerNotifier(ManagedReference<UserListener> listenerRef, WonderlandClientID clientID) {
        this.listenerRef = listenerRef;
        this.clientID = clientID;
    }

    public void run() throws Exception {
        listenerRef.get().userLoggedOut(clientID);
    }

}
