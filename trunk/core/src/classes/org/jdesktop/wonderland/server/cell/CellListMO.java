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

import com.jme.bounding.BoundingVolume;
import com.sun.sgs.app.AppContext;
import com.sun.sgs.app.ManagedObject;
import com.sun.sgs.app.ManagedReference;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.logging.Logger;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.CellTransform;
import org.jdesktop.wonderland.server.TimeManager;

/**
 *
 * @author paulby
 */
public class CellListMO implements Serializable {
    private long listTimestamp = Long.MIN_VALUE;
    private HashMap<CellID, CellDescription> cells = new HashMap();

    /**
     * Add cell to this list, the list is actually a set so multiple adds
     * of the same cell only result in a single entry in the list.
     * 
     * This method updates the list timestamp
     * 
     * @param cell
     */
    public CellDescription addCell(CellMO cell) {
        assert(cell!=null);
        ListInfo info = new ListInfo(cell);
        cells.put(cell.getCellID(), info);
        listTimestamp = TimeManager.getWonderlandTime();
        return info;
    }
    
    public void addCell(CellDescription cellDesc) {
        cells.put(cellDesc.getCellID(), cellDesc);
        listTimestamp = TimeManager.getWonderlandTime();
    }
    
    /**
     * Remove the specified cell from this list, updating the listTimestamp with the
     * time of the change.
     * 
     * @param cell
     */
    public void removeCell(CellMO cell) {
        assert(cell!=null);
        CellDescription desc = cells.remove(cell.getCellID());
        if (desc==null) {
//            Logger.getAnonymousLogger().warning("removeCell failed, cell was not present "+cell.getCellID());
        } else {
            listTimestamp = TimeManager.getWonderlandTime();
        }
    }
    
    /**
     * Remove the specified cell from this list, updating the listTimestamp with the
     * time of the change.
     * 
     * @param cell
     */
    public void removeCell(CellDescription cellDesc) {
        CellDescription desc = cells.remove(cellDesc.getCellID());
        if (desc==null) {
//            Logger.getAnonymousLogger().warning("removeCell failed, cell was not present "+cellDesc.getCellID());
        } else {
            listTimestamp = TimeManager.getWonderlandTime();
        }
        
    }
    
    /**
     * Clear the contents of the list
     */
    public void clear() {
        cells.clear();
    }
    
    /**
     * Return timestamp of last change of the list or one of its items.
     */
    public long getChangeTimestamp() {
        return listTimestamp;
    }
    
    /**
     * Return the list of cells. This is a shallow clone of the internal list
     * of this object.
     * @return
     */
    public Collection<CellDescription> getCells() {
        return cells.values();
    }
    
    public int size() {
        return cells.size();
    }
    
    /**
     * Return true if this list contains the specified cell
     * @param cellDesc
     * @return
     */
    public boolean contains(CellDescription cellDesc) {
        return cells.containsKey(cellDesc.getCellID());
    }
    
    /**
     * Create a shallow clone
     * @return
     */
    @Override
    public Object clone() {
        CellListMO ret = new CellListMO();
        ret.cells = (HashMap<CellID, CellDescription>) this.cells.clone();
        
        return ret;
    }
    
    void notifyCellTransformChanged(CellMO cell, long timestamp) {
        CellDescription desc = cells.get(cell.getCellID());
        if (desc!=null) {
//            System.err.println("CellListMO transform changed "+timestamp+"  "+desc.getCellID()+"  "+this);
            desc.setTransform(cell.getLocalTransform(null), timestamp);
            listTimestamp = timestamp;
        }
    }
    
    // TODO we don't need full set of CellDescription fields, reduce
    // once we settle of the cache scheme.
    public static class ListInfo implements CellDescription, Serializable {
        
        private CellID cellID;
        private ManagedReference<CellMO> cellRef;
        private long contentsTimestamp;
        private long transformTimestamp;
        private BoundingVolume localBounds;
        private CellTransform cellTransform;
        private String name;
        private Class cellClass;
        private boolean isMovable;
        
        ListInfo(CellMO cell) {
            cellID = cell.getCellID();
            cellRef = AppContext.getDataManager().createReference(cell);
            contentsTimestamp = 0;
            transformTimestamp = 0;
            localBounds = cell.getLocalBounds();
            cellTransform = cell.getLocalTransform(null);
            name = cell.getName();
            cellClass = cell.getClass();
            isMovable = cell.isMovable();
        }

        public CellID getCellID() {
            return cellID;
        }
        
        public CellMO getCell() {
            return cellRef.get();
        }

        public long getContentsTimestamp() {
            return contentsTimestamp;
        }

        public long getTransformTimestamp() {
            return transformTimestamp;
        }

        public BoundingVolume getLocalBounds() {
            return localBounds;
        }

        public CellTransform getTransform() {
            if (isMovable) {
                System.out.println("Cell "+name+"  "+cellRef.get().getLocalTransform(null));
            }
            return cellTransform;
        }

        public void setTransform(CellTransform localTransform, long timestamp) {
            cellTransform = localTransform;
            transformTimestamp = timestamp;
        }
        
        public short getPriority() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public Class getCellClass() {
            return cellClass;
        }

        public boolean isMovable() {
            return isMovable;
        }

        
    }
}
