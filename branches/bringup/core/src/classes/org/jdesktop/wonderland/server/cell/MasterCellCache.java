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
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.CellTransform;
import org.jdesktop.wonderland.common.cell.MultipleParentException;
import org.jdesktop.wonderland.common.cell.messages.CellHierarchyMessage;
import org.jdesktop.wonderland.common.cell.messages.CellMessage;
import org.jdesktop.wonderland.server.UserPerformanceMonitor;
import org.jdesktop.wonderland.server.WonderlandContext;
import org.jdesktop.wonderland.server.comms.CommsManager;

/**
 *
 * @author paulby
 */
@ExperimentalAPI
public class MasterCellCache implements ManagedObject, Serializable {

    // Used to generate unique cell ids
    private long cellCounter=0;
    private ManagedReference rootCellRef;
    private CellID rootCellID;
    
    private static final String BINDING_NAME=MasterCellCache.class.getName();
    
    
    /**
     * Creates a new instance of MasterCellCache
     */
    MasterCellCache() {
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
        root.setLocalToVWorld(orig);
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
     *
     * @return
     */
    public static void initialize() {
        new MasterCellCache();
        
        // register the cell message listener
        CommsManager cm = WonderlandContext.getCommsManager();
        cm.registerClientHandler(new CellClientHandler());
        
        // Register the cell cache message handler
        cm.registerClientHandler(new CellCacheClientHandler());
    }
    
    /**
     * Return singleton master cell cache
     * @return
     */
    public static MasterCellCache getMasterCellCache() {
        return AppContext.getDataManager().getBinding(BINDING_NAME, MasterCellCache.class);                
    }
    

    /**
     * Return the cell with the given ID, or null if the id is invalid
     * 
     * @param cellID
     * @return
     */
    public static CellMO getCell(CellID cellID) {
        return AppContext.getDataManager().getBinding("CELL_"+cellID.toString(), CellMO.class);        
    }
    
    /**
     * Add the cell to the rootCell, which in turn will make cell and all it's
     * children live. If the cell already has a parent a MultipleParentException
     * will be thrown.
     */
    public void addCell(CellMO cell) throws MultipleParentException {
        rootCellRef.getForUpdate(CellMO.class).addChild(cell);
    }
    
    /**
     *  Traverse all trees and return the set of cells which are within
     * the specified bounds and are of the give Class 
     * 
     * @param b
     * @param cellClasses
     * @return
     */
    public CellDescription[] getCells(BoundingVolume b, Class[] cellClasses) {
        return new CellDescription[0];
    }

    /**
     * For testing.....
     */
    public void loadWorld() {

        try {
            BoundingBox bounds = new BoundingBox(new Vector3f(), 1, 1, 1);

            CellMO c1 = new SimpleTerrainCellMO();
            c1.setTransform(new CellTransform(null, new Vector3f(1,1,1)));
            c1.setName("c1");
            c1.setLocalBounds(bounds);

            CellMO c2 = new SimpleTerrainCellMO();
            c2.setTransform(new CellTransform(null, new Vector3f(10,10,10)));
            c2.setName("c2");
            c2.setLocalBounds(bounds);

            c1.addChild(c2);
            addCell(c1);
            
            UserPerformanceMonitor monitor = new UserPerformanceMonitor();
            BoundingVolume visBounds = new BoundingSphere(5, new Vector3f());
            
            for(CellID cellID : c1.getVisibleCells(visBounds, monitor)) {
                System.out.println(c1);
            }
            
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
            
        } catch (MultipleParentException ex) {
            Logger.getLogger(MasterCellCache.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * Builds a world defined by a wonderland file system (e.g. on disk). The
     * world's root directory must be set in the system property 
     * wonderland.wfs.root
     */
//    private void buildWFSWorld() {
//        /* Fetch the world root URI, if null, then log an error */
//        URL root = WonderlandServerConfig.getDefault().getWorldRoot();
//        if (root == null) {
//            WFS.getLogger().log(Level.SEVERE, "World Root attribute not set in server config file.");
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
     * Create a transform matrix with the specified translation
     * 
     * @param x
     * @param y
     * @param z
     * @return
     */
//    public static Matrix4d createTransform(double x, double y, double z) {
//        Matrix4d ret = new Matrix4d();
//        ret.setIdentity();
//        ret.setTranslation(new Vector3d(x,y,z));
//        return ret;
//    }
    
    /**
     *  Cell has moved, revalidate the user cell caches for those
     * users that are close
     * @param cell
     */
    void revalidate(CellMO cell) {
        throw new RuntimeException("Not Implemented");                
    }
    
    /**
     * TEMP - part of port of wfs
     */
    void revalidate() {
        
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
            private Vector3f[] pos = new Vector3f[] {
                new Vector3f(375, 375, 375),
                new Vector3f(-375, 375, 375),
                new Vector3f(-375, 375, -375),
                new Vector3f(375, 375, -375),
            };
            private int i = 0;
            private ManagedReference cellRef;
            
            public TestTask(CellMO cell) {
                this.cellRef = AppContext.getDataManager().createReference(cell);
            }
            

            public void run() throws Exception {
                System.out.println("MOVING Cell "+pos[i]);
                cellRef.get(CellMO.class).setTransform(new CellTransform(null, pos[i]));

                i++;
                if (i>=pos.length) i=0;
            }
    
    }
    /**
     * Return a new Create cell message
     */
    public static CellHierarchyMessage newCreateCellMessage(CellMO cell) {
        CellID parent=null;
        
        CellMO p = cell.getParent();
        if (p!=null) {
            parent = p.getCellID();
        }
        
        return new CellHierarchyMessage(CellHierarchyMessage.ActionType.LOAD_CELL,
            cell.getClientCellClassName(),
            cell.getComputedWorldBounds(),
            cell.getCellID(),
            parent,
            cell.getCellChannelName(),
            cell.getTransform(),
            cell.getSetupData()
            );
    }
    
    /**
     * Return a new Cell inactive message
     */
    public static CellHierarchyMessage newInactiveCellMessage(CellMO cell) {
        return new CellHierarchyMessage(CellHierarchyMessage.ActionType.CELL_INACTIVE,
            null,
            null,
            cell.getCellID(),
            null,
            null,
            null,
            null
            );
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
            null,
            null
            );
    }
    /**
     * Return a new Delete tile message
     */
    public static CellHierarchyMessage newRootCellMessage(CellMO cell) {
        return new CellHierarchyMessage(CellHierarchyMessage.ActionType.SET_WORLD_ROOT,
            null,
            null,
            cell.getCellID(),
            null,
            null,
            null,
            null
            );
    }
    
    /**
     * Return a new Delete tile message
     */
    public static CellHierarchyMessage newChangeParentCellMessage(CellMO childCell, CellMO parentCell) {
        return new CellHierarchyMessage(CellHierarchyMessage.ActionType.CHANGE_PARENT,
            null,
            null,
            childCell.getCellID(),
            parentCell.getCellID(),
            null,
            null,
            null
            
            );
    }
    
    /**
     * Return a new tile move message
     */
    public static CellHierarchyMessage newCellMoveMessage(CellMO cell) {
        return new CellHierarchyMessage(CellHierarchyMessage.ActionType.MOVE_CELL,
            null,
            cell.getComputedWorldBounds(),
            cell.getCellID(),
            null,
            null,
            cell.getTransform(),
            null
            );
    }
    
    /**
     * Return a new cell Reconfigure message.
     */
    public static CellHierarchyMessage newContentUpdateCellMessage(CellMO cellGLO) {
        
        /* Return a new CellHiearchyMessage class, with populated data fields */
        return new CellHierarchyMessage(CellHierarchyMessage.ActionType.CONTENT_UPDATE_CELL,
            cellGLO.getClientCellClassName(),
            cellGLO.getComputedWorldBounds(),
            cellGLO.getCellID(),
            cellGLO.getParent().getCellID(),
            cellGLO.getCellChannelName(),
            cellGLO.getTransform(),
            cellGLO.getSetupData()
            
            );
    }
}
