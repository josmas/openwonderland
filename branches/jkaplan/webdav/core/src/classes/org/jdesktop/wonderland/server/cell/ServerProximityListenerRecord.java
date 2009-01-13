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

import com.jme.bounding.BoundingVolume;
import com.sun.sgs.app.AppContext;
import com.sun.sgs.app.ManagedObject;
import com.sun.sgs.app.ManagedReference;
import com.sun.sgs.kernel.KernelRunnable;
import com.sun.sgs.kernel.TransactionScheduler;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.CellTransform;
import org.jdesktop.wonderland.common.cell.ProximityListenerRecord;
import org.jdesktop.wonderland.common.cell.ProximityListenerRecord.ProximityListenerWrapper;
import org.jdesktop.wonderland.server.spatial.UniverseManager;
import org.jdesktop.wonderland.server.spatial.ViewUpdateListener;



/**
 * This listener record provides the server specific hooks for the generic 
 * proximity listener code, which is shared with the client code.
 *
 * @author paulby
 */
public class ServerProximityListenerRecord extends ProximityListenerRecord implements TransformChangeListenerSrv, ViewUpdateListener {

    public ServerProximityListenerRecord() {
    }

    public ServerProximityListenerRecord(ProximityListenerWrapper proximityListener, BoundingVolume[] localBounds) {
        super(proximityListener, localBounds);
    }

    public void viewTransformChanged(CellID cell, CellID viewCellID, CellTransform viewWorldTransform) {
        viewCellMoved(viewCellID, viewWorldTransform);
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
        private ManagedReference<ProximityListenerSrv> listenerRef = null;
        private CellID cellID;

        public ServerProximityListenerWrapper(CellID cell, ProximityListenerSrv listener) {
            if (listener instanceof ManagedObject) {
                listenerRef = AppContext.getDataManager().createReference(listener);
            } else {
                this.listener = listener;
            }
            this.cellID = cell;
        }

        CellID getCellID() {
            return cellID;
        }

        public void viewEnterExit(boolean enter,
                                  BoundingVolume proximityVolume,
                                  int proximityIndex,
                                  CellID viewCellID) {
            if (listenerRef!=null) {
                throw new RuntimeException("Not implemented yet");
//                ProximityNotifierTask tsk = new ProximityNotifierTask(listenerRef, enter, proximityVolume, proximityIndex, cellID, viewCellID);
//                AppContext.getManager(TransactionScheduler.class).runTask(tsk, arg1);
            } else {
                listener.viewEnterExit(enter, cellID, viewCellID, proximityVolume, proximityIndex);
            }
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof ServerProximityListenerWrapper))
                return false;

            if (listenerRef!=null) {
                if (((ServerProximityListenerWrapper)o).listenerRef.equals(listenerRef))
                    return true;

            } else {
                if (((ServerProximityListenerWrapper)o).listener==listener)
                    return true;

            }


            return false;
        }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 53 * hash + (this.listener != null ? this.listener.hashCode() : 0);
        hash = 53 * hash + (this.listenerRef != null ? this.listenerRef.hashCode() : 0);
        return hash;
    }


    }

