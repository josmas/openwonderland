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
package org.jdesktop.wonderland.client.jme;

import com.jme.bounding.BoundingVolume;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.mtgame.RenderComponent;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.cell.CellCacheBasicImpl;
import org.jdesktop.wonderland.client.cell.CellRenderer;
import org.jdesktop.wonderland.client.cell.view.ViewCell;
import org.jdesktop.wonderland.client.comms.CellClientSession;
import org.jdesktop.wonderland.client.jme.cellrenderer.CellRendererJME;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.CellStatus;
import org.jdesktop.wonderland.common.cell.CellTransform;
import org.jdesktop.wonderland.common.cell.config.CellConfig;

/**
*
* @author paulby
*/
/**
 * Concrete implementation of CellCache for the JME Client
 */
public class JmeCellCache extends CellCacheBasicImpl {

    // a list of top-level cells we have added
    private final Set<Entity> rootEntities;

    public JmeCellCache(CellClientSession session, ClassLoader loader) {
        super(session, loader,
              session.getCellCacheConnection(),
              session.getCellChannelConnection());

        rootEntities = Collections.synchronizedSet(new HashSet<Entity>());
    }

    @Override
    public Cell loadCell(CellID cellID,
            String className,
            BoundingVolume localBounds,
            CellID parentCellID,
            CellTransform cellTransform,
            CellConfig setup,
            String cellName) {
        Cell ret = super.loadCell(cellID,
                           className,
                           localBounds,
                           parentCellID,
                           cellTransform,
                           setup,
                           cellName);
//        logger.warning("Loaded Cell "+ret.getClass().getName());


        // Renderers are now responsible for adding themselves to the scene
        // when their setStatus call is called

        return ret;
    }

    @Override
    public void unloadCell(CellID cellID) {
        Cell cell = getCell(cellID);
        CellRenderer rend = cell.getCellRenderer(Cell.RendererType.RENDERER_JME);
        if (cell!=null && rend!=null) {
            if (rend instanceof CellRendererJME) {
                System.err.println("Unload cell !");
                Entity e = ((CellRendererJME) rend).getEntity();
                ClientContextJME.getWorldManager().removeEntity(e);
                rootEntities.remove(e);
            } else {
                logger.warning("Unexpected renderer class "+rend.getClass().getName());
            }
        }
        super.unloadCell(cellID);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setViewCell(ViewCell viewCell) {
        super.setViewCell(viewCell);
        ClientContextJME.getViewManager().register(viewCell);

        // TODO this will not work for federation, need to determine primary view cell in a
        // higher level manager
        ClientContextJME.getViewManager().setPrimaryViewCell(viewCell);
    }

    /**
     * Get all root entities in this cache
     * @return the set of root entities in this cache
     */
    public Set<Entity> getRootEntities() {
        return rootEntities;
    }


    /**
     * Remove all top-level entities in this cache from the scene graph
     */
    public void detachRootEntities() {
        synchronized (rootEntities) {
            for (Entity e : rootEntities) {
                ClientContextJME.getWorldManager().removeEntity(e);
            }
        }

        rootEntities.clear();
    }

    /**
     * Traverse up the cell hierarchy and return the first Entity
     * @param cell
     * @return
     */
//    private Entity findParentEntity(Cell cell) {
//        if (cell==null)
//            return null;
//
//        CellRenderer rend = cell.getCellRenderer(Cell.RendererType.RENDERER_JME);
//        if (cell!=null && rend!=null) {
//            if (rend instanceof CellRendererJME) {
////                    System.out.println("FOUND PARENT ENTITY on CELL "+cell.getName());
//                return ((CellRendererJME)rend).getEntity();
//            }
//        }
//
//        return findParentEntity(cell.getParent());
//    }

}


