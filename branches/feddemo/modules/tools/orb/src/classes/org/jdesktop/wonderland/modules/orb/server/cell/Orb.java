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
package org.jdesktop.wonderland.modules.orb.server.cell;

import com.sun.sgs.app.AppContext;
import com.sun.sgs.app.DataManager;
import com.sun.sgs.app.ManagedReference;
import com.sun.sgs.app.ManagedObject;

import java.io.Serializable;

import java.util.logging.Logger;

import org.jdesktop.wonderland.server.cell.CellComponentMO;
import org.jdesktop.wonderland.server.cell.CellManagerMO;
import org.jdesktop.wonderland.server.cell.CellMOFactory;

import org.jdesktop.wonderland.modules.orb.common.OrbCellServerState;

import org.jdesktop.wonderland.modules.orb.server.cell.OrbCellMO;

import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.MultipleParentException;

import com.jme.bounding.BoundingVolume;

import com.jme.math.Vector3f;

/**
 * Spawn an orb at a specified location
 * @author jprovino
 */
public class Orb implements ManagedObject, Serializable {

    private static final Logger logger =
        Logger.getLogger(Orb.class.getName());
     
    private ManagedReference<OrbCellMO> orbCellMORef;

    private String username;
    private CellID cellID;

    public Orb(String username, String externalCallID, Vector3f center, 
	    double size, boolean simulateCalls, String playerWithVpCallID) {

	this.username = username;

	logger.fine("orb center :  " + center + " size " + size);

        String cellType = 
	    "org.jdesktop.wonderland.modules.orb.server.cell.OrbCellMO";

        OrbCellMO orbCellMO = (OrbCellMO) CellMOFactory.loadCellMO(cellType, 
	    center, (float) size, username, externalCallID, simulateCalls,
	    playerWithVpCallID);

	if (orbCellMO == null) {
	    logger.warning("Unable to spawn orb");
	    return;
	}

	cellID = orbCellMO.getCellID();

	try {
            orbCellMO.setServerState(new OrbCellServerState());
        } catch (ClassCastException e) {
            logger.warning("Error setting up new cell " +
                orbCellMO.getName() + " of type " +
                orbCellMO.getClass() + e.getMessage());
            return;
        }

	try {
	    CellManagerMO.getCellManager().insertCellInWorld(orbCellMO);
	} catch (MultipleParentException e) {
	    logger.warning("Can't insert orb in world:  " + e.getMessage());
	    return;
	}
	
	orbCellMORef = AppContext.getDataManager().createReference(orbCellMO);
    }

    public CellID getCellID() {
	return cellID;
    }

    public OrbCellMO getOrbCellMO() {
	return orbCellMORef.get();
    }

    public void addComponent(CellComponentMO component) {
	getOrbCellMO().addComponent(component);
    }

    public void done() {
	if (orbCellMORef == null) {
	    return;
	}

	CellManagerMO.getCellManager().removeCellFromWorld(orbCellMORef.get());

	orbCellMORef = null;
    }

    public String toString() {
	return username;
    }

}
