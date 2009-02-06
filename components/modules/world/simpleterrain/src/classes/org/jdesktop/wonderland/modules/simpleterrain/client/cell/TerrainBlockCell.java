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
package org.jdesktop.wonderland.modules.simpleterrain.client.cell;

import com.jme.renderer.ColorRGBA;
import org.jdesktop.wonderland.client.cell.*;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.state.CellClientState;
import org.jdesktop.wonderland.common.cell.config.jme.MaterialJME;
import org.jdesktop.wonderland.modules.simpleterrain.client.jme.cellrenderer.TerrainBlockRenderer;

/**
 * Simple shape
 * 
 * @author paulby
 */
public class TerrainBlockCell extends Cell {

    /**
     * @param cellID
     * @param cellCache
     * @param mass
     */
    public TerrainBlockCell(CellID cellID, CellCache cellCache) {
        super(cellID, cellCache);
    }
    
    @Override
    public void setClientState(CellClientState configData) {
        super.setClientState(configData);
    }

    
    @Override
    protected CellRenderer createCellRenderer(RendererType rendererType) {
        CellRenderer ret = null;
        switch(rendererType) {
            case RENDERER_2D :
                // No 2D Renderer yet
                break;
            case RENDERER_JME :
                ret= new TerrainBlockRenderer(this);
                break;                
        }
        
        return ret;
    }


}
