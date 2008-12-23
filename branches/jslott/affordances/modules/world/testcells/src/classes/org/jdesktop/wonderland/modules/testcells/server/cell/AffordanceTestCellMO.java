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
package org.jdesktop.wonderland.modules.testcells.server.cell;

import org.jdesktop.wonderland.server.cell.*;
import com.jme.bounding.BoundingBox;
import com.jme.math.Vector3f;
import org.jdesktop.wonderland.common.cell.setup.BasicCellSetup;
import org.jdesktop.wonderland.common.ExperimentalAPI;
import org.jdesktop.wonderland.common.cell.CellTransform;
import org.jdesktop.wonderland.common.cell.ClientCapabilities;
import org.jdesktop.wonderland.common.cell.config.CellConfig;
import org.jdesktop.wonderland.common.cell.config.jme.MaterialJME;
import org.jdesktop.wonderland.modules.testcells.common.cell.config.AffordanceTestCellConfig;
import org.jdesktop.wonderland.server.comms.WonderlandClientID;


/**
 * Cell to test cell affordances
 * 
 * @author paulby
 */
@ExperimentalAPI
public class AffordanceTestCellMO extends CellMO {
    
    private AffordanceTestCellConfig.Shape shape;
    private float mass;
    private MaterialJME materialJME = null;
    private String affordanceType = null;
    
    /** Default constructor, used when cell is created via WFS */
    public AffordanceTestCellMO() {
        this(new Vector3f(), 1, "TRANSLATE");
    }

    public AffordanceTestCellMO(Vector3f center, float size, String affordanceType) {
        this(center, size, AffordanceTestCellConfig.Shape.BOX, affordanceType);
    }

    public AffordanceTestCellMO(Vector3f center, float size, AffordanceTestCellConfig.Shape shape, String affordanceType) {
        this(center, size, shape, 0f, affordanceType);
    }
    
    public AffordanceTestCellMO(Vector3f center, float size, AffordanceTestCellConfig.Shape shape, float mass, String affordanceType) {
        this(center, size, shape, mass, null, affordanceType);
    }

    public AffordanceTestCellMO(Vector3f center, float size, AffordanceTestCellConfig.Shape shape, float mass, MaterialJME materialJME, String affordanceType) {
        super(new BoundingBox(new Vector3f(), size, size, size), new CellTransform(null, center));
        this.shape = shape;
        this.mass = mass;
        this.materialJME = materialJME;
        this.affordanceType = affordanceType;
    }
    
    @Override
    protected String getClientCellClassName(WonderlandClientID clientID, ClientCapabilities capabilities) {
        return "org.jdesktop.wonderland.modules.testcells.client.cell.AffordanceTestCell";
    }

    @Override
    public CellConfig getCellConfig(WonderlandClientID clientID, ClientCapabilities capabilities) {
        return new AffordanceTestCellConfig(shape, mass, materialJME, affordanceType);
    }

    @Override
    public void setupCell(BasicCellSetup setup) {
        super.setupCell(setup);
    }

    @Override
    public void reconfigureCell(BasicCellSetup setup) {
        super.reconfigureCell(setup);
        setupCell(setup);
    }
}
