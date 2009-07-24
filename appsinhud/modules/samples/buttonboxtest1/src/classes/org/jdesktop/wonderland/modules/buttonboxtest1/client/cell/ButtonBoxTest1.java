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
package org.jdesktop.wonderland.modules.buttonboxtest1.client.cell;

import org.jdesktop.wonderland.client.cell.*;
import org.jdesktop.wonderland.client.cell.Cell.RendererType;
import org.jdesktop.wonderland.client.input.Event;
import org.jdesktop.wonderland.client.input.EventClassListener;
import org.jdesktop.wonderland.client.jme.input.MouseButtonEvent3D;
import org.jdesktop.wonderland.client.jme.input.MouseEvent3D;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.CellStatus;
import org.jdesktop.wonderland.modules.buttonboxtest1.client.jme.cellrenderer.ButtonBoxTestRenderer;
import org.jdesktop.wonderland.modules.testcells.client.cell.SimpleShapeCell;

/**
 * A test cell for the ButtonBox.
 */
public class ButtonBoxTest1 extends SimpleShapeCell {

    /** The renderer which will draw the button box. */
    private ButtonBoxTestRenderer cellRenderer;

    /** Create a new instance of ButtonBoxTest1. */
    public ButtonBoxTest1(CellID cellID, CellCache cellCache) {
        super(cellID, cellCache);
    }

    /**
     * Create a cell renderer which is appropriate for the client.
     */
    @Override
    protected CellRenderer createCellRenderer(RendererType rendererType) {
        switch (rendererType) {
            case RENDERER_2D:
                // Note: There is currently no 2D version of the button box
                return null;
            case RENDERER_JME:
                cellRenderer = new ButtonBoxTestRenderer(this);
                break;
        }

        return cellRenderer;
    }

    /**
     * This is called when the status of the cell changes.
     */
    @Override
    public void setStatus(CellStatus status, boolean increasing) {
        super.setStatus(status, increasing);

        switch (status) {

            // The cell is now visible
            case ACTIVE:
                if (increasing) {
                    // Make the button box mouse input sensitive when it becomes visible
                    cellRenderer.addEventListener(new MyMouseListener());
                }
                break;

            // The cell is no longer visible
            case DISK:
                if (!increasing) {
                    // The button box no longer needs to be input sensitive because it is no longer visible
                    cellRenderer.removeEventListener();
                }
                break;
        }
    }

    /**
     * A mouse event listener. This receives mouse input events from the button box.
     */
    private class MyMouseListener extends EventClassListener {

        /**
         * This returns the classes of the Wonderland input events we are interested in receiving.
         */
        public Class[] eventClassesToConsume() {
            // Only respond to mouse button events
            return new Class[]{MouseButtonEvent3D.class};
        }

        /**
         * This will be called when a mouse event occurs over one of the components of the button box.
         */
        public void computeEvent(Event event) {

            // Only respond to mouse button click events
            MouseButtonEvent3D buttonEvent = (MouseButtonEvent3D) event;
            if (buttonEvent.isClicked() &&
                    buttonEvent.getButton() == MouseButtonEvent3D.ButtonId.BUTTON1) {

                // For now, just print name of the clicked node
                System.out.println("Left mouse button click on " +
                        ((MouseEvent3D) event).getNode().getName());
            }
        }
    }
}
