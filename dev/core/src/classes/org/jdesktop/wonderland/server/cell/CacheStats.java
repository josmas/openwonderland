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
import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author paulby
 */
public class CacheStats {

    private HashMap<SpaceMO, SpaceData> spaceInfo = new HashMap();
    private long startTime;
    
    public CacheStats() {
        startTime = System.nanoTime();
    }
    
    void logCellIntersect(SpaceMO space, CellDescription cellDesc) {
        SpaceData data = spaceInfo.get(space);
        if (data==null) {
            data = new SpaceData(space);
            spaceInfo.put(space, data);
        }
        
        data.addCellIntersect(cellDesc);
    }
    
    void report() {
        long totalTime = System.nanoTime() - startTime;
        for(SpaceData d : spaceInfo.values()) {
            d.report();
        }
        System.out.println("Total time = "+totalTime/1000000+" ms.");
    }

    class SpaceData { 
        private ArrayList<CellDescription> cellIntersects = new ArrayList();
        private SpaceID spaceID;
        
        SpaceData(SpaceMO space) {
            spaceID = space.getSpaceID();
        }
        
        void addCellIntersect(CellDescription cellDesc) {
            cellIntersects.add(cellDesc);
        }
        
        void report() {
            System.out.print(spaceID+" : ");
            for(CellDescription desc : cellIntersects) {
                System.out.print(desc.getCellID()+" ");
            }
            System.out.println();
        }
    }
}
