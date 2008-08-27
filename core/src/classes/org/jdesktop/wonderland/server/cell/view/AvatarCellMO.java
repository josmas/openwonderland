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
package org.jdesktop.wonderland.server.cell.view;

import org.jdesktop.wonderland.server.cell.*;
import com.jme.bounding.BoundingSphere;
import com.jme.math.Vector3f;
import com.sun.sgs.app.AppContext;
import com.sun.sgs.app.ClientSession;
import com.sun.sgs.app.ManagedReference;
import java.io.Serializable;
import org.jdesktop.wonderland.common.ExperimentalAPI;
import org.jdesktop.wonderland.common.cell.AvatarBoundsHelper;
import org.jdesktop.wonderland.common.cell.CellTransform;
import org.jdesktop.wonderland.common.cell.ClientCapabilities;
import org.jdesktop.wonderland.server.UserMO;
import org.jdesktop.wonderland.server.cell.MovableComponentMO.CellTransformChangeListener;

/**
 * Superclass for all avatar cells. 
 * 
 * @author paulby
 */
@ExperimentalAPI
public class AvatarCellMO extends ViewCellMO {
    
    private ManagedReference<ViewCellCacheMO> avatarCellCacheRef;
    private ManagedReference<UserMO> userRef;

    public AvatarCellMO(UserMO user) {
        super(new BoundingSphere(AvatarBoundsHelper.AVATAR_CELL_SIZE, new Vector3f()),
              new CellTransform(null, new Vector3f())  );
        this.userRef = AppContext.getDataManager().createReference(user);
    }
    
    @Override 
    protected String getClientCellClassName(ClientSession clientSession,ClientCapabilities capabilities) {
        return "org.jdesktop.wonderland.client.cell.view.AvatarCell";
    }
    
    /**
     * {@inheritDoc}
     */
    public UserMO getUser() {
        return userRef.get();
    }
    
    /**
     * {@inheritDoc}
     */
    public ViewCellCacheMO getCellCache() {
        if (avatarCellCacheRef==null) {
            ViewCellCacheMO cache = new ViewCellCacheMO(this);
            avatarCellCacheRef = AppContext.getDataManager().createReference(cache);
        }
        
        return avatarCellCacheRef.getForUpdate();
    }

    public CellMO getCell() {
        return this;
    }
    
    class AvatarMoveListener implements CellTransformChangeListener, Serializable {

        public void transformChanged(CellMO cell, CellTransform transform) {
            System.out.println("AvatarMO.cellMoved");
        }
        
    }

    public CellTransform getWorldTransform() {
        return super.getLocalToWorld();
    }

}
