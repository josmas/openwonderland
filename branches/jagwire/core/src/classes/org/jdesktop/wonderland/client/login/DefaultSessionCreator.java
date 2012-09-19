/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jdesktop.wonderland.client.login;

import org.jdesktop.wonderland.client.comms.WonderlandServerInfo;
import org.jdesktop.wonderland.client.comms.WonderlandSession;
import org.jdesktop.wonderland.client.comms.session.WonderlandSessionImpl;

/**
 *
 * @author Ryan
 */
public class DefaultSessionCreator implements SessionCreator<WonderlandSession> {

    public WonderlandSession createSession(ServerSessionManager manager, WonderlandServerInfo serverInfo, ClassLoader loader) {
        return new WonderlandSessionImpl(manager, serverInfo, loader);
    }
    
}
