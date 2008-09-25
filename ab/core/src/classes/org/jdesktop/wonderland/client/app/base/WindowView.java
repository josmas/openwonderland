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
package org.jdesktop.wonderland.client.app.base;

import org.jdesktop.wonderland.common.ExperimentalAPI;

/**
 * The generic view type for 2D and 3D windows.
 *
 * @author deronj
 */

@ExperimentalAPI
public class WindowView {

    /** The window being viewed */
    protected Window window;

    /** The name of the space in which the view is displayed */
    protected String spaceName;

    /** The GUI handler for this view */
    protected Gui gui;

    /** 
     * Create a new instance of WindowView.
     *
     * @param window The window this view displays.
     * @param spaceName The GUI space in which the view resides.
     */
    public WindowView (Window window, String spaceName) {
	this.window = window;
	this.spaceName = spaceName;
    }

    /** 
     * Release resources held.
     */
    public void cleanup () {
	if (gui != null) {
	    gui.cleanup();
	    gui = null;
	}
    }

    /**
     * The window this view displays.
     */
    public Window getWindow () { 
	return window; 
    }

    /**
     * The name of the space in which the view is displayed.
     */
    public String getSpaceName () {
	return spaceName;
    }
}
