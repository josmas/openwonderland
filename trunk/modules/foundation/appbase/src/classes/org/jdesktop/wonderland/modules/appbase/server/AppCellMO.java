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
package org.jdesktop.wonderland.modules.appbase.server;

import com.jme.bounding.BoundingVolume;
import com.jme.math.Vector3f;
import org.jdesktop.wonderland.common.cell.CellTransform;
import org.jdesktop.wonderland.modules.appbase.common.AppCellConfig;
import org.jdesktop.wonderland.common.cell.state.CellClientState;
import org.jdesktop.wonderland.server.cell.CellMO;
import org.jdesktop.wonderland.common.ExperimentalAPI;
import com.sun.sgs.app.ClientSession;
import org.jdesktop.wonderland.common.cell.ClientCapabilities;
import org.jdesktop.wonderland.common.cell.state.CellServerState;
import org.jdesktop.wonderland.server.comms.WonderlandClientID;
import org.jdesktop.wonderland.server.setup.BasicCellSetupHelper;


/**
 * A server-side <code>app.base</code> app cell.
 *
 * @author deronj
 */

@ExperimentalAPI
public abstract class AppCellMO extends CellMO { 

    /** Default constructor, used when the cell is created via WFS */
    public AppCellMO() {
	super();
    }
    
    /**
     * Creates a new instance of <code>AppCellMO</code> with the specified localBounds and transform.
     * If either parameter is null an IllegalArgumentException will be thrown.
     *
     * @param localBounds the bounds of the new cell, must not be null.
     * @param transform the transform for this cell, must not be null.
     */
    public AppCellMO (BoundingVolume localBounds, CellTransform transform) {
        super(localBounds, transform);
    }
    
    /** 
     * Return the app type of this cell.
     */
    public abstract AppTypeMO getAppType ();

    /** 
     * {@inheritDoc}
     */
    @Override
    protected CellClientState getCellClientState (CellClientState cellClientState, WonderlandClientID clientID, ClientCapabilities capabilities) {
        // If the given cellClient State is null, then create one
        if (cellClientState == null) {
            cellClientState = new AppCellConfig();
        }
        return super.getCellClientState(cellClientState, clientID, capabilities);
    }

    /** 
     * {@inheritDoc}
     */
    @Override
    public void setServerState(CellServerState setupData) {
        super.setServerState(setupData);
    }
}
