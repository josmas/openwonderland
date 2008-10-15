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
import org.jdesktop.wonderland.client.jme.cellrenderer.CellRendererJME;
import org.jdesktop.wonderland.client.jme.input.test.MouseEvent3DLogger;
import org.jdesktop.wonderland.client.jme.input.test.SpinObjectEventListener;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.modules.testcells.client.jme.cellrenderer.ShapeRenderer;

/**
 * Test for mouse over spin
 * 
 * @deprecated
 * @author paulby
 */
public class MouseSpinCell extends SimpleShapeCell {
    
    public MouseSpinCell(CellID cellID, CellCache cellCache) {
        super(cellID, cellCache);
    }
    
    @Override
    protected CellRenderer createCellRenderer(RendererType rendererType) {
        CellRenderer ret = super.createCellRenderer(rendererType);

        SpinAnimObjectEventListener spinEventListener = new SpinAnimObjectEventListener();
        spinEventListener.addToEntity(((CellRendererJME)ret).getEntity());

        return ret;
    }
    

}
