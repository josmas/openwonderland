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
package org.jdesktop.wonderland.server.cell;

import com.sun.sgs.app.AppContext;
import com.sun.sgs.app.ManagedObject;
import com.sun.sgs.app.ManagedReference;
import com.sun.sgs.app.NameNotBoundException;
import com.sun.sgs.app.util.ScalableHashSet;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;
import org.jdesktop.wonderland.common.ExperimentalAPI;
import org.jdesktop.wonderland.common.InternalAPI;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.MultipleParentException;
import org.jdesktop.wonderland.server.WonderlandContext;
import org.jdesktop.wonderland.server.comms.CommsManager;
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

    private Set<Class<? extends CellComponentMO>> avatarComponents = null;

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
        if (cellID.equals(CellID.getInvalidCellID()))
            return null;

        try {
            return (CellMO) AppContext.getDataManager().getBinding(getCellBinding(cellID));
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
        new CellExporter().export(null);
    }
    
    /**
     * Returns a unique cell id and registers the cell with the system
     * @return
     */
    CellID createCellID(CellMO cell) {
        CellID cellID = new CellID(cellCounter++);
        AppContext.getDataManager().setBinding(getCellBinding(cellID), cell);
        return cellID;
    }

    /**
     * Return a unique name for a cell, given its ID
     * @param ID the cell's ID
     */
    static String getCellBinding(CellID cellID) {
        return "CELL_" + cellID.toString();
    }

    /**
     * Register a component that will be added to avatar cells
     * 
     * @param component
     */
    public void registerAvatarCellComponent(Class<? extends CellComponentMO> componentClass) {
        if (avatarComponents==null)
            avatarComponents = new HashSet();

        avatarComponents.add(componentClass);
    }

    Iterable<Class<? extends CellComponentMO>> getAvatarCellComponentClasses() {
        if (avatarComponents==null)
            return null;
        else
            return avatarComponents;
    }
}
