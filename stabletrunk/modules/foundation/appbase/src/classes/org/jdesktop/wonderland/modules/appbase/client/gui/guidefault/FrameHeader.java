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
package org.jdesktop.wonderland.modules.appbase.client.gui.guidefault;

import com.jme.renderer.ColorRGBA;
import java.util.LinkedList;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.wonderland.modules.appbase.client.ControlArb;
import org.jdesktop.wonderland.modules.appbase.client.Window2DFrame;
import org.jdesktop.wonderland.modules.appbase.client.Window2DView;
import org.jdesktop.wonderland.common.ExperimentalAPI;
import org.jdesktop.wonderland.common.cell.CellTransform;

/**
 * The frame header (top side) for FrameWorldDefault.
 *
 * @author deronj
 */ 

@ExperimentalAPI
public class FrameHeader extends FrameSide {

    /** The window title */
    protected FrameLabelTitle title;

    /** Who is currently controlling the app */
    protected FrameLabelController controller;

    /** The close button */
    protected FrameCloseButton closeButton;
    
    /**
     * Create a new instance of FrameHeader.
     *
     * @param view The view the frame encloses.
     * @param closeListeners The listeners to be notified when the header's close button is pressed.
     */
    public FrameHeader (Window2DView view, LinkedList<Window2DFrame.CloseListener> closeListeners) {
	super("FrameHeader", view, Side.TOP, new Gui2DSide(view));

	// TODO: bug: currently getting an instantiation exception on the image resource.
       	//closeButton = new FrameCloseButton(view, closeListeners);

	title = new FrameLabelTitle(view, gui);

	// The position of the controller label depends on the position of the close button
	controller = new FrameLabelController(view, gui, closeButton);
    }

    /**
     * {@inheritDoc}
     */
    public void cleanup () {
	super.cleanup();

	if (title != null) {
	    title.cleanup();
	    title = null;
	}

	if (controller != null) {
	    controller.cleanup();
	    controller = null;
	}
	
	if (closeButton != null) {
	    closeButton.cleanup();
	    closeButton = null;
	}
    }

    /**
     * Update the size and position of the header and its subcomponents.
     *
     * @throw InstantiationException if couldn't allocate resources for the visual representation.
     */
    public void update () throws InstantiationException {
	if (title != null) {
	    title.update();
	}
	if (controller != null) {
	    controller.update();
	}
	if (closeButton != null) {
	    closeButton.update();
	}
	super.update();
    }

    /**
     * Set the title displayed in the header.
     *
     * @param text The new title.
     */
    public void setTitle (String text) {
	if (title != null) {
	    title.setText(text);
	}
    }

    /**
     * The control state of the app has changed. Make the corresponding change in the frame.
     *
     * @param controlArb The app's control arb.
     */
    public void updateControl (ControlArb controlArb) {
	super.updateControl(controlArb);
	if (title != null) {
	    title.updateControl(controlArb);
	}
	if (controller != null) {
	    controller.updateControl(controlArb);
	}
	if (closeButton != null) {
	    closeButton.updateControl(controlArb);
	}
    }

    /**
     * {@inheritDoc}
     */
    public void setColor (ColorRGBA color) {
	super.setColor(color);
	if (title != null) {
	    title.setColor(color);
	}
	if (controller != null) {
	    controller.setColor(color);
	}
	if (closeButton != null) {
	    closeButton.setColor(color);
	}
    }

    /**
     * Attach this component's event listeners to the given entity.
     */
    protected void attachEventListeners (Entity entity) {
	if (title != null) {
	    title.attachEventListeners(entity);
	}
	if (controller != null) {
	    controller.attachEventListeners(entity);
	}
	if (closeButton != null) {
	    closeButton.attachEventListeners(entity);
	}
    }

    /**
     * Detach this component's event listeners from the given entity.
     */
    protected void detachEventListeners (Entity entity) {
	if (title != null) {
	    title.detachEventListeners(entity);
	}
	if (controller != null) {
	    controller.detachEventListeners(entity);
	}
	if (closeButton != null) {
	    closeButton.detachEventListeners(entity);
	}
    }
}

