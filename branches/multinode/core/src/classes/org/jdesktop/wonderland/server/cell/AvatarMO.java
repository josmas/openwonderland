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

import com.jme.bounding.BoundingSphere;
import com.jme.math.Vector3f;
import com.sun.sgs.app.AppContext;
import com.sun.sgs.app.ManagedReference;
import java.io.Serializable;
import org.jdesktop.wonderland.ExperimentalAPI;
import org.jdesktop.wonderland.common.cell.AvatarBoundsHelper;
import org.jdesktop.wonderland.common.cell.CellTransform;
import org.jdesktop.wonderland.server.UserMO;

/**
 * Superclass for all avatar cells. 
 * 
 * @author paulby
 */
@ExperimentalAPI
public class AvatarMO extends MoveableCellMO {
    
    private ManagedReference<AvatarCellCacheMO> avatarCellCacheRef;
    private ManagedReference<UserMO> userRef;

    public AvatarMO(UserMO user) {
        this.userRef = AppContext.getDataManager().createReference(user);
//        addCellMoveListener(new AvatarMoveListener());
        setTransform(new CellTransform(null, new Vector3f()));
        setLocalBounds(new BoundingSphere(AvatarBoundsHelper.AVATAR_CELL_SIZE, new Vector3f()));
    }
    
    public UserMO getUser() {
        return userRef.get();
    }
    
    /**
     * Return the avatar cell cache managed object for this avatar
     * @return
     */
    AvatarCellCacheMO getCellCache() {
        if (avatarCellCacheRef==null) {
            AvatarCellCacheMO cache = new AvatarCellCacheMO(this);
            avatarCellCacheRef = AppContext.getDataManager().createReference(cache);
        }
        
        return avatarCellCacheRef.getForUpdate();
    }
    
    class AvatarMoveListener implements CellMoveListener, Serializable {

        public void cellMoved(MoveableCellMO cell, CellTransform transform) {
            System.out.println("AvatarMO.cellMoved");
        }
        
    }
}
