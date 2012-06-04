/**
 * Open Wonderland
 *
 * Copyright (c) 2012, Open Wonderland Foundation, All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above copyright and
 * this condition.
 *
 * The contents of this file are subject to the GNU General Public License,
 * Version 2 (the "License"); you may not use this file except in compliance
 * with the License. A copy of the License is available at
 * http://www.opensource.org/licenses/gpl-license.php.
 *
 * The Open Wonderland Foundation designates this particular file as subject to
 * the "Classpath" exception as provided by the Open Wonderland Foundation in
 * the License file that accompanied this code.
 */

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

import java.io.InputStream;
import java.net.URL;
import java.util.List;
import org.jdesktop.wonderland.common.cell.CellID;

/**
 * Client side cell for rendering JME content
 * 
 * @author paulby
 */
public class ModelCell extends Cell implements AssetPreloader {
    
    public ModelCell(CellID cellID, CellCache cellCache) {
        super(cellID, cellCache);
    }
    
    @Override
    protected CellRenderer createCellRenderer(RendererType rendererType) {
        ModelCellComponent mc = getComponent(ModelCellComponent.class);
        return mc.getCellRenderer(rendererType, this);
    }

    public List<URL> getAssets() {
        ModelCellComponent mc = getComponent(ModelCellComponent.class);
        return mc.getAssets();
    }

    public List<URL> assetLoaded(URL url, InputStream loaded) {
        ModelCellComponent mc = getComponent(ModelCellComponent.class);
        return mc.assetLoaded(url, loaded);
    }
}
