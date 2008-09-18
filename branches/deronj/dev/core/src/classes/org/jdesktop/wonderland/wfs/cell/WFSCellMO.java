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
package org.jdesktop.wonderland.wfs.cell;

import com.jme.bounding.BoundingBox;
import com.jme.math.Vector3f;
import com.sun.sgs.app.ClientSession;
import com.sun.sgs.app.ManagedObject;
import java.io.Serializable;
import org.jdesktop.wonderland.common.cell.CellTransform;
import org.jdesktop.wonderland.common.cell.ClientCapabilities;
import org.jdesktop.wonderland.common.cell.config.CellConfig;
import org.jdesktop.wonderland.server.cell.CellMO;

/**
 * The WFSCellGLO class is a cell that represents a portion of the world which
 * was loaded from a WFS. This class uses WFSLoader and WFSReloader to load the
 * world
 *
 * @author Jordan Slott <jslott@dev.java.net>
 */
public class WFSCellMO extends CellMO implements ManagedObject, Serializable {
    
    /* The prefix of the public binding name to find this GLO later */
    public static final String WFS_CELL_GLO = "WFS_CELL_GLO_";
    
    /* The name of the root */
    private String rootName = null;
    
    /** Constructor */
    public WFSCellMO(String rootName) {
        /*
         * These bounds may not entirely be correct -- a WFSCellGLO should simply
         * assume the bounds of its parent.
         */
//        super(new BoundingBox(new Vector3f(), Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY),
//                new CellTransform(null, null, null));
        super(new BoundingBox(new Vector3f(), (float)2.0, (float)2.0, (float)2.0), new CellTransform(null, new Vector3f((float)0.0, (float)0.0, (float)0.0)));

        this.rootName = rootName;
    }
    
    /**
     * Returns the "binding name" for the WFS cell GLO so that it may easily
     * be retrieved. This binding name is simply WFS_CELL_GLO + ID.
     * 
     * @return The ManagedObject binding name
     */
    public String getBindingName() {
        return WFSCellMO.WFS_CELL_GLO + this.rootName;
    }
 
    @Override protected String getClientCellClassName(ClientSession clientSession, ClientCapabilities capabilities) {
        return "org.jdesktop.wonderland.client.cell.WFSCell";
    }
    
    @Override
    public CellConfig getClientStateData(ClientSession clientSession,ClientCapabilities capabilities) {
        return null;
    }
}
