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

/**
 *
 * @author paulby
 */
public class CellListMO implements ManagedObject, Serializable {
    private long version = Long.MIN_VALUE;
    private HashMap<CellID, CellDescription> cells = new HashMap();

    /**
     * Add cell to this list, the list is actually a set so multiple adds
     * of the same cell only result in a single entry in the list.
     * 
     * This method increments the list version field.
     * 
     * @param cell
     */
    public void addCell(CellMO cell) {
        assert(cell!=null);
        cells.put(cell.getCellID(), new ListInfo(cell));
        version++;
    }
    
    /**
     * Remove the specified cell from this list, increment the list version
     * number.
     * 
     * @param cell
     */
    public void removeCell(CellMO cell) {
        assert(cell!=null);
        if (cells.remove(cell.getCellID())==null) {
            Logger.getAnonymousLogger().warning("removeCell failed, object was not present");
        }
        version++;        
    }
    
    /**
     * Return the list version number
     * @return the list version number
     */
    public long getVersion() {
        return version;
    }
    
    /**
     * Return the list of cells. This is a shallow clone of the internal list
     * of this object.
     * @return
     */
    public Collection<CellDescription> getCells() {
        return cells.values();
    }
    
    // TODO we don't need full set of CellDescription fields, reduce
    // once we settle of the cache scheme.
    public static class ListInfo implements CellDescription, Serializable {
        
        private CellID cellID;
        private ManagedReference<CellMO> cellRef;
        private int contentsVersion;
        private int transformVersion;
        private boolean isMovable;
        private BoundingVolume localBounds;
        private CellTransform cellTransform;
        private String name;
        
        ListInfo(CellMO cell) {
            cellID = cell.getCellID();
            cellRef = AppContext.getDataManager().createReference(cell);
            contentsVersion = 0;
            transformVersion = 0;
            isMovable = (cell.getComponent(MovableComponentMO.class)!=null);
            if (cell instanceof AvatarMO)
                isMovable=false;
            localBounds = cell.getLocalBounds();
            cellTransform = cell.getTransform();
            name = cell.getName();
            System.out.println("Adding to CellList "+name+"  "+cellTransform.getTranslation(null));
        }

        public CellID getCellID() {
            return cellID;
        }
        
        public CellMO getCell() {
            return cellRef.get();
        }

        public int getContentsVersion() {
            return contentsVersion;
        }

        public int getTransformVersion() {
            if (isMovable)
                transformVersion++;     // HACK FOR TESTING
            return transformVersion;
        }

        public BoundingVolume getLocalBounds() {
            return localBounds;
        }

        public CellTransform getTransform() {
            if (isMovable) {
                System.out.println("Cell "+name+"  "+cellRef.get().getTransform());
                return cellRef.get().getTransform();
            }
            return cellTransform;
        }

        public short getPriority() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public boolean isMovableCell() {
            return isMovable;
        }

        public Class getCellClass() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        
    }
}
