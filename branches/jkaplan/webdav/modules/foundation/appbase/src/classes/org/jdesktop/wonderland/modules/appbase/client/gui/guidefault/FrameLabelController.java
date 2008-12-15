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

import org.jdesktop.wonderland.modules.appbase.client.Window2DView;
import org.jdesktop.wonderland.common.ExperimentalAPI;

/**
 * A 2D label which displays in the window header the user name of the application controller.
 *
 * @author deronj
 */ 

@ExperimentalAPI
public class FrameLabelController extends FrameLabel {

    /** The position of the controller label depends on the position of the close button */
    protected FrameCloseButton closeButton;

    /** 
     * Create a new instance of FrameComponent.
     *
     * @param view The view the frame encloses.
     * @param gui The event handler.
     * @param closeButton The close button which is next to this component in the frame header.
     */
    public FrameLabelController (Window2DView view, Gui2D gui, FrameCloseButton closeButton) {
	super("FrameLabelController", view, gui);
	this.closeButton = closeButton;

	// TODO: test
	setText("deronj");
    }

    /**
     * {@inheritDoc}
     */
    public void cleanup () {
	closeButton = null;
    }

    /**
     * Recalculate the geometry layout.
     */
    protected void updateLayout () {
	if (closeButton == null) return;

	// Make sure the close button layout is up to date.
	closeButton.updateLayout();

	// The width of controller is a quarter of view width
	width = 0.25f * view.getWidth();
	height = LABEL_HEIGHT;

	// Location of controller depends on the location of the close button
	x = closeButton.getX() - width;
	y = 0;
    }
}
