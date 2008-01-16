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
import javax.vecmath.Matrix4d;
import org.jdesktop.wonderland.server.UserMO;

/**
 * Superclass for all avatar cells. 
 * 
 * @author paulby
 */
public class AvatarMO extends MoveableCellMO {
    
    private ManagedReference avatarCellCache;
    private ManagedReference userRef;

    public AvatarMO(UserMO user) {
        this.userRef = AppContext.getDataManager().createReference(user);
        addCellMoveListener(new AvatarMoveListener());
    }
    
    public UserMO getUser() {
        return userRef.get(UserMO.class);
    }
    
    class AvatarMoveListener implements CellMoveListener {

        public void cellMoved(MoveableCellMO cell, Matrix4d transform) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        
    }
}
