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
package org.jdesktop.wonderland.modules.affordances.client.cell;

import org.jdesktop.wonderland.client.cell.*;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.config.CellConfig;
import org.jdesktop.wonderland.common.cell.config.jme.MaterialJME;
import org.jdesktop.wonderland.modules.affordances.client.jme.cellrenderer.AffordanceTestCellRenderer;
import org.jdesktop.wonderland.modules.affordances.common.cell.config.AffordanceTestCellConfig;

/**
 * Simple shape
 * 
 * @author paulby
 */
public class AffordanceTestCell extends Cell {

    private AffordanceTestCellConfig.Shape shape;
    private MaterialJME materialJME;

    /**
     * Mass of zero will result in a static rigid body, non zero will be dynamic
     * @param cellID
     * @param cellCache
     * @param mass
     */
    public AffordanceTestCell(CellID cellID, CellCache cellCache) {
        super(cellID, cellCache);
        addComponent(new ChannelComponent(this));
        addComponent(new MovableComponent(this));
    }
    
    @Override
    public void configure(CellConfig configData) {
        super.configure(configData);
        AffordanceTestCellConfig c = (AffordanceTestCellConfig) configData;
        this.shape = c.getShape();
        this.materialJME = c.getMaterialJME();
    }

    
    @Override
    protected CellRenderer createCellRenderer(RendererType rendererType) {
        CellRenderer ret = null;
        switch(rendererType) {
            case RENDERER_2D :
                // No 2D Renderer yet
                break;
            case RENDERER_JME :
                ret= new AffordanceTestCellRenderer(this);
                break;                
        }
        
        return ret;
    }

    public AffordanceTestCellConfig.Shape getShape() {
        return shape;
    }

    public MaterialJME getMaterialJME() {
        return materialJME;
    }
}
