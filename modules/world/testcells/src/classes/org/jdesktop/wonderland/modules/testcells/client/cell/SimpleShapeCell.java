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
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.config.CellConfig;
import org.jdesktop.wonderland.modules.testcells.client.jme.cellrenderer.ShapeRenderer;
import org.jdesktop.wonderland.modules.testcells.common.cell.config.SimpleShapeConfig;

/**
 * Simple shape
 * 
 * @author paulby
 */
public class SimpleShapeCell extends Cell {

    private SimpleShapeConfig.Shape shape;

    public SimpleShapeCell(CellID cellID, CellCache cellCache) {
        super(cellID, cellCache);
    }
    
    @Override
    public void configure(CellConfig configData) {
        super.configure(configData);
        SimpleShapeConfig c = (SimpleShapeConfig) configData;
        this.shape = c.getShape();
    }

    
    @Override
    protected CellRenderer createCellRenderer(RendererType rendererType) {
        CellRenderer ret = null;
        switch(rendererType) {
            case RENDERER_2D :
                // No 2D Renderer yet
                break;
            case RENDERER_JME :
                ret= new ShapeRenderer(this);
                break;                
        }
        
        return ret;
    }

    public SimpleShapeConfig.Shape getShape() {
        return shape;
    }

}
