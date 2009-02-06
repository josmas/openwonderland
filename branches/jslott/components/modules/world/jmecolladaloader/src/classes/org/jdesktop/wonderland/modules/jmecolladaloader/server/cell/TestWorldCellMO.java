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
package org.jdesktop.wonderland.modules.jmecolladaloader.server.cell;

import com.jme.bounding.BoundingBox;
import com.jme.bounding.BoundingSphere;
import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import com.sun.sgs.app.AppContext;
import com.sun.sgs.app.ManagedReference;
import com.sun.sgs.app.Task;
import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.wonderland.common.ExperimentalAPI;
import org.jdesktop.wonderland.common.cell.CellTransform;
import org.jdesktop.wonderland.common.cell.ClientCapabilities;
import org.jdesktop.wonderland.common.cell.state.CellServerState;
import org.jdesktop.wonderland.modules.jmecolladaloader.common.cell.state.TestWorldCellServerState;
import org.jdesktop.wonderland.server.cell.CellMO;
import org.jdesktop.wonderland.server.cell.CellManagerMO;
import org.jdesktop.wonderland.server.cell.MovableComponentMO;
import org.jdesktop.wonderland.server.comms.WonderlandClientID;


/**
 * Test Cell for use until WFS is integrated, this will be removed.
 * 
 * @author paulby
 */
@ExperimentalAPI
public class TestWorldCellMO extends CellMO {

    /** Default constructor, used when cell is created via WFS */
    public TestWorldCellMO() {
        this(new Vector3f(), 50);
    }

    private ManagedReference<MovableCellMO> c2Ref, c3Ref;

    public TestWorldCellMO(Vector3f center, float size) {
        super(new BoundingBox(new Vector3f(), size, size, size), new CellTransform(null, center));

        try {
            BoundingBox bounds = new BoundingBox(new Vector3f(), 1, 1, 1);
            MovableCellMO c2 = new MovableCellMO(bounds,
                    new CellTransform(null, new Vector3f(10, 5, 10)));
            c2.setName("c2");
            c2.setLocalBounds(bounds);
            c2Ref = AppContext.getDataManager().createReference(c2);

            MovableCellMO c3 = new MovableCellMO(
                    new BoundingSphere(2, new Vector3f()),
                    new CellTransform(null, new Vector3f(5, 5, 5)));
            c3.setName("c3");
            c3Ref = AppContext.getDataManager().createReference(c3);

            CellMO c4 = new MovableCellMO(
                    new BoundingSphere(0.5f, new Vector3f()),
                    new CellTransform(null, new Vector3f(1, 0, 0)));
            c4.setName("c4");

            c3.addChild(c4);

//            float cellSize = 20;
//            int xMax = 30;
//            int zMax = 3;
//
//            for(int x=0; x<cellSize*xMax; x+=cellSize) {
//                for(int z=0; z<cellSize*zMax; z+=cellSize) {
//                    addChild(new StaticModelCellMO(new Vector3f(x,0,z), cellSize/2f));
//                }
//            }

            addChild(c2);
            addChild(c3);

            addChild(new JmeColladaCellMO(new Vector3f(0,0,0), 15,
                            "wla://jmecolladaloader/RoomLow10x15/models/RoomLow10x15.dae",
                            null,
                            new Quaternion(new float[]{-(float)Math.PI/2, 0f, 0f})));

            addChild(new JmeColladaCellMO(new Vector3f(15,0,0), 15,
                            "wla://jmecolladaloader/RoomLow10x15/models/RoomLow10x15.dae",
                            null,
                            new Quaternion(new float[]{-(float)Math.PI/2, 0f, 0f})));

            addChild(new JmeColladaCellMO(new Vector3f(0,0,10), 15,
                            "wla://jmecolladaloader/OutsideFloor10x10/models/OutsideFloor10x10.dae",
                            null,
                            new Quaternion(new float[]{-(float)Math.PI/2, 0f, 0f})));

        } catch (Exception ex) {
            Logger.getLogger(CellManagerMO.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    protected void setLive(boolean live) {
        super.setLive(live);
        if (live == true) {
            Task t = new TestTask(c3Ref.get(), c2Ref.get());
            AppContext.getTaskManager().schedulePeriodicTask(t, 5000, 1000);
        }
    }


    @Override
    protected String getClientCellClassName(WonderlandClientID clientID,
                                            ClientCapabilities capabilities)
    {
        return "org.jdesktop.wonderland.modules.jmecolladaloader.client.cell.TestWorldCell";
    }

    @Override
    public CellServerState getServerState(CellServerState setup) {
        if (setup == null) {
            setup = new TestWorldCellServerState();
        }

        return super.getServerState(setup);
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
            cellRef.get().getComponent(MovableComponentMO.class).moveRequest(null, new CellTransform(null, pos));
            cell2Ref.get().getComponent(MovableComponentMO.class).moveRequest(null, new CellTransform(null, pos2));
        }
    }
}
