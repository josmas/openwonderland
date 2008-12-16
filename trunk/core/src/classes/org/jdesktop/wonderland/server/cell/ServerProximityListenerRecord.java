/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.server.cell;

import com.jme.bounding.BoundingVolume;
import com.sun.sgs.app.ManagedReference;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.CellTransform;
import org.jdesktop.wonderland.common.cell.ProximityListenerRecord;
import org.jdesktop.wonderland.common.cell.ProximityListenerRecord.ProximityListenerWrapper;
import org.jdesktop.wonderland.server.spatial.UniverseManager;
import org.jdesktop.wonderland.server.spatial.ViewUpdateListener;



/**
 *
 * @author paulby
 */
public class ServerProximityListenerRecord extends ProximityListenerRecord implements TransformChangeListenerSrv, ViewUpdateListener {

    public ServerProximityListenerRecord(ProximityListenerWrapper proximityListener, BoundingVolume[] localBounds) {
        super(proximityListener, localBounds);
    }

    public void viewTransformChanged(CellID cell, CellID viewCellID, CellTransform viewWorldTransform) {
        viewCellMoved(viewWorldTransform);
    }

    public void transformChanged(ManagedReference<CellMO> cellRef, CellTransform localTransform, CellTransform worldTransform) {
        updateWorldBounds(worldTransform);
    }

    void setLive(boolean isLive, final CellMO cell, UniverseManager mgr) {
        if (isLive) {
            ServerProximityListenerWrapper rec = (ServerProximityListenerWrapper) proximityListener;
            mgr.addTransformChangeListener(cell, this);
            mgr.addViewUpdateListener(cell, this);

            // The cell will be added to the UniverseManager when this transaction
            // completes. At which point we will get a transformChanged callback
            // as the world transform is calculated
        } else {
            mgr.removeTransformChangeListener(cell, this);
            mgr.removeViewUpdateListener(cell, this);
        }
    }

}
/**
     * Internal structure containing the array of bounds for a given listener.
     */
    class ServerProximityListenerWrapper implements ProximityListenerRecord.ProximityListenerWrapper {

        private ProximityListenerSrv listener;
        private CellID cellID;

        public ServerProximityListenerWrapper(CellID cell, ProximityListenerSrv listener) {
            this.listener = listener;
            this.cellID = cell;
        }

        CellID getCellID() {
            return cellID;
        }

        public void viewEnterExit(boolean enter, BoundingVolume proximityVolume, int proximityIndex) {
            // TODO Check if listener is an MO and call from a Transaction if it is
            listener.viewEnterExit(enter, cellID, proximityVolume, proximityIndex);
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof ServerProximityListenerWrapper))
                return false;

            if (((ServerProximityListenerWrapper)o).listener==listener)
                return true;

            return false;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 41 * hash + (this.listener != null ? this.listener.hashCode() : 0);
            return hash;
        }
    }

