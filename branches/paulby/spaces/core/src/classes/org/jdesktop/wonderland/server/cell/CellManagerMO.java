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
    private static final Logger logger = Logger.getLogger(CellManagerMO.class.getName());
    
    
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
        root.setName("root");
        root.setLive(true);       
        
        // Special case for the root cell, ensure the bounds are updated
//        BoundsManager.get().cellTransformChanged(rootCellID, orig);  
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
     * Insert the cell into the most appropriate location in the graph. For static
     * cells this is the first leaf space that encloses the origin of cell, for
     * movable cells they are placed at the root of the movable graph.
     */
    public void insertCellInGraph(CellMO cell) throws MultipleParentException {
        if (cell.getComponent(MovableComponentMO.class)!=null) {
            // Movable Cell
            rootCellRef.getForUpdate().addChild(cell);
            SpaceCellMO space = findEnclosingSpace(rootCellRef.get(), cell.getLocalTransform().getTranslation(null));
            if (space==null) {
                logger.severe("Unable to find space to contain cell at "+cell.getLocalTransform().getTranslation(null) +" aborting addCell");
                return;
            }
            System.out.println("Cell "+cell.getLocalTransform().getTranslation(null)+"  added to space "+space);
            CellTransform transform = cell.getLocalTransform();
            transform.sub(space.getLocalTransform());
            cell.addToSpace(space);
        } else {
            // Static cell
            rootCellRef.getForUpdate().addChild(cell);
        }
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
     * Create a static grid of space nodes
     */
    private void createSpaces() {
        int gridWidth = 50;
        int gridDepth = 50;
        int gridHeight = 1;
        
        float boundsDim = 10;
        
        // The spaces must overlap slightly so that the view does not land between 2 spaces
        float fudge = 1.00001f;
        BoundingBox gridBounds = new BoundingBox(new Vector3f(), boundsDim*fudge, boundsDim*fudge, boundsDim*fudge);
        
        SpaceCellMO[][][] gridCells = new SpaceCellMO[gridWidth][gridHeight][gridDepth];
        
        CellMO rootCell = rootCellRef.getForUpdate();
        
        for(int x=0; x<gridWidth; x++) {
            for(int y=0; y<gridHeight; y++) {
                for(int z=0; z<gridDepth; z++) {
                    try {
                        SpaceCellMO cell = new SpaceCellMO(gridBounds, new CellTransform(null, new Vector3f(x * boundsDim*2, y*boundsDim*2, z * boundsDim*2) ));
                        cell.setName("space_" + x + "_" +y +"_"+ z);
                        insertCellInGraph(cell);

                        gridCells[x][y][z] = cell;

                        rootCell.addToSpace(cell);
                    } catch (MultipleParentException ex) {
                        Logger.getLogger(CellManagerMO.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        }
                
        int xzMaxDist = 2;
        int yMaxDist = 1;
        for(int x=0; x<gridWidth; x++) {
            for(int y=0; y<gridHeight; y++) {
                for(int z=0; z<gridDepth; z++) {
                        SpaceCellMO cell = gridCells[x][y][z];
                        for(int xDist=0; xDist<xzMaxDist; xDist++) {
                            for(int yDist=0; yDist<yMaxDist; yDist++) {
                                for(int zDist=0; zDist<xzMaxDist; zDist++) {
                                    if (!(xDist==0 && yDist==0 && zDist==0)) {
                                        if ((x-xDist>=0) &&
                                            (y-yDist>=0) &&
                                            (z-zDist>=0) ) {
                                                gridCells[x-xDist][y-yDist][z-zDist].addProximitySpace(cell);
                                                cell.addProximitySpace(gridCells[x-xDist][y-yDist][z-zDist]);
                                        }

                                        if ((x+xDist<gridWidth) &&
                                            (y+yDist<gridHeight) &&
                                            (z+zDist<gridDepth) ) {
                                                gridCells[x+xDist][y+yDist][z+zDist].addProximitySpace(cell);
                                                cell.addProximitySpace(gridCells[x+xDist][y+yDist][z+zDist]);                                
                                        }
                                    }
                                   
                                }
                            }
                        }                
                }
            }
        }
    }
    
    /**
     * Find the deepest space that contains the point
     * @param point
     * @return
     */
    private SpaceCellMO findEnclosingSpace(CellMO root, Vector3f point) {
        SpaceCellMO ret = null;
        
        Collection<ManagedReference<CellMO>> childrenRefs = root.getAllChildrenRefs();
        
        if (childrenRefs!=null) {
            for(ManagedReference<CellMO> childRef : childrenRefs) {
                ret = findEnclosingSpace(childRef.get(), point);
                if (ret!=null)
                    break;
            }
        }
        
        if (ret==null && root instanceof SpaceCellMO) {
//            System.out.println("Checking space "+root.getCachedVWBounds());
            if (root.getWorldBounds().contains(point)) {
                return (SpaceCellMO)root;
            }
        }
        
        
        return ret;
    }
    
    /**
     * Find the deepest (closest to a leaf) space that contains the point
     * @param point
     * @return
     */
    SpaceCellMO findEnclosingSpace(Vector3f point) {
        return findEnclosingSpace(rootCellRef.get(), point);
    }
    
    
    /**
     * For testing.....
     */
    public void loadWorld() {
        //buildWFSWorld();
        
        createSpaces();

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
        mo.setLocalTransform(new CellTransform(null, null, null));
        mo.setName("root");
        mo.setLocalBounds(new BoundingSphere(Float.POSITIVE_INFINITY, new Vector3f()));
        
        try {
            AppContext.getDataManager().setBinding(mo.getBindingName(), mo);
            this.insertCellInGraph(mo);
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
    
}
