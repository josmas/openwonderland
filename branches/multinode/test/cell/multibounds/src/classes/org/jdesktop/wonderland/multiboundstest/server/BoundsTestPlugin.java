/**
 * Project Wonderland
 *
 * $RCSfile:$
 *
 * Copyright (c) 2004-2007, Sun Microsystems, Inc., All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * $Revision:$
 * $Date:$
 * $State:$
 */
package org.jdesktop.wonderland.multiboundstest.server;

import com.jme.bounding.BoundingBox;
import com.jme.bounding.BoundingSphere;
import com.jme.bounding.BoundingVolume;
import com.jme.math.Vector3f;
import com.sun.sgs.app.AppContext;
import com.sun.sgs.app.ManagedReference;
import com.sun.sgs.app.Task;
import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.wonderland.common.cell.CellTransform;
import org.jdesktop.wonderland.common.cell.MultipleParentException;
import org.jdesktop.wonderland.server.ServerPlugin;
import org.jdesktop.wonderland.server.WonderlandContext;
import org.jdesktop.wonderland.server.cell.CellMO;
import org.jdesktop.wonderland.server.cell.CellManagerMO;
import org.jdesktop.wonderland.server.cell.MovableCellMO;
import org.jdesktop.wonderland.server.cell.RevalidatePerformanceMonitor;
import org.jdesktop.wonderland.server.cell.SimpleTerrainCellMO;

/**
 * Sample plugin that doesn't do anything
 * @author jkaplan
 */
public class BoundsTestPlugin implements ServerPlugin {

    private static final Logger logger =
            Logger.getLogger(BoundsTestPlugin.class.getName());

    public void initialize() {
        logger.info("Initialize bounds test plugin");

        try {
            // createStaticGrid();

            BoundingBox bounds = new BoundingBox(new Vector3f(), 1, 1, 1);

            CellMO c1 = new MovableCellMO(bounds,
                    new CellTransform(null, new Vector3f(1, 1, 1)));
            c1.setName("c1");

            MovableCellMO c2 = new MovableCellMO(bounds,
                    new CellTransform(null, new Vector3f(10, 10, 10)));
            c2.setName("c2");
            c2.setLocalBounds(bounds);

            MovableCellMO c3 = new MovableCellMO(
                    new BoundingSphere(2, new Vector3f()),
                    new CellTransform(null, new Vector3f(5, 5, 5)));
            c3.setName("c3");

            CellMO c4 = new MovableCellMO(
                    new BoundingSphere(0.5f, new Vector3f()),
                    new CellTransform(null, new Vector3f(0, 0, 0)));
            c4.setName("c4");

            c3.addChild(c4);

            c1.addChild(c2);
            c1.addChild(c3);
            WonderlandContext.getCellManager().addCell(c1);

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
            Logger.getLogger(CellManagerMO.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Create a static grid of nodes
     */
    private void createStaticGrid() {
        int gridWidth = 10;
        int gridDepth = 10;

        float boundsDim = 5;
        BoundingBox gridBounds = new BoundingBox(new Vector3f(), boundsDim, boundsDim, boundsDim);

        for (int x = 0; x < gridWidth; x++) {
            for (int z = 0; z < gridDepth; z++) {
                try {
                    CellMO cell = new SimpleTerrainCellMO(new Vector3f(x * boundsDim * 2, 0, z * boundsDim * 2), boundsDim);
                    cell.setName("grid_" + x + "_" + z);
                    WonderlandContext.getCellManager().addCell(cell);
                } catch (MultipleParentException ex) {
                    Logger.getLogger(CellManagerMO.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
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
            pos = cell.getTransform().getTranslation(null);
            pos2 = cell.getTransform().getTranslation(null);
        }

        public void run() throws Exception {
            pos.x += dir;
            pos2.z += dir;
            if (pos.x > 40 || pos.x < 2) {
                dir = -dir;
            }
            cellRef.get().setTransform(new CellTransform(null, pos));
            cell2Ref.get().setTransform(new CellTransform(null, pos2));
        }
    }
}
