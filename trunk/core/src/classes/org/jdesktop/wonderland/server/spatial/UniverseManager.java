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
package org.jdesktop.wonderland.server.spatial;

import com.jme.bounding.BoundingVolume;
import com.sun.sgs.app.AppContext;
import org.jdesktop.wonderland.common.cell.CellTransform;
import org.jdesktop.wonderland.server.cell.CellMO;
import org.jdesktop.wonderland.server.cell.view.ViewCellMO;

/**
 * Manages Cells within the WonderlandUniverse. Computes and tracks the
 * WorldTransform and WorldBounds of cells as their localTransform and localBounds
 * are modified (or parents/children in the graph are modified). Changes to
 * the graph will trigger cell cache updates.
 *
 * @author paulby
 */
public class UniverseManager {

    private static UniverseManager universeManager=new UniverseManager();

    private UniverseServiceManager serviceMgr;

    UniverseManager() {
        serviceMgr = AppContext.getManager(UniverseServiceManager.class);
    }

    public static UniverseManager getUniverseManager() {
        return universeManager;
    }

    public void addChild(CellMO parent, CellMO child) {
        serviceMgr.addChild(parent,child);
    }

    public void createCell(CellMO cellMO) {
        serviceMgr.createCell(cellMO);
    }

    public void removeCell(CellMO cellMO) {
        serviceMgr.removeCell(cellMO);
    }

    public void removeChild(CellMO parent, CellMO child) {
        serviceMgr.removeChild(parent, child);
    }

    public  void addRootToUniverse(CellMO rootCellMO) {
        serviceMgr.addRootToUniverse(rootCellMO);
    }

    public void removeRootFromUniverse(CellMO rootCellMO) {
        serviceMgr.removeRootFromUniverse(rootCellMO);
    }

    public void setLocalTransform(CellMO cell, CellTransform localCellTransform) {
        serviceMgr.setLocalTransform(cell, localCellTransform);
    }

    public CellTransform getWorldTransform(CellMO cell, CellTransform result) {
        return serviceMgr.getWorldTransform(cell, result);
    }

    public BoundingVolume getWorldBounds(CellMO cell, BoundingVolume result) {
        return serviceMgr.getWorldBounds(cell, result);
    }

    public void viewLogin(ViewCellMO viewCell) {
        serviceMgr.viewLogin(viewCell);
    }

    public void viewLogout(ViewCellMO viewCell) {
        serviceMgr.viewLogout(viewCell);

    }
}
