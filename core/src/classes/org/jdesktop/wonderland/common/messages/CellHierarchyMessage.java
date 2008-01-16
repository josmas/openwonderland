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
package org.jdesktop.wonderland.common.messages;

import javax.media.j3d.Bounds;
import javax.vecmath.Matrix4d;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.CellSetup;
import org.jdesktop.wonderland.server.cell.CellMO;

/**
 *
 * @author paulby
 */
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
    private CellHierarchyMessage(ActionType msgType, 
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

    /**
     * Return a new Create cell message
     */
    public static CellHierarchyMessage newCreateCellMessage(CellMO cell) {
        CellID parent;
        
        parent = cell.getParent().getCellID();
        
        return new CellHierarchyMessage(CellHierarchyMessage.ActionType.LOAD_CELL,
            cell.getClientCellClassName(),
            cell.getComputedWorldBounds(),
            cell.getCellID(),
            parent,
            cell.getCellChannelName(),
            cell.getTransform(),
            cell.getSetupData()
            );
    }
    
    /**
     * Return a new Cell inactive message
     */
    public static CellHierarchyMessage newInactiveCellMessage(CellMO cell) {
        return new CellHierarchyMessage(CellHierarchyMessage.ActionType.CELL_INACTIVE,
            null,
            null,
            cell.getCellID(),
            null,
            null,
            null,
            null
            );
    }
    
    /**
     * Return a new Delete cell message
     */
    public static CellHierarchyMessage newDeleteCellMessage(CellID cellID) {
        return new CellHierarchyMessage(CellHierarchyMessage.ActionType.DELETE_CELL,
            null,
            null,
            cellID,
            null,
            null,
            null,
            null
            );
    }
    /**
     * Return a new Delete tile message
     */
    public static CellHierarchyMessage newRootCellMessage(CellMO cell) {
        return new CellHierarchyMessage(CellHierarchyMessage.ActionType.SET_WORLD_ROOT,
            null,
            null,
            cell.getCellID(),
            null,
            null,
            null,
            null
            );
    }
    
    /**
     * Return a new Delete tile message
     */
    public static CellHierarchyMessage newChangeParentCellMessage(CellMO childCell, CellMO parentCell) {
        return new CellHierarchyMessage(CellHierarchyMessage.ActionType.CHANGE_PARENT,
            null,
            null,
            childCell.getCellID(),
            parentCell.getCellID(),
            null,
            null,
            null
            
            );
    }
    
    /**
     * Return a new tile move message
     */
    public static CellHierarchyMessage newCellMoveMessage(CellMO cell) {
        return new CellHierarchyMessage(CellHierarchyMessage.ActionType.MOVE_CELL,
            null,
            cell.getComputedWorldBounds(),
            cell.getCellID(),
            null,
            null,
            cell.getTransform(),
            null
            );
    }
    
    /**
     * Return a new cell Reconfigure message.
     */
    public static CellHierarchyMessage newContentUpdateCellMessage(CellMO cellGLO) {
        
        /* Return a new CellHiearchyMessage class, with populated data fields */
        return new CellHierarchyMessage(CellHierarchyMessage.ActionType.CONTENT_UPDATE_CELL,
            cellGLO.getClientCellClassName(),
            cellGLO.getComputedWorldBounds(),
            cellGLO.getCellID(),
            cellGLO.getParent().getCellID(),
            cellGLO.getCellChannelName(),
            cellGLO.getTransform(),
            cellGLO.getSetupData()
            
            );
    }
}
