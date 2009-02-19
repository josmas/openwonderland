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
package org.jdesktop.wonderland.modules.appbase.client;

import java.lang.reflect.Constructor;
import org.jdesktop.wonderland.modules.appbase.client.gui.GuiFactory;
import org.jdesktop.wonderland.common.annotation.Plugin;
import org.jdesktop.wonderland.client.ClientPlugin;
import org.jdesktop.wonderland.client.login.ServerSessionManager;
import org.jdesktop.wonderland.common.ExperimentalAPI;
import java.util.logging.Logger;

/**
 * An object which is created during the user client login process in order to initialize the 
 * app base for that user client.
 */
@ExperimentalAPI
@Plugin
public class AppClientPlugin implements ClientPlugin {

    /** The logger for app.base */
    static final Logger logger = Logger.getLogger("wl.app.base");

    /** The default 2D GUI factory to use. */
    private final String GUI_2D_FACTORY_CLASS_DEFAULT = 
        "org.jdesktop.wonderland.modules.appbase.client.cell.gui.guidefault.Gui2DFactory";

    /** All client plugins must have a no-arg constructor. */
    public AppClientPlugin () {}

    /**
     * This is executed at the start up of all user clients. 
     * <br><br>
     * Note: it is *NOT* executed by the SAS provider client because this client is set up to ignore all client plugins.
     */
    public void initialize(ServerSessionManager loginInfo) {
        System.err.println("*************** Initializing app base for user client");
        initAppBaseUserClient();
    }

    /**
     * Called to initialize the app base for a user client on client startup.
     */
    public void initAppBaseUserClient () {
        initGui2DFactory();
    }

    /**
     * Initialize the app base 2D gui factory for a user client.
     */
    public void initGui2DFactory() {

        // TODO: later on we might allow the default gui factory to be overridden by the user. 

        ClassLoader classLoader = getClass().getClassLoader();
        GuiFactory gui2DFactory = null;
        try {
            Class clazz = Class.forName(GUI_2D_FACTORY_CLASS_DEFAULT, true, classLoader);
            Constructor constructor = clazz.getConstructor();
            gui2DFactory = (GuiFactory) constructor.newInstance();
        } catch(Exception e) {
            logger.severe("Error instantiating app base 2D GUI factory "+ GUI_2D_FACTORY_CLASS_DEFAULT+
                        ", Exception = " + e);
        }

        if (gui2DFactory == null) {
            logger.severe("Error instantiating app base 2D GUI factory "+ GUI_2D_FACTORY_CLASS_DEFAULT);
        } else {
            App.setGui2DFactory(gui2DFactory);
        }
    }
}
