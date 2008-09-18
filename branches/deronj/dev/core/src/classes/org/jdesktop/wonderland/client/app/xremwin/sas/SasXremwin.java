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
package org.jdesktop.wonderland.client.app.xremwin.sas;

/**
 * An SAS provider client that supports Xremwin an xremwin execution capability.
 *
 * @author deronj
 */

@ExperimentalAPI
public class SasXremwin extends ProviderClient implements ProviderServerConnection.LaunchListener {

    private static Logger logger = Logger.getLogger(SasXremwin.class.getName());

    private static appType = new AppTypeXrw();

    private class AppSpatialInfo {
	BoundingVolume bounds;
	CellTransform transform;
	Vector2f pixelScale;
    }

    private SaxXremwin client;
    private int exitValue;
    private boolean stop;

    // Execution Capability
    private static final String CAPABILITY = "Xremwin";

    private ServerInfo serverInfo;
    private LoginParameters loginParams;
    private String configFileName;

    private AppCollection apps;

    // Keys are of the form "<appName>;<command"
    private HashMap<String,AppSpatialInfo> appSpatialInfos = HashMap<String,AppSpatialInfo>();

    private SasXremin (ServerInfo serverInfo, LoginParameters loginParams, String configFileName) {
	this.serverInfo = serverInfo;
	this.loginParams = loginParams;
	this.configFileName = configFileName;
    }
    
    private boolean initialize () {

	// Get server info 
	if (!initConnection(serverInfo, loginParams)) {
	    logger.severe("Cannot establish connection to server " + serverInfo);
	    return false;
	}

	// Get config file name
	if (!initApps(configFileName);
	    logger.severe("Cannot initialize apps from config file " + configFileName);
	    return false;
	}

	// Register launch listener
	serverConnection.registerLaunchListener(CAPABILITY, this);

	// Register apps with server
	if (!serverConnection.addExecutionCapability(CAPABILITY, apps)) {
	    logger.severe("Cannot register apps with server");
	    return false;
	}

	return true;
    }

    public static void main(String args[]) {

	// TODO: get serverInfo and configFileName from args
        String serverName = "localhost";
	int serverPort = 1139;
	String configFileName = null;

	// TODO: use Rel 0.4 SMC configuration, but make user name configurable in addition to password
	// Note: multiple copies of auser may be logged into the server simultaneously
	String userName = "sasxremwin";
	String password = "dummy";

	WonderlandServerInfo serverInfo = new ServerInfo(serverName, serverPort);
	LoginParameters loginParams = new LoginParameters(userName, password);

	client = new SasXremwin(serverInfo, loginParameters, configFileName);
	if (!client.initialize()) {
	    logger.severe("SasXremwin exit.");
	    System.exit(1);
	}

	sleepUntilExit();

	client.disconnect();
	System.exit(exitValue);
    }

    private SasXremwin getClient () {
	return client;
    }


    // Block the main thread until it is woken up by this.exit()
    private void sleepUntilExit () {
	while (!stop) {
	    synchronized (this) {
		try {
		    wait();
		} catch (InterruptedException ex) {}
	    }
	}
    }	    

    private void exit (int exitValue) {
	this.exitValue = exitValue;
	stop = true;
	synchronized (this) {
	    notify();
	}
    }

    private void initApps (String configFileName) {
	// TODO: later: read apps config file
	String name;
	String command;
	AppSpatialInfo spatialInfo;
	
	AppList appList = new AppList();

	name = "Center Firefox";
	command = "/usr/bin/firefox -P WonderlandFirefoxTeamRoomCenter";
	appList.add(name, command, );
	spatialInfo = new AppSpatialInfo();
	spatialInfo.bounds = new BoundingVolume(...);
	spatialInfo.transform = new Transform(..);
	spatialInfo.pixelScale = new Vector2f(0.00525f, 0.00525f);
	spatialInfos.put(name + ";" + command, spatialInfo);

	name = "Open Office (Wonderland Overview)";
	command = "soffice -env:UserInstallation=file:///tmp/instance0 ./data/Wonderland/test/appshare/OH07MPK20-revised.odp";
	appList.add(name, command, );
	spatialInfo = new AppSpatialInfo();
	spatialInfo.bounds = new BoundingVolume(...);
	spatialInfo.transform = new Transform(..);
	spatialInfo.pixelScale = new Vector2f(0.0070f, 0.0062f);
	spatialInfos.put(name + ";" + command, spatialInfo);
    }

    private LaunchStatus launch (SasLaunchMessage message) {
	String appName = message.getAppName();
	String unixCommand = message.getLaunchInfo();

	AppSpatialInfo spatialInfo = spatialInfos.get(appName + ";" + unixCommand);
	if (spatialInfo == null) {
	    return new LaunchStatus(LaunchStatus.LAUNCH_REFUSED, "Cannot find app spatial info");
	}

	// Create a reporter to report output and exit value to the launching user client
        ProviderUserReporter reporter = new ProviderUserReporter(appName, message.getUserClientID(), session);

	AppConventional.LaunchStatus status;
	status = AppConventional.userLaunchApp(appType, appName, unixCommand, false,
					       spatialInfo.getBounds(), spatialInfo.getTransform(),
					       spatialInfo.getPixelScale, reporter);

	// TODO: convert conventional status to sas launch status;
	return sasStatus;
    }
}

