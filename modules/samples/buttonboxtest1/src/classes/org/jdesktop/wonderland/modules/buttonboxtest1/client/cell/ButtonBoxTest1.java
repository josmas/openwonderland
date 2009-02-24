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
    public boolean setStatus(CellStatus status) {
        boolean ret = super.setStatus(status);

        switch (status) {

	    // The cell is now visible
            case ACTIVE:
		// Make the button box mouse input sensitive when it becomes visible
                cellRenderer.addEventListeners(new MyMouseListener(), 
					       new MyMouseListener());
                break;

	    // The cell is no longer visible
            case DISK:
		// The button box no longer needs to be input sensitive because it is no longer visible
                cellRenderer.removeEventListeners();
                break;
        }

        return ret;
    }

    /**
     * A mouse event listener. This receives mouse input events from the button box.
     */
    private class MyMouseListener extends EventClassListener {

	/**
	 * This returns the classes of the Wonderland input events we are interested in receiving.
	 */
	public Class[] eventClassesToConsume () {
	    // Only respond to mouse button events
	    return new Class[] { MouseButtonEvent3D.class };
	}

	/**
	 * This specifies whether listeners attached to parent entities of the "event hit entity"
	 * should be also notified of the event. The event hit entity is the entity which owns
	 * the geometry which is under the mouse cursor when the input event occurs.
	 */
	public boolean propagatesToParent (Event event) {

	    // Initially, don't let the base see events which land on the buttons
	    return false;

	    // EXPERIMENT: Let base see events which land on the buttons
	    // return true;
	}

	/**
	 * This will be called when a mouse event occurs over one of the components of the button box.
	 */
	public void computeEvent (Event event) {

	    // Only respond to mouse button click events
	    MouseButtonEvent3D buttonEvent = (MouseButtonEvent3D) event;
	    if (buttonEvent.isClicked() && 
		buttonEvent.getButton() == MouseButtonEvent3D.ButtonId.BUTTON1) {
		    
		// For now, just print name of the clicked entity
		System.out.println("Left mouse button click on " + 
				   ((MouseEvent3D)event).getNode().getName());
	    }
	}
    }
}
