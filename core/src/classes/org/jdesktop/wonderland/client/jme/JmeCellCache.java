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
import org.jdesktop.mtgame.Entity;
import org.jdesktop.mtgame.RenderComponent;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.cell.CellCacheBasicImpl;
import org.jdesktop.wonderland.client.cell.CellRenderer;
import org.jdesktop.wonderland.client.cell.view.ViewCell;
import org.jdesktop.wonderland.client.comms.CellClientSession;
import org.jdesktop.wonderland.client.jme.cellrenderer.CellRendererJME;
import org.jdesktop.wonderland.client.jme.input.test.EnterExitEvent3DLogger;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.CellTransform;
import org.jdesktop.wonderland.common.cell.config.CellConfig;

/**
*
* @author paulby
*/
/**
 * Concrete implementation of CellCache for the JME Client
 */
class JmeCellCache extends CellCacheBasicImpl {

    public JmeCellCache(CellClientSession session, ClassLoader loader) {
        super(session, loader,
              session.getCellCacheConnection(),
              session.getCellChannelConnection());
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
//            logger.warning("Loaded Cell "+ret.getClass().getName());

        CellRenderer rend = ret.getCellRenderer(Cell.RendererType.RENDERER_JME);
        if (ret!=null && rend!=null) {
            if (rend instanceof CellRendererJME) {
                Entity parentEntity= findParentEntity(ret.getParent());
                Entity thisEntity = ((CellRendererJME)rend).getEntity();

                thisEntity.addComponent(CellRefComponent.class, new CellRefComponent(ret));

//                  Test code for mouse event listeners
//		    MouseEvent3DLogger mouseEventListener =
//			new MouseEvent3DLogger(className+"_"+cellID);
//		    mouseEventListener.addToEntity(thisEntity);

//                  Test code for enter/exit event listeners
//		    EnterExitEvent3DLogger enterExitEventListener = 
//			new EnterExitEvent3DLogger(className+"_"+cellID);
//		    enterExitEventListener.addToEntity(thisEntity);

                if (parentEntity!=null)
                    parentEntity.addEntity(thisEntity);
                else
                    ClientContextJME.getWorldManager().addEntity(thisEntity);

//		    /* TODO: temporary
//		    MouseEvent3DLogger mouseEventListener =
//			new MouseEvent3DLogger(className+"_"+cellID);
//		    mouseEventListener.addToEntity(thisEntity);
//		    */

                /* TODO: temporary
                KeyEvent3DLogger keyEventListener =
                    new KeyEvent3DLogger(className+"_"+cellID);
                keyEventListener.addToEntity(thisEntity);
                */

                /* TODO: temporary
                SpinObjectEventListener spinEventListener = new SpinObjectEventListener();
                spinEventListener.addToEntity(thisEntity);
                */

                // Figure out the correct parent entity for this cells entity.
                if (parentEntity!=null && thisEntity!=null) {
                    RenderComponent parentRendComp = (RenderComponent) parentEntity.getComponent(RenderComponent.class);
                    RenderComponent thisRendComp = (RenderComponent)thisEntity.getComponent(RenderComponent.class);
                    if (parentRendComp!=null && parentRendComp.getSceneRoot()!=null && thisRendComp!=null) {
                        thisRendComp.setAttachPoint(parentRendComp.getSceneRoot());
                    }
                }

            } else
                logger.warning("Unexpected renderer class "+rend.getClass().getName());
        } else {
            logger.info("No Entity for Cell "+ret.getClass().getName());
        }
        return ret;
    }

    @Override
    public void unloadCell(CellID cellID) {
        Cell cell = getCell(cellID);
        CellRenderer rend = cell.getCellRenderer(Cell.RendererType.RENDERER_JME);
        if (cell!=null && rend!=null) {
            if (rend instanceof CellRendererJME)
                ClientContextJME.getWorldManager().removeEntity(((CellRendererJME)rend).getEntity());
            else
                logger.warning("Unexpected renderer class "+rend.getClass().getName());
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
     * Traverse up the cell hierarchy and return the first Entity
     * @param cell
     * @return
     */
    private Entity findParentEntity(Cell cell) {
        if (cell==null)
            return null;

        CellRenderer rend = cell.getCellRenderer(Cell.RendererType.RENDERER_JME);
        if (cell!=null && rend!=null) {
            if (rend instanceof CellRendererJME) {
//                    System.out.println("FOUND PARENT ENTITY on CELL "+cell.getName());
                return ((CellRendererJME)rend).getEntity();
            }
        }

        return findParentEntity(cell.getParent());
    }
}


