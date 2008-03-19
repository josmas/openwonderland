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

import com.jme.bounding.BoundingVolume;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamException;
import org.jdesktop.wonderland.ExperimentalAPI;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.CellSetup;
import org.jdesktop.wonderland.common.cell.CellTransform;
import org.jdesktop.wonderland.common.messages.Message;

/**
 *
 * @author paulby
 */
@ExperimentalAPI
public class CellHierarchyMessage extends Message {
    
    protected ActionType msgType;
    protected CellID cellID;
    private CellID parentID;
    private BoundingVolume localBounds;
    private BoundingVolume computedBounds;
    private String cellClassName;
    private String cellChannelName;
    private CellTransform cellTransform;
    private CellSetup setupData;
    private String avatarID;
    
    private CellTransform cellLocal2VW;      // FOR TESTING - TODO REMOVE

    /**
     * SET_AVATAR - client informs server which avatar to use for a cell cache
     * LOAD_CLIENT_AVATAR - server informs client to load its avatar
     */
    public enum ActionType { LOAD_CELL, UNLOAD_CELL, MOVE_CELL, CHANGE_PARENT, SET_WORLD_ROOT,
        DELETE_CELL, UPDATE_CELL_CONTENT, SET_AVATAR, LOAD_CLIENT_AVATAR};
    
    /**
     * Creates a new instance of CellHierarchyMessage 
     *
     * 
     * @param cellClassName Fully qualified classname for cell
     */
    public CellHierarchyMessage(ActionType msgType, 
                                String cellClassName, 
                                 BoundingVolume localBounds, 
                                CellID cellID, 
                                CellID parentID,
                                String cellChannelName,
                                CellTransform cellTransform,
                                CellSetup setupData) {
        this.msgType = msgType;
        this.cellClassName = cellClassName;
        this.cellID = cellID;
        this.parentID = parentID;
        this.localBounds = localBounds;
        this.cellChannelName = cellChannelName;
        this.cellTransform = cellTransform;
        this.setupData = setupData;
    }
    
    private CellHierarchyMessage(ActionType msgType) {
        this.msgType = msgType;
    }
    
    public CellHierarchyMessage() {
    }
    
    /**
     * Return the action type of this message
     * @return
     */
    public ActionType getActionType() {
        return msgType;
    }

    public CellID getCellID() {
        return cellID;
    }

    public CellID getParentID() {
        return parentID;
    }

    public BoundingVolume getLocalBounds() {
        return localBounds;
    }

    public BoundingVolume getComputedBounds() {
        return computedBounds;
    }
    
    public String getCellClassName() {
        return cellClassName;
    }

    public String getCellChannelName() {
        return cellChannelName;
    }

    public CellTransform getCellTransform() {
        return cellTransform;
    }

    public CellSetup getSetupData() {
        return setupData;
    }

    private void setAvatarID(String avatarID) {
        this.avatarID = avatarID;
    }
    
    /**
     * The Avatar to which this cache is tied.
     * @return
     */
    public String getAvatarID() {
        return avatarID;
    }

    /**
     * FOR TESTING
     * TODO REMOVE
     * @return
     */
    public CellTransform getCellLocal2VW() {
        return cellLocal2VW;
    }

    /**
     * FOR TESTING
     * TODO REMOVE
     * @return
     */
    public void setCellLocal2VW(CellTransform cellLocal2VW) {
        this.cellLocal2VW = cellLocal2VW;
    }


    public static CellHierarchyMessage newSetAvatarMessage(String avatarID) {
        CellHierarchyMessage ret = new CellHierarchyMessage(ActionType.SET_AVATAR);
        ret.setAvatarID(avatarID);
        return ret;
    }
    
    public static CellHierarchyMessage newLoadClientAvatar(CellID cellID) {
        CellHierarchyMessage ret = new CellHierarchyMessage(ActionType.LOAD_CLIENT_AVATAR);
        return ret;
       
    }
}
