/**
 * Project Wonderland
 *
 * Copyright (c) 2004-2009, Sun Microsystems, Inc., All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * Sun designates this particular file as subject to the "Classpath" 
 * exception as provided by Sun in the License file that accompanied 
 * this code.
 */
package org.jdesktop.wonderland.tools.wfs.test;

import java.net.URL;
import org.jdesktop.wonderland.tools.wfs.*;

/**
 *
 * @author jordanslott
 */
public class WFSTest {
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
