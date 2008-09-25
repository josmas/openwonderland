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

import java.net.URL;
import org.jdesktop.wonderland.client.jme.cellrenderer.StaticModelRenderer;
import org.jdesktop.wonderland.client.modules.ModulePluginList;
import org.jdesktop.wonderland.client.modules.ModuleUtils;
import org.jdesktop.wonderland.common.cell.CellID;

/**
 * Client side class for world root cells.
 * 
 * @author paulby
 * @deprecated
 */
public class StaticModelCell extends Cell {
    
    public StaticModelCell(CellID cellID, CellCache cellCache) {
        super(cellID, cellCache);
        
//        ModulePluginList list = ModuleUtils.fetchPluginJars();
//        if (list == null) {
//            System.out.println("NULL PLUGIN LIST");
//            return;
//        }
//        for (String uri : list.getJarURIs()) {
//            System.out.println("PLUGIN " + uri);
//            try {
//                URL url = new URL(uri);
//                url.openStream();
//            } catch (Exception excp) {
//                excp.printStackTrace();
//            }
//        }
//        try {
//            URL url = new URL("wla://mpk20/sphere2.dae");
//            url.openStream();
//        } catch (Exception excp) {
//        }
    }
    
    @Override
    protected CellRenderer createCellRenderer(RendererType rendererType) {
        CellRenderer ret = null;
        switch(rendererType) {
            case RENDERER_2D :
                // No 2D Renderer yet
                break;
            case RENDERER_JME :
                ret= new StaticModelRenderer(this);
                break;                
        }
        
        return ret;
    }
    

}
