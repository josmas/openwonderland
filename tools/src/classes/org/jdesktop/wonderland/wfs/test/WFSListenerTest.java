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

package org.jdesktop.wonderland.wfs.test;

import java.net.URL;
import org.jdesktop.wonderland.wfs.WFS;
import org.jdesktop.wonderland.wfs.WFSCell;
import org.jdesktop.wonderland.wfs.WFSCellDirectory;
import org.jdesktop.wonderland.wfs.WFSFactory;
import org.jdesktop.wonderland.wfs.event.WFSEvent;
import org.jdesktop.wonderland.wfs.event.WFSListener;

/**
 * Tests the listener mechanism in the WFS API.
 * 
 * @author Jordan Slott <jslott@dev.java.net>
 */
public class WFSListenerTest implements WFSListener {
    /**
     * Called when the attribute file of a cell has changed in memory. If the
     * cell's attributes have not changed, but a thread calls WFSCell.setCellSetupUpdate(),
     * then this method will be called at that time.
     * 
     * @param cellEvent The WFSCellEvent object
     */
    public void cellAttributeUpdate(WFSEvent event) {
        System.out.println("EVENT: Cell Attribute Update: " + event.getWFSCell().getCellName());
    }
    
    /**
     * Called when children have been added to this cell.
     * 
     * @param cellEvent The WFSCellEvent object
     */
    public void cellChildrenAdded(WFSEvent event) {
        System.out.println("EVENT: Cell Children Added: " + event.getWFSCell().getCellName());
    }
    
    /**
     * Called when children have been removed from this cell.
     * 
     * @param cellEvent The WFSCellEvent object
     */
    public void cellChildrenRemoved(WFSEvent event) {
        System.out.println("EVENT: Cell Children Removed: " + event.getWFSCell().getCellName());
    }
    
    /**
     * Called when this cell has been removed from the hierarchy.
     * 
     * @param cellEvent The WFSCellEvent object
     */
    public void cellRemoved(WFSEvent event) {
        System.out.println("EVENT: Cell Removed: " + event.getWFSCell().getCellName());
    }
    
    public static void main(String[] args) {
        try {
            WFS wfs = WFSFactory.open(new URL("file:///Users/jordanslott/wonderland/wfs/default-wfs"));
            
            WFSCellDirectory dir = wfs.getRootDirectory();
            WFSCell cell = dir.getCellByName("building");
            wfs.addWFSListener(new WFSListenerTest());
            String setup = cell.getCellSetup();
            cell.setCellSetup(setup);
            WFSCell newcell = cell.createCellDirectory().addCell("fubar");
            WFSCell newcell2 = cell.getCellDirectory().addCell("fubar2");
            cell.getCellDirectory().removeCell(newcell);
        } catch (Exception excp) {
            excp.printStackTrace();
        }      
    }
}
