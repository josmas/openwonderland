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
    private int notificationType;

    /**
     * Constant indicating that the notification should be of a logout event.
     */
    public static final int LOGOUT = 0;

    /**
     * Constant indicating that the notification should be of a logout event.
     */
    public static final int LOGIN = 1;

    public UserListenerNotifier(ManagedReference<UserListener> listenerRef, WonderlandClientID clientID, int notificationType) {
        this.listenerRef = listenerRef;
        this.clientID = clientID;
        this.notificationType = notificationType;
    }

    public void run() throws Exception {

        if(notificationType==LOGOUT)
            listenerRef.get().userLoggedOut(clientID);
        else if(notificationType==LOGIN)
            listenerRef.get().userLoggedIn(clientID);
        else
            throw new Exception("Invalid notification type: " + notificationType + ". Expecting LOGIN(1) or LOGOUT(0).");
    }

}
