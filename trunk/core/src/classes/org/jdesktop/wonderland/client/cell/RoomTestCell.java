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
package org.jdesktop.wonderland.client.cell;

import org.jdesktop.wonderland.client.jme.cellrenderer.RoomTestRenderer;
import org.jdesktop.wonderland.common.cell.CellID;

/**
 * Test Cell for use until WFS is integrated, this will be removed.
 * 
 * @deprecated
 * @author paulby
 */
public class RoomTestCell extends Cell {
    
    public RoomTestCell(CellID cellID) {
        super(cellID);
    }
    
    @Override
    protected CellRenderer createCellRenderer(RendererType rendererType) {
        CellRenderer ret = null;
        switch(rendererType) {
            case RENDERER_2D :
                // No 2D Renderer yet
                break;
            case RENDERER_JME :
                ret= new RoomTestRenderer(this);
                break;                
        }
        
        return ret;
    }
    

}
