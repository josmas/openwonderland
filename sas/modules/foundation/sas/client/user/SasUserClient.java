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
package org.jdesktop.wonderland.client.app.sas.user;

/**
 * The base class for a SAS user client. The Wonderland user client must register this with 
 * via ClientContext.registerSasUserClient before it can be used. The primary user client is
 * the one that owns the GUI, that is, invoking an app launch via the GUI causes a SAS app launch
 * for the Wonderland session of the primary SasUserClient. Subclasses should implement installToGui
 * and uninstallFromGui to merge/unmerge the supported SAS apps into the Wonderland user client's GUI.
 * Note that a global lock is held during the invocations to installToGui and uninstallFromGui.
 *
 * @author deronj
 */

@ExperimentalAPI
public abstract class SasUserClient {

    private static Object guiLock = new Integer(0);

    private static UserClient primaryUserClient;
    
    private boolean isPrimary;
    
    private WonderlandSession session;

    private UserServerConnection serverConnection;

    /**
     * The supported apps for various execution capabilities. This is the union of all apps supported by
     * all of the SAS providers of this execution capability connected to the same Wonderland server as
     * this SAS user client.
     */
    private HashMap<String,AppCollection>() supportedApps = HashMap<String,AppCollection>()();

    public SasUserClient (WonderlandSession session) {
	this.session = session;
	serverConnection = new UserServerConnection(this);
	session.connect(serverConnection);
    }

    // TODO: later: where to call this from? */
    public void cleanup () {
	session.disconnect(serverConnection);
    }

    
    public static void setPrimary () {
	if (primaryUserClient != null) {
	    primaryUserClient.deactivate();
	}
	primaryUserClient = this;
	activate();
    }

    public static boolean isPrimary () {
	return isPrimary;
    }

    private void activate () {
	isPrimary = true;
	synchronized (guiLock) {
	    installToGui(supportedApps);
	}
    }

    private void deactivate () {
	isPrimary = false;
	synchronized (guiLock) {
	    uninstallGui(supportedApps);
	}
    }

    public BigInteger getID() {
	return session.getID();
    }

    boolean addExecutionCapability (String executionCapability, AppCollection apps) {

	// Add these new apps into the list of apps supported.
	AppCollection newApps = supportedApps.get(executionCapability);
	if (newApps == null) {
	    newApps = apps;
	} else {
	    newApps.union(apps);
	}
	supportedApps.put(executionCapability, newApps);

	// Reflect changes in the GUI
	reinstallGui();
    }

    boolean removeExecutionCapability (String executionCapability, AppCollection apps) {

	// Add these new apps into the list of apps supported.
	AppCollection newApps = supportedApps.get(executionCapability);
	if (newApps == null) {
	    newApps = apps;
	} else {
	    newApps.union(apps);
	}
	supportedApps.put(executionCapability, newApps);

	// Reflect changes in the GUI
	reinstallGui();
    }


    private void reinstallGui () {
	synchronized (guiLock) {
	    uninstallGui(supportedApps);
	    installGui(supportedApps);
	}
    }

    /** 
     * Install menu items for this SAS UserClient into the GUI of the Wonderland user client.
     * @param appMap A Hashmap from an execution capability to the app collection for it that is supported.
     */
    protected abstract void installToGui (HashMap<String,AppCollection> appMap);

    /**
     * Install menu items for this SAS UserClient into the GUI of the Wonderland user client.
     * @param appMap A Hashmap from an execution capability to the app collection for it that is no longer supported.
     */
    protected abstract void uninstallFromGui (HashMap<String,AppCollection> apps);

    
    /**
     * Launch the given app. 
     *
     * NOTE: this should not be called from the AWT Event Dispatch thread.
     */
    protected LaunchStatus guiLaunch (String executionCapability, String appName, String launchInfo, 
				      StringBuffer launchStatusDetail) {

	serverConnection.launch(this, executionCapability, appName, launchInfo, launchStatusDetail);
    }

    
}
