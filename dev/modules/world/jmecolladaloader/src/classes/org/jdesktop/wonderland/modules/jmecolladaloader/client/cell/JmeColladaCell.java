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
package org.jdesktop.wonderland.modules.jmecolladaloader.client.cell;

import org.jdesktop.wonderland.client.cell.*;
import org.jdesktop.wonderland.modules.jmecolladaloader.client.jme.cellrenderer.JmeColladaRenderer;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.config.CellConfig;
import org.jdesktop.wonderland.modules.jmecolladaloader.common.cell.config.ColladaCellConfig;

/**
 * Client side cell for rendering JME content
 * 
 * @author paulby
 */
public class JmeColladaCell extends Cell {
    
    /* The URI of the model asset */
    private String modelURI = null;
    
    public JmeColladaCell(CellID cellID, CellCache cellCache) {
        super(cellID, cellCache);
    }
    
    /**
     * Called when the cell is initially created and any time there is a 
     * major configuration change. The cell will already be attached to it's parent
     * before the initial call of this method
     * 
     * @param setupData
     */
    @Override
    public void configure(CellConfig config) {
        super.configure(config);
        ColladaCellConfig colladaConfig = (ColladaCellConfig)config;
        this.modelURI = colladaConfig.getModelURI();
    }
    
    @Override
    protected CellRenderer createCellRenderer(RendererType rendererType) {
        CellRenderer ret = null;
        switch(rendererType) {
            case RENDERER_2D :
                // No 2D Renderer yet
                break;
            case RENDERER_JME :
                ret= new JmeColladaRenderer(this);
                break;                
        }
        
        return ret;
    }
    
    /**
     * Returns the URI of the model asset.
     * 
     * TODO shouldn't this be a URL instead of a String ?
     * 
     * @return The asset URI
     */
    public String getModelURI() {
        System.out.println("URL "+modelURI);
        return this.modelURI;
    }
}
