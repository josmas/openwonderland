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
 * A 2D label which displays the window title.
 *
 * @author deronj
 */ 

@ExperimentalAPI
public class FrameLabelTitle extends FrameLabel {

    /** 
     * Create a new instance of FrameComponent.
     *
     * @param view The view the frame encloses.
     * @param gui The event handler.
     */
    public FrameLabelTitle (Window2DView view, /*TODO: Gui2D*/ Object gui) {
	super("FrameLabelTitle", view, gui);
    }

    /**
     * Calculate the geometry layout.
     */
    protected void updateLayout () {

	// The width of title is half of view width
	width = view.getWidth() / 2f;
	height = LABEL_HEIGHT;

	// TODO
	//width = 50f;
	//height = 50f;

	//	x = FrameWorldDefault.SIDE_THICKNESS;
	x = -(view.getWidth() - width) / 2f;
	y = 0f;

    }
}
