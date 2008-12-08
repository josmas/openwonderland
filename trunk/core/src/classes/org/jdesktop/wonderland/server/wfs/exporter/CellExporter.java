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

package org.jdesktop.wonderland.server.wfs.exporter;

import com.sun.sgs.app.ManagedReference;
import java.io.StringWriter;
import org.jdesktop.wonderland.server.wfs.importer.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.setup.BasicCellSetup;
import org.jdesktop.wonderland.common.wfs.CellDescriptor;
import org.jdesktop.wonderland.common.wfs.CellPath;
import org.jdesktop.wonderland.common.wfs.WorldRoot;
import org.jdesktop.wonderland.server.cell.CellMO;
import org.jdesktop.wonderland.server.cell.CellManagerMO;


/**
 * The CellImporter class is responsible for loading a WFS from the HTTP-based
 * WFS service.
 * 
 * @author Jordan Slott <jslott@dev.java.net>
 */
public class CellExporter {
    /* The logger for the wfs loader */
    private static final Logger logger = Logger.getLogger(CellExporter.class.getName());
    
    /* The URL for the web server for this loader instance */
    private URL webServerURL = null;
    
    /** Default Constructor */
    public CellExporter() {
        try {
            this.webServerURL = CellImporterUtils.getWebServerURL();
        } catch (MalformedURLException excp) {
            logger.log(Level.WARNING, "[WFS] No web server URL", excp);
        }
    }
    
    public void export() {
        // Get started, fetch the cell manager and the set of root cells
        CellManagerMO cellManagerMO = CellManagerMO.getCellManager();
        Set<CellID> rootCells = cellManagerMO.getRootCells();
        
        // Create a new snapshot, keep the WorldRoot reference around
        WorldRoot worldRoot = CellExporterUtils.createSnapshot();
        if (worldRoot == null) {
            return;
        }
        
        // Iterate through the cells and write them out to the server
        Iterator<CellID> it = rootCells.iterator();
        while (it.hasNext() == true) {
            CellID cellID = it.next();
            CellMO cellMO = CellManagerMO.getCell(cellID);
            writeCell(worldRoot, null, cellMO);
        }
    }
    
    /**
     * Recursively write out this cell to the server and all of its children
     */
    private void writeCell(WorldRoot worldRoot, CellPath parentPath, CellMO cellMO) {
        // Create the cell on the server, fetch the setup information from the
        // cell. If the cell does not return a valid setup object, then simply
        // ignore the cell (and its children).
        String cellName = cellMO.getName();
        BasicCellSetup setup = cellMO.getCellSetup(null);
        if (setup == null) {
            return;
        }
        
        // Write the setup information as an XML string. If we have trouble
        // writing this, then punt.
        String setupStr = null;
        try {
            StringWriter sw = new StringWriter();
            setup.encode(sw);
            setupStr = sw.toString();
        } catch (java.lang.Exception excp) {
            logger.log(Level.WARNING, "[WFS] Failed to encode cell " + cellName +
                    " in WFS " + worldRoot.toString(), excp);
            return;
        }
        
        // Create the descriptor for the cell using the world root, path of the
        // parent, name of the cell and setup information we obtained from the
        // cell
        CellDescriptor cellDescriptor = new CellDescriptor(worldRoot,
                parentPath, cellName, setupStr);
        
        // Ask the server to create the cell
        try {
            StringWriter sw = new StringWriter();
            cellDescriptor.encode(sw);
            CellExporterUtils.createCell(sw.toString());
        } catch (java.lang.Exception excp) {
            logger.log(Level.WARNING, "[WFS] Failed to write cell " + cellName +
                    " in WFS " + worldRoot.toString(), excp);
            return;
        }
        
        // Form the new parent path for this cell to pass down. If this parent
        // is at the root, then parentPath is null, so we need to check for this
        CellPath thisPath = (parentPath != null) ?
            parentPath.getChildPath(cellName) : new CellPath(cellName);
        
        // Iterate through all of the child cells of this cell and recursively
        // write them out to the server.
        Collection<ManagedReference<CellMO>> cellRefs = cellMO.getAllChildrenRefs();
        Iterator<ManagedReference<CellMO>> it = cellRefs.iterator();
        while (it.hasNext() == true) {
           CellMO childCellMO = it.next().get();
           writeCell(worldRoot, thisPath, childCellMO);
        }           
    }
}
