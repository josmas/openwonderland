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
package org.jdesktop.wonderland.modules.testcells.server.cell;

import com.jme.bounding.BoundingVolume;
import com.sun.sgs.app.ManagedObject;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.server.cell.ProximityListenerSrv;

/**
 *
 * @author paulby
 */
public class ProximityTest implements ProximityListenerSrv { // , ManagedObject {
    public void viewEnterExit(boolean entered, CellID cell, CellID viewCellID, BoundingVolume proximityVolume, int proximityIndex) {
        System.err.println("ENter/Exit "+entered);
    }

}
