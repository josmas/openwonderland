/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.modules.jmecolladaloader.server.cell;

import com.jme.bounding.BoundingBox;
import com.jme.bounding.BoundingSphere;
import com.jme.math.Vector3f;
import com.sun.sgs.app.AppContext;
import com.sun.sgs.app.ManagedReference;
import com.sun.sgs.app.Task;
import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.wonderland.common.cell.CellTransform;
import org.jdesktop.wonderland.server.WonderlandContext;
import org.jdesktop.wonderland.server.cell.CellMO;
import org.jdesktop.wonderland.server.cell.CellManagerMO;
import org.jdesktop.wonderland.server.cell.MovableComponentMO;
import org.jdesktop.wonderland.modules.jmecolladaloader.server.cell.RoomTestCellMO;
import org.jdesktop.wonderland.server.ServerPlugin;

/**
 *
 * @author paulby
 */
public class TestWorld implements ServerPlugin {

    public TestWorld() {
    }

    public void initialize() {
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
            int xMax = 3;
            int zMax = 3;
            
            for(int x=0; x<cellSize*xMax; x+=cellSize) {
                for(int z=0; z<cellSize*zMax; z+=cellSize) {
                    WonderlandContext.getCellManager().insertCellInWorld(new StaticModelCellMO(new Vector3f(x,0,z), cellSize/2f));
                }
            }

            WonderlandContext.getCellManager().insertCellInWorld(c2);
            WonderlandContext.getCellManager().insertCellInWorld(c3);

            WonderlandContext.getCellManager().insertCellInWorld(new RoomTestCellMO(new Vector3f(5, 0, 5), 16));
//            WonderlandContext.getCellManager().insertCellInWorld(new TestColladaCellMO(new Vector3f(5, 1, 5), 4));
//            WonderlandContext.getCellManager().insertCellInWorld(new TestColladaCellMO(new Vector3f(4, 1, 5), 4));
//            WonderlandContext.getCellManager().insertCellInWorld(new TestColladaCellMO(new Vector3f(3, 1, 5), 4));
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
}
