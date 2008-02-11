/**
 * Project Wonderland
 *
 * Copyright (c) 2004-2008, Sun Microsystems, Inc., All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * $Revision$
 * $Date$
 * $State$
 */
package org.jdesktop.wonderland.server.cell;

import com.sun.sgs.app.AppContext;
import com.sun.sgs.app.ManagedObject;
import com.sun.sgs.app.ManagedReference;
import com.sun.sgs.app.Task;
import java.io.Serializable;
import java.util.ArrayList;

/**
 * Temporary approach. In the final version AvatarCellCaches will be updated
 * by their own task, but at the moment that is causing excessive data contention.
 * 
 * @author paulby
 */
public class CacheManager implements ManagedObject, Serializable {

    private ArrayList<ManagedReference> caches = new ArrayList();
    
    private static final String BINDING_NAME="CacheManager";
    
    private CacheManager() {
        AppContext.getDataManager().setBinding(BINDING_NAME, this);
        
        AppContext.getTaskManager().schedulePeriodicTask(new CacheRevalidateTask(), 2000, 500);
    }
    
    public static void initialize() {
        new CacheManager();
    }
        
    private void addCacheImpl(AvatarCellCacheMO cache) {
        caches.add(AppContext.getDataManager().createReference(cache));
    }
    
    private void removeCacheImpl(AvatarCellCacheMO cache) {
        caches.remove(AppContext.getDataManager().createReference(cache));
    }
    
    public static void addCache(AvatarCellCacheMO cache) {
        CacheManager mgr = AppContext.getDataManager().getBinding(BINDING_NAME, CacheManager.class);
        AppContext.getDataManager().markForUpdate(mgr);
        
        mgr.addCacheImpl(cache);
    }

    public static void removeCache(AvatarCellCacheMO cache) {
        CacheManager mgr = AppContext.getDataManager().getBinding(BINDING_NAME, CacheManager.class);
        AppContext.getDataManager().markForUpdate(mgr);
        
        mgr.removeCacheImpl(cache);
    }

    private void revalidate() {
        for(ManagedReference cacheRef : caches)
            cacheRef.getForUpdate(AvatarCellCacheMO.class).revalidate();
    }
    
    static class CacheRevalidateTask implements Task, Serializable {
            public void run() throws Exception {
                CacheManager mgr = AppContext.getDataManager().getBinding(BINDING_NAME, CacheManager.class);
                mgr.revalidate();
            }
        
    }
}
