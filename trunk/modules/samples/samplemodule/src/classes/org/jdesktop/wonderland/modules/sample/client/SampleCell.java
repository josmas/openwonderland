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
package org.jdesktop.wonderland.modules.sample.client;

import java.util.ResourceBundle;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.cell.Cell.RendererType;
import org.jdesktop.wonderland.client.cell.CellCache;
import org.jdesktop.wonderland.client.cell.CellRenderer;
import org.jdesktop.wonderland.client.cell.annotation.UsesCellComponent;
import org.jdesktop.wonderland.client.contextmenu.ContextMenuActionListener;
import org.jdesktop.wonderland.client.contextmenu.ContextMenuItem;
import org.jdesktop.wonderland.client.contextmenu.ContextMenuItemEvent;
import org.jdesktop.wonderland.client.contextmenu.SimpleContextMenuItem;
import org.jdesktop.wonderland.client.contextmenu.cell.ContextMenuComponent;
import org.jdesktop.wonderland.client.contextmenu.spi.ContextMenuFactorySPI;
import org.jdesktop.wonderland.client.scenemanager.event.ContextEvent;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.CellStatus;
import org.jdesktop.wonderland.common.cell.state.CellClientState;
import org.jdesktop.wonderland.modules.sample.common.SampleCellClientState;

/**
 * Client-side cell for rendering JME content
 * 
 * @author jkaplan
 * @author Ronny Standtke <ronny.standtke@fhnw.ch>
 */
public class SampleCell extends Cell {

    private static final ResourceBundle BUNDLE = ResourceBundle.getBundle(
            "org/jdesktop/wonderland/modules/sample/client/resources/Bundle");

    /* The type of shape: BOX or SPHERE */
    private String shapeType = null;
    private SampleRenderer cellRenderer = null;
    @UsesCellComponent
    ContextMenuComponent menuComponent;

    public SampleCell(CellID cellID, CellCache cellCache) {
        super(cellID, cellCache);
    }

    /**
     * Called when the cell is initially created and any time there is a major
     * configuration change. The cell will already be attached to it's parent
     * before the initial call of this method
     * 
     * @param clientState
     */
    @Override
    public void setClientState(CellClientState clientState) {
        super.setClientState(clientState);
        shapeType = ((SampleCellClientState) clientState).getShapeType();
        if (cellRenderer != null) {
            cellRenderer.updateShape();
        }
    }

    @Override
    protected CellRenderer createCellRenderer(RendererType rendererType) {
        if (rendererType == RendererType.RENDERER_JME) {
            cellRenderer = new SampleRenderer(this);
            return cellRenderer;
        }
        return super.createCellRenderer(rendererType);
    }

    public String getShapeType() {
        return shapeType;
    }

    @Override
    protected void setStatus(CellStatus status, boolean increasing) {
        super.setStatus(status, increasing);
        switch (status) {
            case ACTIVE:
                if (increasing) {
//                    menuComponent.setShowStandardMenuItems(false);
                    menuComponent.addContextMenuFactory(
                            new SampleContextMenuFactory());
                }
                break;
            case DISK:
                // TODO cleanup
                break;
        }

    }

    /**
     * Context menu factory for the Sample menu item
     */
    class SampleContextMenuFactory implements ContextMenuFactorySPI {

        public ContextMenuItem[] getContextMenuItems(ContextEvent event) {
            return new ContextMenuItem[]{new SimpleContextMenuItem(
                        BUNDLE.getString("Sample"), null,
                        new SampleContextMenuListener())
                    };
        }
    }

    /**
     * Listener for event when the Sample context menu item is selected
     */
    class SampleContextMenuListener implements ContextMenuActionListener {

        public void actionPerformed(ContextMenuItemEvent event) {
            logger.warning("Sample context menu action performed!");
        }
    }
}
