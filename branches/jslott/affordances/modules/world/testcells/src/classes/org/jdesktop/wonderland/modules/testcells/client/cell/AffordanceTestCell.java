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
package org.jdesktop.wonderland.modules.testcells.client.cell;

import org.jdesktop.wonderland.client.cell.*;
import org.jdesktop.wonderland.client.jme.affordances.ResizeAffordance;
import org.jdesktop.wonderland.client.jme.affordances.RotateAffordance;
import org.jdesktop.wonderland.client.jme.affordances.TranslateAffordance;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.CellStatus;
import org.jdesktop.wonderland.common.cell.config.CellConfig;
import org.jdesktop.wonderland.common.cell.config.jme.MaterialJME;
import org.jdesktop.wonderland.modules.testcells.client.jme.cellrenderer.AffordanceCellRenderer;
import org.jdesktop.wonderland.modules.testcells.common.cell.config.AffordanceTestCellConfig;
import sun.reflect.generics.visitor.Reifier;

/**
 * Simple shape
 * 
 * @author paulby
 */
public class AffordanceTestCell extends Cell {

    private AffordanceTestCellConfig.Shape shape;
    private MaterialJME materialJME;
    private float mass;
    private String affordanceType;

    /**
     * Mass of zero will result in a static rigid body, non zero will be dynamic
     * @param cellID
     * @param cellCache
     * @param mass
     */
    public AffordanceTestCell(CellID cellID, CellCache cellCache) {
        super(cellID, cellCache);
    }
    
    @Override
    public void configure(CellConfig configData) {
        super.configure(configData);
        AffordanceTestCellConfig c = (AffordanceTestCellConfig) configData;
        this.shape = c.getShape();
        this.mass = c.getMass();
        this.materialJME = c.getMaterialJME();
        this.affordanceType = c.getAffordanceType();
    }

    @Override
    public boolean setStatus(CellStatus status) {
        switch (status) {
            case ACTIVE:
                // Attach the appropriate affordance
                if (affordanceType.equals("TRANSLATE") == true) {
                    TranslateAffordance aff = TranslateAffordance.addToCell(this);
                }
                else if (affordanceType.equals("ROTATE") == true) {
                    RotateAffordance aff = RotateAffordance.addToCell(this);
                }
                else if (affordanceType.equals("RESIZE") == true) {
                    ResizeAffordance aff = ResizeAffordance.addToCell(this);
                }
                break;
        }
        return super.setStatus(status);
    }

    
    
    @Override
    protected CellRenderer createCellRenderer(RendererType rendererType) {
        CellRenderer ret = null;
        switch(rendererType) {
            case RENDERER_2D :
                // No 2D Renderer yet
                break;
            case RENDERER_JME :
                ret= new AffordanceCellRenderer(this);
                break;                
        }
        
        return ret;
    }

    public AffordanceTestCellConfig.Shape getShape() {
        return shape;
    }

    public float getMass() {
        return mass;
    }

    public MaterialJME getMaterialJME() {
        return materialJME;
    }

    public String getAffordanceType() {
        return affordanceType;
    }
}
