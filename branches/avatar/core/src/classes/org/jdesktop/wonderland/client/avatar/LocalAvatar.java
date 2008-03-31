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

package org.jdesktop.wonderland.client.avatar;

import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import org.jdesktop.wonderland.client.cell.EntityCell;
import org.jdesktop.wonderland.client.tools.AvatarHandler;
import org.jdesktop.wonderland.client.tools.AvatarHandler.AvatarMessageListener;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.CellTransform;
import org.jdesktop.wonderland.common.cell.messages.AvatarMessage;

/**
 * The Avatar that is local to this client. Local means it's controlled
 * by this client
 * 
 * @author paulby
 */
public class LocalAvatar implements AvatarMessageListener {

    private CellID avatarCellID;
    private AvatarHandler avatarClient;
    
    public LocalAvatar(AvatarHandler avatarClient) {
        this.avatarClient = avatarClient;
    }
    
    public void setAvatarCellID(CellID cellID) {
        this.avatarCellID = cellID;
    }
    
    public void localMoveRequest(Vector3f location, Quaternion orientation) {
        avatarClient.send(AvatarMessage.newMoveRequestMessage(avatarCellID, location, orientation));
    }
    
    public void avatarGesture(int gesture) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void avatarMoved(CellTransform transform) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    
}
