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
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.wonderland.ExperimentalAPI;
import org.jdesktop.wonderland.InternalAPI;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.CellTransform;
import org.jdesktop.wonderland.common.cell.ClientCapabilities;
import org.jdesktop.wonderland.common.cell.MultipleParentException;
import org.jdesktop.wonderland.common.cell.messages.CellHierarchyMessage;
import org.jdesktop.wonderland.common.cell.messages.CellHierarchyMoveMessage;
import org.jdesktop.wonderland.common.cell.messages.CellHierarchyUnloadMessage;
import org.jdesktop.wonderland.server.WonderlandContext;
import org.jdesktop.wonderland.server.comms.CommsManager;

/**
 *
 * @author paulby
 */
@ExperimentalAPI
public class CellManager implements ManagedObject, Serializable {

    // Used to generate unique cell ids
    private long cellCounter=0;
    
    private ManagedReference<CellMO> rootCellRef;
    private CellID rootCellID;
    
    private static final String BINDING_NAME=CellManager.class.getName();
    
    
    /**
     * Creates a new instance of CellManager
     */
    CellManager() {
        AppContext.getDataManager().setBinding(BINDING_NAME, this);
        createRootCell();
    }
    
    private void createRootCell() {
        CellMO root = new CellMO();
        rootCellID = root.getCellID();
        BoundingSphere rootBounds = new BoundingSphere(Float.POSITIVE_INFINITY, new Vector3f());
        root.setLocalBounds(rootBounds);
        CellTransform orig = new CellTransform(null, null);
        root.setTransform(orig);
        root.setName("root");
        root.setLive(true);       
        
        // Special case for the root cell, ensure the bounds are updated
        BoundsManager.get().cellTransformChanged(rootCellID, orig);  
        rootCellRef = AppContext.getDataManager().createReference(root);
    }
    
    /**
     * Return the root cell id, used by AvatarCellCacheMO
     * @return
     */
    CellID getRootCellID() {
        return rootCellID;
    }
    
    /**
     * Initialize the master cell cache
     */
    public static void initialize() {
        new CellManager();
        
        // register the cell message listener
        CommsManager cm = WonderlandContext.getCommsManager();
        cm.registerClientHandler(new CellChannelHandlerMO());
        
        // Register the cell cache message handler
        cm.registerClientHandler(new CellCacheClientHandler());
        
        // Register the avatar message handler
        cm.registerClientHandler(new ViewHandlerMO());
    }
    
    /**
     * Return singleton master cell cache
     * @return the master cell cache
     */
    public static CellManager getCellManager() {
        return (CellManager) AppContext.getDataManager().getBinding(BINDING_NAME);                
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
    public CellDescription[] getCells(BoundingVolume b, Class<?>... cellClasses) {
        return new CellDescription[0];
    }

    /**
     * Create a static grid of nodes
     */
    private void createStaticGrid() {
        int gridWidth = 10;
        int gridDepth = 10;
        
        float boundsDim = 5;
        BoundingBox gridBounds = new BoundingBox(new Vector3f(), boundsDim, boundsDim, boundsDim);
        
        for(int x=0; x<gridWidth; x++) {
            for(int z=0; z<gridDepth; z++) {
                try {
                    CellMO cell = new SimpleTerrainCellMO();
                    cell.setTransform(new CellTransform(null, new Vector3f(x * boundsDim*2, 0, z * boundsDim*2)));
                    cell.setName("grid_" + x + "_" + z);
                    cell.setLocalBounds(gridBounds);
                    addCell(cell);
                } catch (MultipleParentException ex) {
                    Logger.getLogger(CellManager.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
                
    }
    
    
    /**
     * For testing.....
     */
    public void loadWorld() {
//        createStaticGrid();
        
        try {
            BoundingBox bounds = new BoundingBox(new Vector3f(), 1, 1, 1);

            CellMO c1 = new EntityCellMO();
            c1.setTransform(new CellTransform(null, new Vector3f(1,1,1)));
            c1.setName("c1");
            c1.setLocalBounds(bounds);

            EntityCellMO c2 = new EntityCellMO();
            c2.setTransform(new CellTransform(null, new Vector3f(10,10,10)));
            c2.setName("c2");
            c2.setLocalBounds(bounds);

            EntityCellMO c3 = new EntityCellMO();
            c3.setTransform(new CellTransform(null, new Vector3f(5,5,5)));
            c3.setName("c3");
            c3.setLocalBounds(new BoundingSphere(2, new Vector3f()));

            CellMO c4 = new EntityCellMO();
            c4.setTransform(new CellTransform(null, new Vector3f(0,0,0)));
            c4.setName("c4");
            c4.setLocalBounds(new BoundingSphere(0.5f, new Vector3f()));
            
            c3.addChild(c4);
            
            c1.addChild(c2);
            c1.addChild(c3);
            addCell(c1);
            
            Task t = new TestTask(c3, c2);
            
            AppContext.getTaskManager().schedulePeriodicTask(t, 5000, 1000);
            
            RevalidatePerformanceMonitor monitor = new RevalidatePerformanceMonitor();
            BoundingVolume visBounds = new BoundingSphere(5, new Vector3f());
            
//            for(CellID cellID : getCell(getRootCellID()).getVisibleCells(visBounds, monitor)) {
//                System.out.println(cellID);
//            }
            
            // Octtree test
//            Matrix4d centerTransform = new Matrix4d();
//            centerTransform.setIdentity();
//            float size = 1000;
//            OctTreeCellMO oct = new OctTreeCellMO(
//                    createBoundingBox(size, size, size), 
//                    centerTransform);
//            addCell(oct);          
//            
//            final CellMO test = new CellMO();
//            test.setLocalBounds(createBoundingBox(50,50,50));
//            test.setTransform(createTransform(375,375,-375));
//            
//            Bounds cellVWBounds = test.getLocalBounds();
//            Matrix4d m4d = test.getTransform();
//            cellVWBounds.transform(new Transform3D(m4d));
//            CellMO parent = oct.insertCellInHierarchy(test, cellVWBounds);
//            System.out.println("Got parent "+parent);
//            if (parent==null) {
//                System.out.println("FAILED TO LOCATE PARENT");
//            } 
//            
//            Task t = new TestTask(test);
//            
//            AppContext.getTaskManager().schedulePeriodicTask(t, 5000, 1000);
            
        } catch (Exception ex) {
            Logger.getLogger(CellManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * Builds a world defined by a wonderland file system (e.g. on disk). The
     * world's root directory must be setTranslation in the system property 
     * wonderland.wfs.root
     */
//    private void buildWFSWorld() {
//        /* Fetch the world root URI, if null, then log an error */
//        URL root = WonderlandServerConfig.getDefault().getWorldRoot();
//        if (root == null) {
//            WFS.getLogger().log(Level.SEVERE, "World Root attribute not setTranslation in server config file.");
//            return;
//        }
//
//        /* Attempt to create a new GLO based upon the WFS root, if not log an error */
//        WFSCellMO glo = new WFSCellMO(root);
//        try {
//            AppContext.getDataManager().setBinding(glo.getBindingName(), glo);
//            this.addCell(glo);
//        } catch (java.lang.Exception excp) {
//            WFS.getLogger().log(Level.SEVERE, "Unable to load WFS into world: " + root.toString());
//            WFS.getLogger().log(Level.SEVERE, excp.toString());
//        }
//    }

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

    static class TestTask implements Task, Serializable {
            private ManagedReference<EntityCellMO> cellRef;
            private Vector3f pos;
            private Vector3f pos2;
            private int dir = 2;
            private ManagedReference<EntityCellMO> cell2Ref;
            
            public TestTask(EntityCellMO cell, EntityCellMO c2) {
                this.cellRef = AppContext.getDataManager().createReference(cell);
                this.cell2Ref = AppContext.getDataManager().createReference(c2);
                pos = cell.getTransform().getTranslation(null);
                pos2 = cell.getTransform().getTranslation(null);
            }
            

            public void run() throws Exception {
                pos.x += dir;
                pos2.z += dir;
                if (pos.x > 40 || pos.x<2)
                    dir = -dir;
                cellRef.get().setTransform(new CellTransform(null, pos));
                cell2Ref.get().setTransform(new CellTransform(null, pos2));
            }
    
    }
    
    /**
     * Return a new Create cell message
     */
    public static CellHierarchyMessage newCreateCellMessage(CellMO cell, ClientCapabilities capabilities) {
        CellID parent=null;
        
        CellMO p = cell.getParent();
        if (p!=null) {
            parent = p.getCellID();
        }
        
        return new CellHierarchyMessage(CellHierarchyMessage.ActionType.LOAD_CELL,
            cell.getClientCellClassName(capabilities),
            cell.getLocalBounds(),
            cell.getCellID(),
            parent,
            cell.getTransform(),
            cell.getClientSetupData(capabilities)
            );
    }
    
    /**
     * Return a new LoadLocalAvatar cell message
     */
    public static CellHierarchyMessage newLoadLocalAvatarMessage(AvatarMO cell, ClientCapabilities capabilities) {
        CellID parent=null;
        
        CellMO p = cell.getParent();
        if (p!=null) {
            parent = p.getCellID();
        }
        
        return new CellHierarchyMessage(CellHierarchyMessage.ActionType.LOAD_CLIENT_AVATAR,
            cell.getClientCellClassName(capabilities),
            cell.getLocalBounds(),
            cell.getCellID(),
            parent,
            cell.getTransform(),
            cell.getClientSetupData(capabilities)
            );
    }
    
    /**
     * Return a new Cell inactive message
     */
    public static CellHierarchyUnloadMessage newUnloadCellMessage(CellMO cell) {
        return new CellHierarchyUnloadMessage(cell.getCellID());
    }
    
    /**
     * Return a new Delete cell message
     */
    public static CellHierarchyMessage newDeleteCellMessage(CellID cellID) {
        return new CellHierarchyMessage(CellHierarchyMessage.ActionType.DELETE_CELL,
            null,
            null,
            cellID,
            null,
            null,
            null
            );
    }
    /**
     * Return a new set world root cell message
     */
    public static CellHierarchyMessage newRootCellMessage(CellMO cell) {
        return new CellHierarchyMessage(CellHierarchyMessage.ActionType.SET_WORLD_ROOT,
            null,
            null,
            cell.getCellID(),
            null,
            null,
            null
            );
    }
    
    /**
     * Return a new Delete cell message
     */
    public static CellHierarchyMessage newChangeParentCellMessage(CellMO childCell, CellMO parentCell) {
        return new CellHierarchyMessage(CellHierarchyMessage.ActionType.CHANGE_PARENT,
            null,
            null,
            childCell.getCellID(),
            parentCell.getCellID(),
            null,
            null
            
            );
    }
    
    /**
     * Return a new cell move message
     */
    public static CellHierarchyMessage newCellMoveMessage(CellDescription cell) {
        return new CellHierarchyMoveMessage(cell.getLocalBounds(),
            cell.getCellID(),
            cell.getTransform()
            );
    }
    
    /**
     * Return a new cell update message. Indicates that the content of the cell
     * has changed.
     */
    public static CellHierarchyMessage newContentUpdateCellMessage(CellMO cellGLO, ClientCapabilities capabilities) {
        CellID parentID = null;
        if (cellGLO.getParent() != null) {
            parentID = cellGLO.getParent().getCellID();
        }
        
        /* Return a new CellHiearchyMessage class, with populated data fields */
        return new CellHierarchyMessage(CellHierarchyMessage.ActionType.UPDATE_CELL_CONTENT,
            cellGLO.getClientCellClassName(capabilities),
            cellGLO.getLocalBounds(),
            cellGLO.getCellID(),
            parentID,
            cellGLO.getTransform(),
            cellGLO.getClientSetupData(capabilities)
            
            
            
            );
    }
}
