/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.client.cell;

import com.jme.bounding.BoundingVolume;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.wonderland.client.avatar.ViewCell;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.CellSetup;
import org.jdesktop.wonderland.common.cell.CellTransform;
import org.jdesktop.wonderland.common.cell.MultipleParentException;
import org.jdesktop.wonderland.common.cell.messages.ViewCreateResponseMessage;

/**
 *
 * @author paulby
 */
public class CellCacheBasicImpl implements CellCache, CellCacheConnection.CellCacheMessageListener {

    private Map<CellID, Cell> cells = Collections.synchronizedMap(new HashMap());
    private Set<Cell> rootCells = Collections.synchronizedSet(new HashSet());
    
    private static Logger logger = Logger.getLogger(CellCacheBasicImpl.class.getName());
    
    private ViewCell viewCell = null;
    
    public Cell getCell(CellID cellId) {
        return cells.get(cellId);
    }
    
    /**
     * Return the set of cells in this cache
     * @return
     */
    public Cell[] getCells() {
        return (Cell[]) cells.values().toArray(new Cell[cells.size()]);
    }

    public void loadCell(CellID cellId, 
                         String className, 
                         BoundingVolume localBounds, 
                         CellID parentCellID, 
                         CellTransform cellTransform, 
                         CellSetup setup) {
        Cell cell = instantiateCell(className, cellId);
        cell.setLocalBounds(localBounds);
        cell.setTransform(cellTransform);
        Cell parent = cells.get(parentCellID);
        if (parent!=null) {
            try {
                parent.addChild(cell);
            } catch (MultipleParentException ex) {
                Logger.getLogger(CellCacheBasicImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        cells.put(cellId, cell);
        if (cell instanceof RootCell) {
            rootCells.add(cell);
        }
    }

    public void unloadCell(CellID cellId) {
        cells.remove(cellId);
    }

    public void deleteCell(CellID cellId) {
        // TODO - remove local resources from client asset cache
        cells.remove(cellId);
    }

    public void moveCell(CellID cellId, CellTransform cellTransform) {
        logger.warning("MOVE CELL");
        Cell cell = cells.get(cellId);
        if (cell==null) {
            // TODO this is probably ok, need to check
            logger.warning("Got move for non-local cell");
            return;
        }

        if (!(cell instanceof EntityCell)) {
            cell.setTransform(cellTransform);
        }
    }
    
    private Cell instantiateCell(String className, CellID cellId) {
        Cell cell;
        
        try {
            Class clazz = Class.forName(className);
            Constructor constructor = clazz.getConstructor(CellID.class);
            cell = (Cell) constructor.newInstance(cellId);
        } catch(Exception e) {
            logger.log(Level.SEVERE, "Problem instantiating cell "+className, e);
            return null;
        }
        
        return cell;
    }

    public void viewSetup(CellID viewCellID) {
        viewCell = (ViewCell)cells.get(viewCellID);
    }

}
