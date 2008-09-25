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
package org.jdesktop.wonderland.client.app.base.gui.guinull;

import java.util.logging.Logger;
import org.jdesktop.wonderland.client.app.base.GuiFactory;
import org.jdesktop.wonderland.client.app.base.Window;
import org.jdesktop.wonderland.client.app.base.Window2D;
import org.jdesktop.wonderland.client.app.base.WindowFrame;
import org.jdesktop.wonderland.client.app.base.WindowView;
import org.jdesktop.wonderland.common.ExperimentalAPI;

/**
 * A GUI factory which returns GUI classes which don't render anything. Used by the SAS.
 *
 * @author deronj
 */

@ExperimentalAPI
public class Gui2DFactory implements GuiFactory {

   /** The logger for gui.guinull */
    static final Logger logger = Logger.getLogger("wl.app.base.gui.guinull");

    /** The singleton gui factory */
    private static GuiFactory guiFactory;

    /**
     * Returns the singleton GUI factory object.
     */
    public static GuiFactory getFactory () {
	if (guiFactory == null) {
	    guiFactory = new Gui2DFactory();
	}
	return guiFactory;
    }

    /**
     * {@inheritDoc}
     */
    public WindowView createView (Window window, String spaceName) {
	if (!(window instanceof Window2D)) {
	    throw new IllegalArgumentException("Window is not an instance of Window2D");
	}					    
	return new Window2DViewNull((Window2D)window);
    }

    /**
     * {@inheritDoc}
     */
    public WindowFrame createFrame (WindowView view) {
	return new Window2DFrameNull(view);
    }
}

