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
package org.jdesktop.wonderland.modules.jmecolladaloader.client.cell;

import org.jdesktop.wonderland.client.cell.CellCache;
import org.jdesktop.wonderland.common.cell.CellID;

/**
 * A cell that can move
 * 
 * TODO DELETE ME
 * 
 * @author paulby
 * @deprecated
 */
public class MovableCell extends JmeColladaCell {
    
    public MovableCell(CellID cellID, CellCache cellCache) {
        super(cellID, cellCache);
    }

    /**
     * Returns the URI of the model asset.
     * 
     * @return The asset URI
     */
    @Override
    public String getModelURI() {
        return getClass().getClassLoader().getResource("org/jdesktop/wonderland/client/resources/jme/sphere2.dae").toExternalForm();
    }
}
