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
package org.jdesktop.wonderland.modules.affordances.client.cell;

import org.jdesktop.wonderland.client.cell.*;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.state.CellClientState;
import org.jdesktop.wonderland.common.cell.config.jme.MaterialJME;
import org.jdesktop.wonderland.modules.affordances.client.jme.cellrenderer.AffordanceTestCellRenderer;
import org.jdesktop.wonderland.modules.affordances.common.cell.config.AffordanceTestCellConfig;

/**
 * A test cell for affordances.
 * 
 * @author Jordan Slott <jslott@dev.java.net>
 */
public class AffordanceTestCell extends Cell {

    private AffordanceTestCellConfig.Shape shape;
    private MaterialJME materialJME;

    /**
     * Mass of zero will result in a static rigid body, non zero will be dynamic
     * @param cellID
     * @param cellCache
     */
    public AffordanceTestCell(CellID cellID, CellCache cellCache) {
        super(cellID, cellCache);
    }
    
    @Override
    public void setClientState(CellClientState configData) {
        super.setClientState(configData);
        AffordanceTestCellConfig c = (AffordanceTestCellConfig) configData;
        this.shape = c.getShape();
        this.materialJME = c.getMaterialJME();
    }

    
    @Override
    protected CellRenderer createCellRenderer(RendererType rendererType) {
        if (rendererType == RendererType.RENDERER_JME) {
            return new AffordanceTestCellRenderer(this);
        }
        return super.createCellRenderer(rendererType);
    }

    public AffordanceTestCellConfig.Shape getShape() {
        return shape;
    }

    public MaterialJME getMaterialJME() {
        return materialJME;
    }
}
