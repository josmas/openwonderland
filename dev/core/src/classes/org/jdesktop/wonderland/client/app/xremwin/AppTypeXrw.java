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
package org.jdesktop.wonderland.modules.app.xremwin.client;

import java.util.UUID;
import com.jme.math.Vector2f;
import org.jdesktop.wonderland.client.app.base.AppTypeConventional;
import org.jdesktop.wonderland.client.app.base.ProcessReporter;
import org.jdesktop.wonderland.client.app.base.ProcessReporterFactory;
import org.jdesktop.wonderland.common.app.base.AppLaunchMethods;
import org.jdesktop.wonderland.common.app.xremwin.AppLaunchMethodsXrw;
import org.jdesktop.wonderland.common.app.xremwin.AppTypeNameXrw;
import org.jdesktop.wonderland.client.apps.utils.net.NetworkAddress;


//TODO: import org.jdesktop.wonderland.client.app.base.gui.guidefault.Gui2DFactoryConventional;
import org.jdesktop.wonderland.client.app.base.gui.guinull.Gui2DFactoryConventional;

/**
 * The AppType for X11 applications that use the Xremwin protocol.
 *
 * @author deronj
 */

@ExperimentalAPI
public class AppTypeXrw extends AppTypeConventional {

    /**
     * {@inheritDoc}
     */
    public String getName () {
	return AppTypeNameXrw.XREMWIN_APP_TYPE_NAME;
    }

    /**
     * {@inheritDoc}
     */
    public AppLaunchMethods getLaunchMethods () {
	return new AppLaunchMethodsXrw();
    }

    /** 
     * Executes the master program specified by the command argument with a default process reporter.
     *
     * @param appType The app type object of this app type.
     * @param appName The name of this app instance.
     * @param command The command string to execute.
     * @param The size of app window pixels (in World units).
     * @return An object which contains the following fields:
     * app: The object for the launched object.
     * appId: A unique ID for the app.
     */
    public ExecuteMasterProgramReturn executeMasterProgram (String appName, String command, Vector2f pixelScale) {
	executeMasterProgram(appName, command, pixelScale, null);
    }

    /** 
     * Executes the master program specified by the command argument.
     *
     * @param appType The app type object of this app type.
     * @param appName The name of this app instance.
     * @param command The command string to execute.
     * @param The size of app window pixels (in World units).
     * @param reporter The reporter with which to report to the user. If null, a default reporter is used.
     * @return An object which contains the following fields:
     * app: The object for the launched object.
     * appId: A unique ID for the app.
     */
    public ExecuteMasterProgramReturn executeMasterProgram (String appName, String command, Vector2f pixelScale,
							    ProcessReporter reporter) {

	String masterHost = NetworkAddress.getDefaultHostAddress();
	UUID appId = UUID.randomUUID();

	AppXrwMaster app = null;
        try {
	    if (reporter == null) {
		reporter = ProcessReporterFactory.getFactory().create(appName);
	    }
	    AppXrw.logger.severe("reporter = " + reporter);
	    app = new AppXrwMaster(this, appName, masterHost, command, pixelScale, reporter);
	    AppXrw.logger.severe("app = " + app);
	} catch (InstantiationException ex) {
	    return null;
	}
	app.appId = appId;

	ExecuteMasterProgramReturn empr = new ExecuteMasterProgramReturn();
	empr.app = app;
	empr.appId = appId;
	empr.connectionInfo = app.getConnectionInfo();
	AppXrw.logger.severe("empr = " + empr);

	return empr;
    }


    /**
     * {@inheritDoc}
     */
    public GuiFactory getGuiFactory () {
	return Gui2DFactoryConventional.getFactory();
    }
}