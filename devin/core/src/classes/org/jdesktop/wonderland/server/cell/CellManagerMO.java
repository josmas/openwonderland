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
import com.jme.math.Vector3f;
import com.sun.sgs.app.AppContext;
import com.sun.sgs.app.ManagedObject;
import com.sun.sgs.app.ManagedReference;
import com.sun.sgs.app.NameNotBoundException;
import com.sun.sgs.app.Task;
import java.io.Serializable;
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
import org.jdesktop.wonderland.wfs.cell.WFSCellMO;
import org.jdesktop.wonderland.wfs.loader.WFSLoader;

/**
 *
 * @author paulby
 */
@ExperimentalAPI
public class CellManagerMO implements ManagedObject, Serializable {

    // Used to generate unique cell ids
    private long cellCounter=CellID.getFirstCellID();
    
    private ManagedReference<CellMO> rootCellRef;
    
    private static final String BINDING_NAME=CellManagerMO.class.getName();
    private static final Logger logger = Logger.getLogger(CellManagerMO.class.getName());
    
    private SpaceManager spaceManager = new SpaceManagerGridImpl();
    
    private static CellID rootCellID;
    
    /**
     * Creates a new instance of CellManagerMO
     */
    CellManagerMO() {
        AppContext.getDataManager().setBinding(BINDING_NAME, this);
        createRootCell();
        spaceManager.initialize();
    }
    
    private void createRootCell() {
        BoundingSphere rootBounds = new BoundingSphere(Float.MAX_VALUE, new Vector3f());
        CellTransform orig = new CellTransform(null, new Vector3f());
        CellMO root = new RootCellMO(rootBounds, orig);
        rootCellID = root.getCellID();
        root.setName("root");
        root.setLive(true);   
        
        rootCellRef = AppContext.getDataManager().createReference(root);
    }
    
    /**
     * Return the root cell id, used by ViewCellCacheMO. Currently we only use
     * a single root on the server, but the client can support multiple roots.
     * @return
     */
    static CellID getRootCellID() {
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
     * Return the space that encloses this point, if the space does not exist, create it
     * @param position
     * @return
     */
    SpaceMO[] getEnclosingSpace(Vector3f point) {
        return spaceManager.getEnclosingSpace(point);
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
        rootCellRef.getForUpdate().addChild(cell);
    }        
    
    /**
     * For testing.....
     */
    public void loadWorld() {
        buildWFSWorld();
        
        //test();
    }

    public void test() {
        logger.info("Initialize bounds test plugin");

        try {

            BoundingBox bounds = new BoundingBox(new Vector3f(), 1, 1, 1);

            MovableCellMO c2 = new MovableCellMO(bounds,
                    new CellTransform(null, new Vector3f(10, 5, 10)));
            c2.setName("c2");
            c2.setLocalBounds(bounds);

            MovableCellMO c3 = new MovableCellMO(
                    new BoundingSphere(2, new Vector3f()),
                    new CellTransform(null, new Vector3f(5, 5, 5)));
            c3.setName("c3");

            CellMO c4 = new MovableCellMO(
                    new BoundingSphere(0.5f, new Vector3f()),
                    new CellTransform(null, new Vector3f(1, 0, 0)));
            c4.setName("c4");

            c3.addChild(c4);
            
            float cellSize = 20;
            int xMax = 80;
            int zMax = 4;
            
            for(int x=0; x<cellSize*xMax; x+=cellSize) {
                for(int z=0; z<cellSize*zMax; z+=cellSize) {
                    WonderlandContext.getCellManager().insertCellInWorld(new StaticModelCellMO(new Vector3f(x,0,z), cellSize/2f));
                }
            }

            WonderlandContext.getCellManager().insertCellInWorld(c2);
            WonderlandContext.getCellManager().insertCellInWorld(c3);

            WonderlandContext.getCellManager().insertCellInWorld(new RoomTestCellMO(new Vector3f(5, 0, 5), 16));
            WonderlandContext.getCellManager().insertCellInWorld(new TestColladaCellMO(new Vector3f(5, 1, 5), 4));
            WonderlandContext.getCellManager().insertCellInWorld(new TestColladaCellMO(new Vector3f(4, 1, 5), 4));
            WonderlandContext.getCellManager().insertCellInWorld(new TestColladaCellMO(new Vector3f(3, 1, 5), 4));
            WonderlandContext.getCellManager().insertCellInWorld(new RoomTestCellMO(new Vector3f(45, 0, 5), 8));

            
            Task t = new TestTask(c3, c2);

            AppContext.getTaskManager().schedulePeriodicTask(t, 5000, 1000);

        } catch (Exception ex) {
            Logger.getLogger(CellManagerMO.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    static class TestTask implements Task, Serializable {

        private ManagedReference<MovableCellMO> cellRef;
        private Vector3f pos;
        private Vector3f pos2;
        private int dir = 2;
        private ManagedReference<MovableCellMO> cell2Ref;

        public TestTask(MovableCellMO cell, MovableCellMO c2) {
            this.cellRef = AppContext.getDataManager().createReference(cell);
            this.cell2Ref = AppContext.getDataManager().createReference(c2);
            pos = cell.getLocalTransform(null).getTranslation(null);
            pos2 = cell.getLocalTransform(null).getTranslation(null);
        }

        public void run() throws Exception {
            pos.x += dir;
            pos2.z += dir;
            if (pos.x > 40 || pos.x < 4) {
                dir = -dir;
            }
            cellRef.get().getComponent(MovableComponentMO.class).moveRequest(new CellTransform(null, pos));
            cell2Ref.get().getComponent(MovableComponentMO.class).moveRequest(new CellTransform(null, pos2));
        }
    }
    
    /**
     * Builds a world defined by a wonderland file system (e.g. on disk). The
     * world's root directory must be setTranslation in the system property 
     * wonderland.wfs.root
     */
    private void buildWFSWorld() {
        new WFSLoader().load();
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
