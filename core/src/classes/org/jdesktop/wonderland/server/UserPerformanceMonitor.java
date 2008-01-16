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
package org.jdesktop.wonderland.server;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Collect user performance metrics
 * 
 * @author paulby
 */

public class UserPerformanceMonitor {

    private long revalidateCellGetTime = 0;
    private long revalidateBoundsCalcTime = 0;
    private int revalidateCellCount = 0;
    
    private final Map<Class,CellClassStats> cellClassStats =
            new TreeMap<Class, CellClassStats>(new Comparator<Class>() {
        public int compare(Class arg0, Class arg1) {
            return arg0.getName().compareTo(arg1.getName());
        } 
    });
    
    public UserPerformanceMonitor() {
    }
    
    public void resetCacheRevalidationCounters() {
        revalidateCellCount = 0;
        revalidateCellGetTime = 0;
        revalidateBoundsCalcTime = 0;
        
        cellClassStats.clear();
    }
    
    public String getRevalidateStats() {
        String stats = "Cell Count "+revalidateCellCount+
                "  Bounds calc time "+revalidateBoundsCalcTime/1000+" us." +
                "  Sgs get object time "+revalidateCellGetTime/1000+" us.\n";
        
        for (Map.Entry<Class, CellClassStats> me : cellClassStats.entrySet()) {
            CellClassStats ccs = me.getValue();
            long incTime = ccs.incTime.get() / 1000;
            stats += me.getKey() + " :" +
                    "  Total time: " + incTime + " us." +
                    "  Count: " + ccs.incCount.get() + "." +
                    "  Average: " + incTime/ccs.incCount.get() + " us.\n";
        }
        
        return stats;
    }
    
    /**
     *  Increment the calc time by specified number of nano seconds
     */
    public void incRevalidateCalcTime(long incNanoSeconds) {
        revalidateBoundsCalcTime+=incNanoSeconds;
    }
    
    public void incRevalidateCellGetTime(Class c, long incNanoSeconds) {
        revalidateCellGetTime+=incNanoSeconds;
    
        CellClassStats stats = getCellClassStats(c);
        stats.incTime.addAndGet(incNanoSeconds);
    }
    
    public void incRevalidateCellCount(Class c) {
        revalidateCellCount++;
        
        CellClassStats stats = getCellClassStats(c);
        stats.incCount.incrementAndGet();
    }
    
    private final CellClassStats getCellClassStats(Class c) {
        CellClassStats out = cellClassStats.get(c);
        if (out == null) {
            out = new CellClassStats();
            out.incCount = new AtomicInteger();
            out.incTime = new AtomicLong();
            
            cellClassStats.put(c, out);
        }
        
        return out;
    }
    
    class CellClassStats {
        AtomicLong incTime;
        AtomicInteger incCount;
    }
}
