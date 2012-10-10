/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jdesktop.wonderland.client.login;

import java.lang.ref.SoftReference;
import org.jdesktop.wonderland.client.comms.SessionStatusListener;
import org.jdesktop.wonderland.client.comms.WonderlandSession;
import org.jdesktop.wonderland.client.comms.WonderlandSession.Status;

/**
 *
 * @author Ryan
 */
public class CacheReference extends SoftReference<ServerSessionManager> implements SessionLifecycleListener, SessionStatusListener {
    // a strong reference that we hold as long as there are sessions
    // associated with the given session manager
    private ServerSessionManager strongRef;
    private final ManagerCache cache;

    public CacheReference(ServerSessionManager manager, final ManagerCache cache) {
        super(manager);
        this.cache = cache;
        manager.addLifecycleListener(this);
        if (manager.getAllSessions().size() > 0) {
            strongRef = manager;
        }
    }

    public void sessionCreated(WonderlandSession session) {
        session.addSessionStatusListener(this);
    }

    public void primarySession(WonderlandSession session) {
        // ignore
        // ignore
        // ignore
        // ignore
        // ignore
        // ignore
    }

    public void sessionStatusChanged(WonderlandSession session, Status status) {
        // update our reference if the session status changes
        if (status == Status.CONNECTED || status == Status.DISCONNECTED) {
            if (session.getSessionManager().getAllSessions().size() > 0) {
                strongRef = session.getSessionManager();
            } else {
                strongRef = null;
            }
        }
    }
    
}
