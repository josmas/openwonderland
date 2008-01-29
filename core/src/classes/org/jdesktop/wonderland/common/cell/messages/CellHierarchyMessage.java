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

import javax.media.j3d.Bounds;
import javax.vecmath.Matrix4d;
import org.jdesktop.wonderland.ExperimentalAPI;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.CellSetup;
import org.jdesktop.wonderland.common.messages.Message;

/**
 *
 * @author paulby
 */
@ExperimentalAPI
public class CellHierarchyMessage extends Message {
    
    public enum ActionType { LOAD_CELL, MOVE_CELL, CELL_INACTIVE, CHANGE_PARENT, SET_WORLD_ROOT,
        DELETE_CELL, CONTENT_UPDATE_CELL};
    
    private ActionType msgType;
    private CellID cellID;
    private CellID parentID;
    private Bounds bounds;
    private String cellClassName;
    private String cellChannelName;
    private Matrix4d cellOrigin;
    private CellSetup setupData;
    
    /**
     * Creates a new instance of CellHierarchyMessage 
     *
     * 
     * @param cellClassName Fully qualified classname for cell
     */
    public CellHierarchyMessage(ActionType msgType, 
                                String tileClassName, 
                                Bounds bounds, 
                                CellID cellID, 
                                CellID parentID,
                                String cellChannelName,
                                Matrix4d cellOrigin,
                                CellSetup setupData) {
        this.msgType = msgType;
        this.cellClassName = tileClassName;
        this.cellID = cellID;
        this.parentID = parentID;
        this.bounds = bounds;
        this.cellChannelName = cellChannelName;
        this.cellOrigin = cellOrigin;
        this.setupData = setupData;
    }
}
