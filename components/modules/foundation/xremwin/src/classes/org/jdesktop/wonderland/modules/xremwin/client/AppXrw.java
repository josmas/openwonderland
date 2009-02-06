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
package org.jdesktop.wonderland.modules.xremwin.client;

import com.jme.math.Vector2f;
import java.util.HashMap;
import java.util.logging.Logger;
import org.jdesktop.wonderland.common.ExperimentalAPI;
import org.jdesktop.wonderland.modules.appbase.client.AppCell;
import org.jdesktop.wonderland.modules.appbase.client.AppConventional;
import org.jdesktop.wonderland.modules.appbase.client.AppConventionalCell;
import org.jdesktop.wonderland.modules.appbase.client.AppTypeConventional;
import org.jdesktop.wonderland.modules.appbase.client.ControlArb;

/**
 * An X11 app which receives its window contents from the Xremwin server.
 * This is the superclass for both AppXrwMaster and AppXrwSlave.
 *
 * @author deronj
 */
@ExperimentalAPI
class AppXrw extends AppConventional {

    /** The logger for app.modules.xremwin */
    static final Logger logger = Logger.getLogger("wl.app.modules.xremwin");
    /** A mapping of wids to the corresponding windows */
    static final HashMap<Integer, WindowXrw> widToWindow = new HashMap<Integer, WindowXrw>();
    /** The Xremwin protocol interpreter -- Set it subclass constructor */
    protected ClientXrw client;

    /**
     * Create a instance of AppXRW with a generated ID.
     *
     * @param appType The type of 2D app to create.
     * @param appName The name of the app.
     * @param controlArb The control arbiter to use. null means that all users can control at the same time.
     * @param pixelScale The size of the window pixels.
     */
    public AppXrw(AppTypeConventional appType, String appName, ControlArb controlArb, Vector2f pixelScale) {
        super(appType, appName, controlArb, pixelScale);
        AppXrw.logger.severe("AppXrw: appType = " + appType);
    }

    /**
     * Create a new WindowXrw instance and its "World" view.
     *
     * @param app The application to which this window belongs.
     * @param x The X11 x coordinate of the top-left corner window.
     * @param y The X11 y coordinate of the top-left corner window.
     * @param borderWidth The X11 border width.
     * @param decorated Whether the window is decorated with a frame.
     * @param wid The X11 window ID.
     */
    public WindowXrw createWindow(int x, int y, int width, int height, int borderWidth, boolean decorated, int wid) {
        WindowXrw window = null;
        try {
            window = new WindowXrw(this, x, y, width, height, borderWidth, decorated,
                    ((AppConventionalCell) cell).getPixelScale(), wid);
            return window;
        } catch (InstantiationException ex) {
            return null;
        }
    }

    /**
     * Clean up resources.
     */
    public void cleanup() {
        super.cleanup();

        // Note: we may not always receive explicit DestroyWindow messages
        // for all windows. So destroy any that are left over.
        for (int wid : widToWindow.keySet()) {
            WindowXrw window = widToWindow.get(wid);
            if (window != null) {
                window.cleanup();
            }
        }

        if (client != null) {
            client.cleanup();
            client = null;
        }
    }

    /**
     * Returns the client.
     */
    ClientXrw getClient() {
        return client;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void setCell(AppCell cell)
            throws IllegalArgumentException, IllegalStateException {
        client.setCell((AppCellXrw) cell);
        super.setCell(cell);
    }

    /**
     * Get the window this window is a transient for.
     * Returns 0 if the window isn't a transient.
     *
     * @param wid The window whose transient window we want.
     * @return The window ID of the window which is transient
     * for the given window.
     */
    int getTransientForWid(int wid) {
        // TODO: implement
        return 0;
    }
}


