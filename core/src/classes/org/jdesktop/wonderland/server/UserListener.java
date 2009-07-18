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

import com.sun.sgs.app.ManagedObject;
import com.sun.sgs.app.ManagedReference;
import java.io.Serializable;
import org.jdesktop.wonderland.server.comms.WonderlandClientID;

/**
 * Interface for listening for logout notifications.
 *
 * @author paulby
 */
public interface UserListener extends Serializable, ManagedObject {
    /**
     * Notification that a client has logged in.
     * @param clientID of the client that has logged in
     * @param userRef a reference to the user who logged in.
     */
    public void userLoggedIn(WonderlandClientID clientID,
                             ManagedReference<UserMO> userRef);

    /**
     * Notification that a client has logged out.
     * @param clientID of the client that has logged out
     * @param userRef a reference to the user who logged out
     */
    public void userLoggedOut(WonderlandClientID clientID,
                              ManagedReference<UserMO> userRef);
}
