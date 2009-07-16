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
package org.jdesktop.wonderland.client.cell;

import com.jme.scene.Node;
import org.jdesktop.wonderland.client.jme.artimport.ModelLoader;

/**
 *
 * @author paulby
 */
public abstract class ModelCellComponent extends CellComponent {
    public ModelCellComponent(Cell cell) {
        super(cell);
    }

    /**
     * Instantiate and return the cell renderer
     * @param type
     * @param cell
     * @return
     */
    public abstract CellRenderer getCellRenderer(Cell.RendererType type, Cell cell);

    /**
     * Load and return the model. The node returned is the model BG
     *
     * @return
     */
    public abstract Node loadModel();
}
