/**
 * Project Looking Glass
 *
 * $RCSfile: SimpleTerrainCellGLO.java,v $
 *
 * Copyright (c) 2004-2007, Sun Microsystems, Inc., All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * $Revision: 1.13 $
 * $Date: 2007/10/17 17:11:11 $
 * $State: Exp $
 */
package org.jdesktop.wonderland.server.cell;

import com.jme.bounding.BoundingBox;
import com.jme.bounding.BoundingVolume;
import com.jme.math.Vector3f;
import com.sun.sgs.app.ClientSession;
import org.jdesktop.wonderland.common.ExperimentalAPI;
import org.jdesktop.wonderland.common.cell.CellTransform;
import org.jdesktop.wonderland.common.cell.ClientCapabilities;
import org.jdesktop.wonderland.common.cell.setup.ModelCellSetup;
import org.jdesktop.wonderland.server.ChecksumManagerMO;
import org.jdesktop.wonderland.server.setup.BasicCellMOHelper;
import org.jdesktop.wonderland.server.setup.BasicCellMOSetup;
import org.jdesktop.wonderland.server.setup.BeanSetupMO;
import org.jdesktop.wonderland.server.setup.CellMOSetup;

/**
 * A cell for static terrain (or building) geometry
 * @author paulby
 */
@ExperimentalAPI
public class SimpleTerrainCellMO extends CellMO
    implements BeanSetupMO { 
    
    private String filename;
    private String baseUrl;
    	
    public SimpleTerrainCellMO() {
    }
    
    public SimpleTerrainCellMO(int row, int column, String filename, float size) {
        this(new Vector3f(row*size+size/2, 0, column*size+size/2), size);
        
        this.filename = filename;
    }
    
    public SimpleTerrainCellMO(Vector3f center, float size) {
        super(new BoundingBox(new Vector3f(), size, size, size), new CellTransform(null, center));
    }
    
    @Override protected String getClientCellClassName(ClientSession clientSession, ClientCapabilities capabilities) {
        return "org.jdesktop.wonderland.client.extracells.SimpleTerrainCell";
    }

    @Override
    public ModelCellSetup getClientSetupData(ClientSession clientSession, ClientCapabilities capabilities) {
	String checksum = ChecksumManagerMO.getChecksum(filename);
	return new ModelCellSetup(baseUrl, filename, checksum);
    }

    public void setupCell(CellMOSetup setupData) {
        BasicCellMOSetup<ModelCellSetup> setup =
            (BasicCellMOSetup<ModelCellSetup>) setupData;

        super.setupCell(setup);

        this.filename = setup.getCellSetup().getModelFile();
    }

    public void reconfigureCell(CellMOSetup setupData) {
        BasicCellMOSetup<ModelCellSetup> setup =
            (BasicCellMOSetup<ModelCellSetup>) setupData;

        super.reconfigureCell(setup);
    
        setupCell(setup);
    }

     /**
     * Return a new CellMOSetup Java bean class that represents the current
     * state of the cell.
     * 
     * @return a JavaBean representing the current state
     */
    public CellMOSetup getCellMOSetup() {
        /* Create a new BasicCellMOSetup and populate its members */
        BasicCellMOSetup<ModelCellSetup> setup = new BasicCellMOSetup<ModelCellSetup>();
        setup.setCellMOClassName(this.getClass().getName());
        setup.setCellSetup(this.getClientSetupData(null, null));
        
        /* Set the bounds of the cell */
        BoundingVolume bounds = this.getLocalBounds();
        if (bounds != null) {
            setup.setBoundsType(BasicCellMOHelper.getBoundsType(bounds));
            setup.setBoundsRadius(BasicCellMOHelper.getBoundsRadius(bounds));
        }
        
        /* Set the origin, scale, and rotation of the cell */
        CellTransform transform = this.getTransform();
        if (transform != null) {
            setup.setOrigin(BasicCellMOHelper.getTranslation(transform));
            setup.setRotation(BasicCellMOHelper.getRotation(transform));
            setup.setScale(BasicCellMOHelper.getScaling(transform));
        }
        return setup;
    }
}
