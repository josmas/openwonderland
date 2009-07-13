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
import java.util.HashMap;
import org.jdesktop.wonderland.common.ExperimentalAPI;
import org.jdesktop.wonderland.common.cell.ProximityListenerRecord;
import org.jdesktop.wonderland.server.WonderlandContext;
import org.jdesktop.wonderland.server.spatial.UniverseManager;

/**
 * Provides a mechanism for listener notification when the a view cell
 * enters/exits a set of bounds for a cell. 
 * 
 * The bounds must be ordered from largest to smallest, thus localBounds[i]
 * must enclose localBounds[i+1]. The listeners will be notified as the View
 * enters each subsequent bounding volume and then notified the view exits each
 * volume.
 * 
 * For example given a set of Bounding Spheres with the same center and radii of
 * 10, 5, 2. As the ViewCell moves from outside to the center of the spheres the
 * listeners will be called with
 * 
 * enter, 10
 * enter, 5 
 * enter, 2
 * 
 * then as the user moves away from the center the following sequence of exits
 * will be called
 * 
 * exit, 2
 * exit, 5
 * exit, 10
 * 
 * 
 * @author paulby
 */
@ExperimentalAPI
public class ProximityComponentMO extends CellComponentMO {

    protected HashMap<ProximityListenerSrv, ServerProximityListenerRecord> proximityListeners = null;
    protected HashMap<ManagedReference<ProximityListenerSrv>, ServerProximityListenerRecord> proximityListenersRef = null;
    private boolean isLive = false;

//    private ManagedReference<UserLeftListener> userListenerRef;
    
    /**
     * Set a list of bounds for which the system will track view enter/exit for
     * this cell. When the view enters/exits one of these bounds the listener
     * will be called with the index of the bounds in the supplied array.
     * 
     * The bounds must be ordered from largest to smallest, thus localBounds[i]
     * must enclose localBounds[i+1]
     * 
     * @param cell the cell
     * @param localProximityBounds the proximity bounds in cell local coordinates
     */
    public ProximityComponentMO(CellMO cell) {
        super(cell);
    }
    
    /**
     * Add a ProximityListener for the cell to which this component is attached.
     * The listener will be called as View cells in the universe enter or exit
     * the bounds specified.
     *
     * The bounds must be ordered from largest to smallest, thus localBounds[i]
     * must enclose localBounds[i+1]. The listeners will be notified as the View
     * enters each subsequent bounding volume and then notified the view exits each
     * volume.
     *
     * For example given a set of Bounding Spheres with the same center and radii of
     * 10, 5, 2. As the ViewCell moves from outside to the center of the spheres the
     * listeners will be called with
     *
     * enter, 10
     * enter, 5
     * enter, 2
     *
     * then as the user moves away from the center the following sequence of exits
     * will be called
     *
     * exit, 2
     * exit, 5
     * exit, 10
     * 
     * @param listener
     * @param localBounds the set of bounds, in the local coordinate system of the cell
     */
    public void addProximityListener(ProximityListenerSrv listener, BoundingVolume[] localBounds) {

        ServerProximityListenerRecord rec = new ServerProximityListenerRecord(new ServerProximityListenerWrapper(cellID, listener), localBounds);
        if (listener instanceof ManagedObject) {
            if (proximityListenersRef==null)
                proximityListenersRef = new HashMap();
                proximityListenersRef.put(AppContext.getDataManager().createReference(listener), rec);

        } else {
            if (proximityListeners==null)
                proximityListeners = new HashMap();
                proximityListeners.put(listener, rec);
        }


        UniverseManager mgr = AppContext.getManager(UniverseManager.class);
        CellMO cell = cellRef.get();
        rec.setLive(isLive, cell, mgr);
    }

    /**
     * Remove the specified ProximityListener
     * @param listener
     */
    public void removeProximityListener(ProximityListenerSrv listener) {
        ServerProximityListenerRecord rec;
        if (listener instanceof ManagedObject) {
            rec = proximityListenersRef.remove(AppContext.getDataManager().createReference(listener));
        } else {
            rec = proximityListeners.remove(listener);
        }
        if (rec!=null) {
            UniverseManager mgr = AppContext.getManager(UniverseManager.class);
            CellMO cell = cellRef.get();
            rec.setLive(false, cell, mgr);
        }
    }

    /**
     * Updates the bounds of the specified listener to be the specified local bounds.
     *
     * If the specified listener object is not a registered listener, this method
     * will have no effect.
     * 
     * @param listener The listener object who's bounds you want to change.
     * @param localBounds The new bounds list.
     */
    public void setProximityListenerBounds(ProximityListenerSrv listener, BoundingVolume[] localBounds) {
        if(listener instanceof ManagedObject) {

            ManagedReference listenerRef = AppContext.getDataManager().createReference(listener);

            // Check if the specified listener is actually registered.
            if(this.proximityListenersRef.containsKey(listenerRef)) {

                // If so, update the ListenerRecord with new bounds.
                ProximityListenerRecord proxListRec = this.proximityListenersRef.get(listenerRef);
                proxListRec.setProximityBounds(localBounds);
            }

            // Otherwise, just ignore it.
        } else {
            
            // As above, but with the non referenced store.
            if(this.proximityListeners.containsKey(listener)) {
                ProximityListenerRecord proxListRec = this.proximityListeners.get(listener);
                proxListRec.setProximityBounds(localBounds);
            }
        }
    }
    
    @Override
    public void setLive(boolean isLive) {
        super.setLive(isLive);
        this.isLive = isLive;

        if (isLive) {
            UniverseManager mgr = AppContext.getManager(UniverseManager.class);
            CellMO cell = cellRef.get();
            if (proximityListeners!=null)
                for(ServerProximityListenerRecord rec : proximityListeners.values()) {
                    rec.setLive(isLive, cell, mgr);
                }
            if (proximityListenersRef!=null)
                for(ServerProximityListenerRecord rec : proximityListenersRef.values()) {
                    rec.setLive(isLive, cell, mgr);
                }

        // Register for user join/leave events, so we can make sure to fire
        // EXIT events when someone logs out from within a cell.

        // These lines are commented out until we have a fix for Issue 295.
        // UserLeftListener userListener = new UserLeftListener(this);
        // WonderlandContext.getUserManager().addUserListener(userListener);

        // this.userListenerRef = AppContext.getDataManager().createReference(userListener);

        } else {
            UniverseManager mgr = AppContext.getManager(UniverseManager.class);
            CellMO cell = cellRef.get();
            if (proximityListeners!=null)
                for(ServerProximityListenerRecord rec : proximityListeners.values()) {
                    rec.setLive(isLive, cell, mgr);
                }
            if (proximityListenersRef!=null)
                for(ServerProximityListenerRecord rec : proximityListenersRef.values()) {
                    rec.setLive(isLive, cell, mgr);
                }

              // See above comments about UserLeftListener related issues. 
//            if(this.userListenerRef!=null) {
//                WonderlandContext.getUserManager().removeUserListener(this.userListenerRef.get());
//                this.userListenerRef = null;
//            }

         }

    }

    @Override
    protected String getClientClass() {
        return null;
    }

}
