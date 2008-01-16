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

import javax.vecmath.Vector3d;
import org.jdesktop.wonderland.common.Math3DUtils;
import org.jdesktop.wonderland.common.cell.setup.ModelCellSetup;
import org.jdesktop.wonderland.server.ChecksumManagerGLO;
import org.jdesktop.wonderland.server.setup.BasicCellMOSetup;
import org.jdesktop.wonderland.server.setup.BeanSetupMO;
import org.jdesktop.wonderland.server.setup.CellMOSetup;

/**
 *
 * @author paulby
 */
public class SimpleTerrainCellMO extends CellMO
    implements BeanSetupMO { 
    
    private String filename;
    private String baseUrl;
    	
    public SimpleTerrainCellMO() {
    }
    
    public SimpleTerrainCellMO(int row, int column, String filename, float size) {
        this(new Vector3d(row*size+size/2, 0, column*size+size/2), size);
        
        this.filename = filename;
    }
    
    public SimpleTerrainCellMO(Vector3d center, float size) {
        super(Math3DUtils.createBoundingBox(center, size), Math3DUtils.createOriginM4d(center));
    }
    
    public String getClientCellClassName() {
        return "org.jdesktop.lg3d.wonderland.darkstar.client.cell.SimpleTerrainCell";
    }

    public ModelCellSetup getSetupData() {
	String checksum = ChecksumManagerGLO.getChecksum(filename);
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

    public CellMOSetup getCellGLOSetup() {
        return new BasicCellMOSetup<ModelCellSetup>(getLocalBounds(),
            getTransform(), getClass().getName(), getSetupData());
    }

}
