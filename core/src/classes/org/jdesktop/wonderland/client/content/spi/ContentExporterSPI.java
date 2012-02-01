/**
 * Open Wonderland
 *
 * Copyright (c) 2011 - 2012, Open Wonderland Foundation, All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * The Open Wonderland Foundation designates this particular file as
 * subject to the "Classpath" exception as provided by the Open Wonderland
 * Foundation in the License file that accompanied this code.
 */
package org.jdesktop.wonderland.client.content.spi;

import com.jme.math.Vector3f;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.common.cell.CellTransform;

/**
 *
 * @author JagWire
 */
public interface ContentExporterSPI {
    
    /**
     * Returns a list of cell classes with which to apply this exporter to.
     * 
     * @return an array of cell classes. Cell.class for all cells, or subclasses
     * thereof for specific cases.
     */
    public Class[] getCellClasses();
    
    /**
     * Exports a cell. The class implementing this interface gets to decide what
     * 'export' means.
     * 
     * @param cells the cells to export
     * @param origin the location to export from. Cells will be captured 
     * with offsets relative to the given position.
     */
    public void exportCells(Cell[] cells, CellTransform origin);
    
}
