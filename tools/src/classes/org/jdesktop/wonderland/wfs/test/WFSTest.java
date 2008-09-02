/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.wfs.test;

import java.net.URL;
import org.jdesktop.wonderland.wfs.*;

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
