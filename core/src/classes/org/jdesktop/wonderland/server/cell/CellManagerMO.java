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
import com.sun.sgs.app.ManagedObject;
import com.sun.sgs.app.ManagedReference;
import com.sun.sgs.app.NameNotBoundException;
import com.sun.sgs.app.util.ScalableHashSet;
import java.io.Serializable;
import java.util.Set;
import java.util.logging.Logger;
import org.jdesktop.wonderland.common.ExperimentalAPI;
import org.jdesktop.wonderland.common.InternalAPI;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.MultipleParentException;
import org.jdesktop.wonderland.server.WonderlandContext;
import org.jdesktop.wonderland.server.comms.CommsManager;
import org.jdesktop.wonderland.server.spatial.UniverseManager;
import org.jdesktop.wonderland.server.spatial.UniverseManagerFactory;
import org.jdesktop.wonderland.server.wfs.exporter.CellExporter;
import org.jdesktop.wonderland.server.wfs.importer.CellImporter;

/**
 *
 * @author paulby
 */
@ExperimentalAPI
public class CellManagerMO implements ManagedObject, Serializable {

    // Used to generate unique cell ids
    private long cellCounter=CellID.getFirstCellID();
    
    private static final String BINDING_NAME=CellManagerMO.class.getName();
    private static final Logger logger = Logger.getLogger(CellManagerMO.class.getName());

    private ManagedReference<ScalableHashSet<CellID>> rootCellsRef = null;

    /**
     * Creates a new instance of CellManagerMO
     */
    CellManagerMO() {
        AppContext.getDataManager().setBinding(BINDING_NAME, this);

        // create the map of root cells
        ScalableHashSet<CellID> rootCells = new ScalableHashSet();
        rootCellsRef = AppContext.getDataManager().createReference(rootCells);
    }
    
    /**
     * Initialize the master cell cache. This is an implementation detail and
     * should not be called by users of this class.
     */
    @InternalAPI
    public static void initialize() {
        logger.fine("CellManagerMO Initializing");

        // create a new manager.  This only happens during a cold start (when
        // there is no Darkstar database).
        // In any other case, this step is skipped and reinitialize() is
        // called instead to load the cells in the world
        new CellManagerMO();

        // register the cell channel message listener
        CommsManager cm = WonderlandContext.getCommsManager();
        cm.registerClientHandler(new CellChannelConnectionHandler());
        
        // Register the cell cache message handler
        cm.registerClientHandler(new CellCacheConnectionHandler());
        
        // Register the cell hierarchy edit message handler
        cm.registerClientHandler(new CellEditConnectionHandler());
    }

    /**
     * Reinitialize the master cell cache. This is an implementation detail and
     * should not be called by users of this class.
     */
    @InternalAPI
    public static void reinitialize(UniverseManager universe) {
        logger.warning("CellManagerMO Reinitializing");

        try {
            getCellManager().reloadCells(universe);
        } catch (NameNotBoundException nnbe) {
            // if the cell manager has not been initialized, the name
            // will be unbound.  Ignore the error here, because it
            // means initialize() will be called later.
        }
    }

    /**
     * Return singleton master cell cache
     * @return the master cell cache
     */
    public static CellManagerMO getCellManager() {
        return (CellManagerMO) AppContext.getDataManager().getBinding(BINDING_NAME);                
    }
    
    /**
     * Return the cell with the given ID, or null if the id is invalid
     * 
     * @param cellID the cell ID to getTranslation
     * @return the cell with the given ID
     */
    public static CellMO getCell(CellID cellID) {
        try {
            return (CellMO) AppContext.getDataManager().getBinding("CELL_"+cellID.toString()); 
        } catch(NameNotBoundException e) {
            return null;
        }
    }
    
    /**
     * Insert the cell into the world. 
     */
    public void insertCellInWorld(CellMO cell) throws MultipleParentException {
        cell.setLive(true);
        UniverseManagerFactory.getUniverseManager().addRootToUniverse(cell);
        rootCellsRef.getForUpdate().add(cell.cellID);
    }

    /**
     * Remove a cell from the world
     * @param cell the cell to remove
     */
    public void removeCellFromWorld(CellMO cell) {
        UniverseManagerFactory.getUniverseManager().removeRootFromUniverse(cell);
        cell.setLive(false);
        rootCellsRef.getForUpdate().remove(cell.getCellID());
    }

    /**
     * Get the list of all root cells in the world
     * @return a list of root cells
     */
    public Set<CellID> getRootCells() {
        return rootCellsRef.get();
    }

    /**
     * Load the initial world.  This will typically load the cells
     * from WFS
     */
    public void loadWorld() {
        new CellImporter().load();
    }

    public void saveWorld() {
        new CellExporter().export();
    }
    
    /**
     * Reload all root cells into the universe, based on the set of root
     * cells we can find. Be sure to only do this once, during the initial
     * (untimed) Darkstar transaction
     */
    void reloadCells(UniverseManager universe) {
        // get all the root cells
        Set<CellID> rootCellIDs = getRootCells();

        int addedCount = 0;
        int errorCount = 0;

        for (CellID rootCellID : rootCellIDs) {
            CellMO cell = CellManagerMO.getCell(rootCellID);
            if (cell == null) {
                logger.warning("Removing non-existant cell " + rootCellID);
                AppContext.getDataManager().markForUpdate(rootCellIDs);
                rootCellIDs.remove(cell);
                errorCount++;
                continue;
            }

            doInsert(cell, universe);
            universe.addRootToUniverse(cell);
            addedCount++;
        }

        logger.info("Added " + addedCount + " cells. " +
                    errorCount + " errors.");
    }

    /**
     * Insert a cell and all of its children into the universe
     * @param cell the cell to insert
     */
    void doInsert(CellMO cell, UniverseManager universe) {
        if (!cell.isLive()) {
            return;
        }

        // add this cell to the universe
        cell.addToUniverse(universe);

        // now update all children
        for (ManagedReference<CellMO> childRef : cell.getAllChildrenRefs()) {
            doInsert(childRef.get(), universe);
        }
    }
  
    /**
     * Returns a unique cell id and registers the cell with the system
     * @return
     */
    CellID createCellID(CellMO cell) {
        CellID cellID = new CellID(cellCounter++);
        
        AppContext.getDataManager().setBinding("CELL_"+cellID.toString(), cell);
        
        return cellID;
    }
    
}
