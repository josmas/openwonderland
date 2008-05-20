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

import com.jme.bounding.BoundingBox;
import com.jme.bounding.BoundingSphere;
import com.jme.bounding.BoundingVolume;
import com.jme.math.Vector3f;
import com.sun.sgs.app.AppContext;
import com.sun.sgs.app.ManagedObject;
import com.sun.sgs.app.ManagedReference;
import com.sun.sgs.app.Task;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.wonderland.common.ExperimentalAPI;
import org.jdesktop.wonderland.common.InternalAPI;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.CellTransform;
import org.jdesktop.wonderland.common.cell.MultipleParentException;
import org.jdesktop.wonderland.server.WonderlandContext;
import org.jdesktop.wonderland.server.comms.CommsManager;
import org.jdesktop.wonderland.wfs.WFS;
import org.jdesktop.wonderland.wfs.cell.WFSCellMO;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

/**
 *
 * @author paulby
 */
@ExperimentalAPI
public class CellManagerMO implements ManagedObject, Serializable {

    // Used to generate unique cell ids
    private long cellCounter=0;
    
    private ManagedReference<CellMO> rootCellRef;
    private CellID rootCellID;
    
    private static final String BINDING_NAME=CellManagerMO.class.getName();
    
    
    /**
     * Creates a new instance of CellManagerMO
     */
    CellManagerMO() {
        AppContext.getDataManager().setBinding(BINDING_NAME, this);
        createRootCell();
    }
    
    private void createRootCell() {
        BoundingSphere rootBounds = new BoundingSphere(Float.POSITIVE_INFINITY, new Vector3f());
        CellTransform orig = new CellTransform(null, null);
        CellMO root = new RootCellMO(rootBounds, orig);
        rootCellID = root.getCellID();
        root.setLocalBounds(rootBounds);
        root.setTransform(orig);
        root.setName("root");
        root.setLive(true);       
        
        // Special case for the root cell, ensure the bounds are updated
        BoundsManager.get().cellTransformChanged(rootCellID, orig);  
        rootCellRef = AppContext.getDataManager().createReference(root);
    }
    
    /**
     * Return the root cell id, used by ViewCellCacheMO. Currently we only use
     * a single root on the server, but the client can support multiple roots.
     * @return
     */
    CellID getRootCellID() {
        return rootCellID;
    }
    
    /**
     * Initialize the master cell cache. This is an implementation detail and
     * should not be called by users of this class.
     */
    @InternalAPI
    public static void initialize() {
        new CellManagerMO();
        
        // register the cell channel message listener
        CommsManager cm = WonderlandContext.getCommsManager();
        cm.registerClientHandler(new CellChannelConnectionHandler());
        
        // Register the cell cache message handler
        cm.registerClientHandler(new CellCacheConnectionHandler());        
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
        return (CellMO) AppContext.getDataManager().getBinding("CELL_"+cellID.toString());        
    }
    
    /**
     * Add the cell to the rootCell, which in turn will make cell and all it's
     * children live. If the cell already has a parent a MultipleParentException
     * will be thrown.
     */
    public void addCell(CellMO cell) throws MultipleParentException {
        rootCellRef.getForUpdate().addChild(cell);
    }
    
    /**
     * Return the collection of descriptors of the root cells for the world.
     * 
     * TODO - NOT IMPLEMENTED, always returns an empty set.
     * 
     * @return
     */
    public Collection<CellDescription> getRootCells() {
        throw new NotImplementedException();
    }
    
    /**
     *  Traverse all trees and return the CellDescription for the cells which are within
     * the specified bounds and are of the given Class 
     * 
     * TODO - NOT IMPLEMENTED, always returns an empty set.
     * 
     * @param b the bounds to search inside
     * @param cellClasses the classes of cells to search for
     * @return an array of cell descriptions that provide details about the
     * cells in range
     */
    @InternalAPI
    public Collection<CellDescription> getCells(BoundingVolume b, Class<?>... cellClasses) {
        throw new NotImplementedException();
    }
 
    /**
     * For testing.....
     */
    public void loadWorld() {
        //buildWFSWorld();
        
//        createStaticGrid();       
    }

    /**
     * Builds a world defined by a wonderland file system (e.g. on disk). The
     * world's root directory must be setTranslation in the system property 
     * wonderland.wfs.root
     */
    private void buildWFSWorld() {
        /* Fetch the world root URI, if null, then log an error */
        //URL root = WonderlandServerConfig.getDefault().getWorldRoot();
        URL root = null;
        try {
            root = new URL("file:src/worlds/test-wfs");
        } catch (MalformedURLException excp) {
            WFS.getLogger().log(Level.SEVERE, "Invalid WFS URL");
        }
        
        if (root == null) {
            WFS.getLogger().log(Level.SEVERE, "World Root attribute not setTranslation in server config file.");
            return;
        }

        /*
         * Attempt to create a new MO based upon the WFS root. We need to setup
         * some basic properties about the cell by hand (e.g. transform, name).
         */
        WFSCellMO mo = new WFSCellMO(root);
        mo.setTransform(new CellTransform(null, null, null));
        mo.setName("root");
        mo.setLocalBounds(new BoundingSphere(Float.POSITIVE_INFINITY, new Vector3f()));
        
        try {
            AppContext.getDataManager().setBinding(mo.getBindingName(), mo);
            this.addCell(mo);
        } catch (java.lang.Exception excp) {
            WFS.getLogger().log(Level.SEVERE, "Unable to load WFS into world: " + root.toString());
            WFS.getLogger().log(Level.SEVERE, excp.toString());
            excp.printStackTrace();
        }
    }
    
    /**
     * Creates a bounding box with the specified dimensions,centered at 0,0,0
     */
//    public static BoundingBox createBoundingBox(float xDim, float yDim, float zDim) {
//        BoundingBox cellBounds = new BoundingBox(
//                new Point3d(-xDim/2f, -yDim/2f, -zDim/2f), 
//                new Point3d(xDim/2f, yDim/2f, zDim/2f));
//        return cellBounds;
//    }
    
    
    /**
     * Returns a unique cell id and registers the cell with the system
     * @return
     */
    CellID createCellID(CellMO cell) {
        CellID cellID = new CellID(cellCounter++);
        
        AppContext.getDataManager().setBinding("CELL_"+cellID.toString(), cell);
        
        return cellID;
    }
    
//    /**
//     * Called by a cell when it's localBounds are changed
//     * @param cell
//     */
//    abstract void cellLocalBoundsChanged(CellMO cell);
//    
//    /**
//     * Called by a cell when it's transform is changed
//     * @param cell
//     */
//    abstract void cellTransformChanged(CellMO cell);
//    
//    /**
//     * Called when a child is added to a parent cell
//     * @param parent
//     * @param childAdded true when a child is added, false when child is removed
//     */
//    abstract void cellChildrenChanged(CellMO parent, CellMO child, boolean childAdded);
}