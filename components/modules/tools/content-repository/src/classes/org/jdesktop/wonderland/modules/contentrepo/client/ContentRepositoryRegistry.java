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
package org.jdesktop.wonderland.modules.contentrepo.client;

import java.util.HashMap;
import java.util.Map;
import org.jdesktop.wonderland.client.login.ServerSessionManager;

/**
 * Common place to register repository instances
 * @author jkaplan
 */
public class ContentRepositoryRegistry {
    private Map<ServerSessionManager, ContentRepository> repos =
            new HashMap<ServerSessionManager, ContentRepository>();
    
    public static ContentRepositoryRegistry getInstance() {
        return SingletonHolder.REGISTRY;
    }

    private ContentRepositoryRegistry() {        
    }

    /**
     * Register a connection to a repository
     * @param session the session to register
     * @param repo the repository associated with that session
     */
    public void registerRepository(ServerSessionManager session,
                                   ContentRepository repo)
    {
        repos.put(session, repo);
    }

    /**
     * Unregister a session
     * @param session the session to unregister
     */
    public void unregisterRepository(ServerSessionManager session)
    {
        repos.remove(session);
    }

    /**
     * Find a repository for the given session
     * @param session the session to find a repository for
     * @return the repository associated with the given session,
     * or null if it can't be found
     */
    public ContentRepository getRepository(ServerSessionManager session) {
        return repos.get(session);
    }


    static class SingletonHolder {
        private static final ContentRepositoryRegistry REGISTRY =
                new ContentRepositoryRegistry();
    }
}
