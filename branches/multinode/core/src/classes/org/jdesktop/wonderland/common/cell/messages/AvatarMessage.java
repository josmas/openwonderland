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
package org.jdesktop.wonderland.common.cell.messages;

import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import org.jdesktop.wonderland.common.cell.CellID;

/**
 * Messages to/from the avatar cell
 * 
 * @author paulby
 */
public class AvatarMessage extends CellMessage {

    private Vector3f location;
    private Quaternion orientation;
    private String avatarID;

    /**
     * CREATE - ask the server to get or create our avatar
     * MOVE_REQUEST - client asking the server to move avatar
     * MOVED - server informing clients avatar has moved
     * 
     */
    public enum ActionType { CREATE, MOVED, MOVE_REQUEST };
    
    private ActionType actionType;
    
    private AvatarMessage(CellID cellID, ActionType actionType) {
        super(cellID);
        this.actionType = actionType;
    }
    
    public Vector3f getLocation() {
        return location;
    }

    private void setLocation(Vector3f locationVW) {
        this.location = locationVW;
    }

    public Quaternion getOrientation() {
        return orientation;
    }

    public void setOrientation(Quaternion orientation) {
        this.orientation = orientation;
    }

    public ActionType getActionType() {
        return actionType;
    }
    
    public String getAvatarID() {
        return avatarID;
    }

    public void setAvatarID(String avatarID) {
        this.avatarID = avatarID;
    }
    
    public static AvatarMessage newMovedMessage(CellID avatarCellID, Vector3f location, Quaternion orientation) {
        AvatarMessage ret = new AvatarMessage(avatarCellID, ActionType.MOVED);
        ret.setLocation(location);
        ret.setOrientation(orientation);
        return ret;
    }
    
    public static AvatarMessage newMoveRequestMessage(CellID avatarCellID, Vector3f location, Quaternion orientation) {
        AvatarMessage ret = new AvatarMessage(avatarCellID, ActionType.MOVE_REQUEST);
        ret.setLocation(location);
        ret.setOrientation(orientation);
        return ret;
    }
    
    public static AvatarMessage newCreateMessage(String avatarID) {
        AvatarMessage ret = new AvatarMessage(null, ActionType.CREATE);
        ret.setAvatarID(avatarID);
        return ret;
    }
}
