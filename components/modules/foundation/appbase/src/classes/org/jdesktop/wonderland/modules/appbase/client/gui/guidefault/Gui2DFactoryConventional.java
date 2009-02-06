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
package org.jdesktop.wonderland.modules.appbase.client.gui.guidefault;

import java.util.logging.Logger;
import org.jdesktop.wonderland.modules.appbase.client.GuiFactory;
import org.jdesktop.wonderland.modules.appbase.client.Window;
import org.jdesktop.wonderland.modules.appbase.client.Window2D;
import org.jdesktop.wonderland.modules.appbase.client.WindowFrame;
import org.jdesktop.wonderland.modules.appbase.client.WindowView;
import org.jdesktop.wonderland.common.ExperimentalAPI;
import org.jdesktop.wonderland.modules.appbase.client.AppCell;
import org.jdesktop.wonderland.modules.appbase.client.AppCellRenderer;

/**
 * The default 2D GUI factory (a singleton) used by conventional windows. This creates window views and frame objects
 * for 2D applications. The frames are implemented using a very rudimentary component system.
 *
 * @author deronj
 */

@ExperimentalAPI
public class Gui2DFactoryConventional implements GuiFactory {

   /** The logger for gui.guidefault */
    static final Logger logger = Logger.getLogger("wl.app.base.gui.guidefault");

    /** The singleton gui factory */
    private static GuiFactory guiFactory;

    /**
     * Returns the singleton GUI factory object.
     */
    public static GuiFactory getFactory () {
	if (guiFactory == null) {
	    guiFactory = new Gui2DFactoryConventional();
	}
	return guiFactory;
    }

    /**
     * {@inheritDoc}
     */
    public AppCellRenderer createCellRenderer (AppCell cell) {
	return new AppCellRendererJME(cell);
    }

    /**
     * {@inheritDoc}
     */
    public WindowView createView (Window window, String spaceName) {
	if        ("World".equals(spaceName)) {
	    return new ViewWorldConventionalDefault((Window2D)window);
	} else if ("HUD".equals(spaceName)) {
	    // TODO: app windows in HUD: not yet
	    //return new ViewHUD(window);
	    return null;
	}
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public WindowFrame createFrame (WindowView view) {
	if (view instanceof ViewWorldDefault) {
	    return new FrameWorldDefault(view);
	} else {
	    // TODO: app windows in HUD: not yet
	    // return new FrameHUD(view);
	    return null;
	}
    }

}

