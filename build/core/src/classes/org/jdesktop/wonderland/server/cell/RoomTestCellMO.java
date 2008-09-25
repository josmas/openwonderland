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
package org.jdesktop.wonderland.server.cell;

import com.jme.bounding.BoundingBox;
import com.jme.bounding.BoundingVolume;
import com.jme.math.Vector3f;
import com.sun.sgs.app.ClientSession;
import org.jdesktop.wonderland.server.cell.setup.BasicCellSetup;
import org.jdesktop.wonderland.common.ExperimentalAPI;
import org.jdesktop.wonderland.common.cell.CellTransform;
import org.jdesktop.wonderland.common.cell.ClientCapabilities;
import org.jdesktop.wonderland.common.cell.config.CellConfig;
import org.jdesktop.wonderland.common.cell.config.StaticModelCellConfig;
import org.jdesktop.wonderland.server.cell.setup.ColladaCellSetup;
import org.jdesktop.wonderland.server.setup.BasicCellSetupHelper;
import org.jdesktop.wonderland.server.setup.BeanSetupMO;


/**
 * Test Cell for use until WFS is integrated, this will be removed.
 * 
 * @deprecated
 * @author paulby
 */
@ExperimentalAPI
public class RoomTestCellMO extends CellMO
    implements BeanSetupMO { 
    
    private String filename;
    private String baseUrl;
    	
    /** Default constructor, used when cell is created via WFS */
    public RoomTestCellMO() {
    }

    public RoomTestCellMO(Vector3f center, float size) {
        super(new BoundingBox(new Vector3f(), size, size, size), new CellTransform(null, center));
    }
    
    @Override protected String getClientCellClassName(ClientSession clientSession, ClientCapabilities capabilities) {
        return "org.jdesktop.wonderland.client.cell.RoomTestCell";
    }

    @Override
    public CellConfig getCellConfig(ClientSession clientSession, ClientCapabilities capabilities) {
        return new StaticModelCellConfig(this.filename);
    }

    @Override
    public void setupCell(BasicCellSetup setup) {
        super.setupCell(setup);
        this.filename = ((ColladaCellSetup)setup).getModel();
    }

    @Override
    public void reconfigureCell(BasicCellSetup setup) {
        super.reconfigureCell(setup);
        setupCell(setup);
    }

     /**
     * Return a new CellMOSetup Java bean class that represents the current
     * state of the cell.
     * 
     * @return a JavaBean representing the current state
     */
    public BasicCellSetup getCellMOSetup() {
        /* Create a new BasicCellSetup and populate its members */
        ColladaCellSetup setup = new ColladaCellSetup();
        setup.setModel(this.filename);
        
        /* Set the bounds of the cell */
        BoundingVolume bounds = this.getLocalBounds();
        if (bounds != null) {
            setup.setBounds(BasicCellSetupHelper.getSetupBounds(bounds));
        }

        /* Set the origin, scale, and rotation of the cell */
        CellTransform transform = this.getLocalTransform(null);
        if (transform != null) {
            setup.setOrigin(BasicCellSetupHelper.getSetupOrigin(transform));
            setup.setRotation(BasicCellSetupHelper.getSetupRotation(transform));
            setup.setScaling(BasicCellSetupHelper.getSetupScaling(transform));
        }
        return setup;
    }
}
