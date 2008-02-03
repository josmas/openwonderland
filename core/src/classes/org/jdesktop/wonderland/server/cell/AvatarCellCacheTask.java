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
import com.sun.sgs.app.ManagedReference;
import com.sun.sgs.app.Task;
import java.io.Serializable;

/**
 *
 * @author paulby
 */
class AvatarCellCacheTask implements Task, Serializable {
    private ManagedReference userCache;
    private ManagedReference moveableCellRef = null;
    
    enum TaskType { REVALIDATE, CELL_MOVED };
    
    private TaskType action;

    private AvatarCellCacheTask(ManagedReference userCache, TaskType action, ManagedReference moveableCellRef) {
        this.userCache = userCache;            
        this.action = action;
        this.moveableCellRef = moveableCellRef;
    }
    
    public static AvatarCellCacheTask createRevalidateTask(ManagedReference userCache) {
        return new AvatarCellCacheTask(userCache, TaskType.REVALIDATE, null);
    }
    
    public static AvatarCellCacheTask createCellMovedTask(ManagedReference userCache, MoveableCellMO cell) {
        return new AvatarCellCacheTask(userCache, TaskType.CELL_MOVED, AppContext.getDataManager().createReference(cell));
    }

    public void run() throws Exception {
        switch(action) {
        case REVALIDATE :
            long t = System.nanoTime();
            userCache.getForUpdate(AvatarCellCacheMO.class).revalidate();
            break;
        case CELL_MOVED :
            // TODO fix
            //userCache.getForUpdate(AvatarCellCacheMO.class).cellMoved(moveableCellRef.get(MoveableCellMO.class));
            break;
        }
    }

}
