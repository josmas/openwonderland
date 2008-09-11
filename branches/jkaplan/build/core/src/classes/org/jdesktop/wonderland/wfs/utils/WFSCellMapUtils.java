/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.wfs.utils;

import java.util.Iterator;

/**
 * XXX
 * 
 * @author Jordan Slott <jslott@dev.java.net>
 */
public class WFSCellMapUtils {
    /**
     * Takes a cell map and returns a set that contains the cells that are
     * in this cell map, but not the given cell map (hence, have been "deleted"
     * from this cell map.
     * 
     * @param map The map of cells to compare to
     * @return A set of cells that are not in the given map, but in this map
     */
    public static WFSCellSet getDeletedCells(WFSCellMap map1, WFSCellMap map2) {
        WFSCellSet deleted = new WFSCellSet(map1.keySet());
        deleted.removeAll(map2.keySet());
        return deleted;
    }
    
    /**
     * Takes a cell map and returns a set that contains the cells that are
     * not in this cell map, but in the given cell map (hence, have been "added"
     * to this cell map.
     * 
     * @param map The map of cells to compare to
     * @return A set of cells that are in the given map, but not this map
     */
    public static WFSCellSet getAddedCells(WFSCellMap map1, WFSCellMap map2) {
        WFSCellSet added = new WFSCellSet(map2.keySet());
        added.removeAll(map1.keySet());
        return added;
    }
    
    /**
     * Takes a cell map and returns a set that contains the cells that are
     * in both maps and have been modified. A cell has been modified if the
     * cell in the given map has a modified date later than the cell on this
     * map
     * 
     * @param map The map of cells to compare to
     * @return A set of cells that have been modified
     */
    public static WFSCellSet getModifiedCells(WFSCellMap<Long> map1, WFSCellMap<Long> map2) {
        /* Find the intersection between the two sets */
        WFSCellSet modified = new WFSCellSet(map2.keySet());
        modified.retainAll(map1.keySet());
        
        /* For each element in the intersection set, check the modified dates */
        Iterator<String> it = modified.iterator();
        while (it.hasNext() == true) {
            String name = it.next();
            long existingDate = map1.get(name);
            long newDate = map2.get(name);
            if (newDate > existingDate) {
                it.remove();
            }
        }
        return modified;
    }
}
