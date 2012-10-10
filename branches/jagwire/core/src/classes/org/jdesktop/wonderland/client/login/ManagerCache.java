/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jdesktop.wonderland.client.login;

import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jdesktop.wonderland.client.comms.SessionStatusListener;
import org.jdesktop.wonderland.client.comms.WonderlandSession;
import org.jdesktop.wonderland.client.comms.WonderlandSession.Status;

/**
 * A cache of session manager objects.  This cache maintains real
 * references to any session with active sessions, but only weak references
 * to a session with no active sessions.  When a session is removed from
 * the cache, all associated plugins are cleaned up.
 */
public class ManagerCache {
    private final Map<String, Reference<ServerSessionManager>> references = new HashMap<String, Reference<ServerSessionManager>>();

    public synchronized ServerSessionManager get(String key) {
        Reference<ServerSessionManager> ref = references.get(key);
        if (ref == null) {
            return null;
        }
        return ref.get();
    }

    public synchronized Collection<ServerSessionManager> getAll() {
        List<ServerSessionManager> out = new ArrayList<ServerSessionManager>();
        for (Reference<ServerSessionManager> ref : references.values()) {
            ServerSessionManager mgr = ref.get();
            if (mgr != null) {
                out.add(mgr);
            }
        }
        return out;
    }

    public synchronized ServerSessionManager put(String key, ServerSessionManager value) {
        // put the reference
        CacheReference ref = new CacheReference(value, this);
        Reference<ServerSessionManager> old = references.put(key, ref);
        if (old == null) {
            return null;
        }
        return old.get();
    }

    public synchronized ServerSessionManager remove(String key) {
        Reference<ServerSessionManager> ref = references.remove(key);
        if (ref == null) {
            return null;
        }
        return ref.get();
    }
    
}
