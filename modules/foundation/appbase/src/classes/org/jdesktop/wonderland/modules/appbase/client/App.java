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

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.logging.Logger;
import com.jme.bounding.BoundingBox;
import com.jme.bounding.BoundingVolume;
import javax.swing.JOptionPane;
import org.jdesktop.wonderland.common.ExperimentalAPI;
import org.jdesktop.wonderland.client.comms.ConnectionFailureException;

/**
 * The generic application superclass. All apps in Wonderland have this
 * root class. 
 * <br><br>
 * Before an app can become visible in the world it must be associated with an AppCell
 * using <code>setCell</code>. <code>setCell</code> may be called only once for the app. Once called the app 
 * remains permanently associated with that cell until the app or cell is cleaned up.
 *
 * When you are done with this app you should call <code>cleanup</code> to clean up its resources.
 * (This is optional).
 *
 * @author deronj
 */ 

@ExperimentalAPI
public class App {

    /** The logger for app.base */
    static final Logger logger = Logger.getLogger("wl.app.base");

    /** The list of all windows created by this app */
    protected LinkedList<Window> windows = new LinkedList<Window>();

    /** The type of this app */
    protected AppType appType;

    /** The control arbiter for this app. null means that all users can control the app at the same time */
    protected ControlArb controlArb;

    /** The world cell to which the app belongs */
    protected AppCell cell;

    /**
     * Create a new instance of App.
     *
     * @param appType The type of app to create.
     * @param controlArb The control arbiter to use. null means that all users can control the app at the same time.
     */
    public App (AppType appType, ControlArb controlArb) {
	this.appType = appType;
	this.controlArb = controlArb;
	appType.appAdd(this);
    }
    
    /**
     * Clean up resources held. Specifically, the control arbiter is cleaned up.
     */
    public void cleanup () {
	if (appType != null) {
	    appType.appRemove(this);
	    appType = null;
	}

	if (controlArb != null) {
	    controlArb.cleanup();
	    controlArb = null;
	}

	cell = null;

	if (windows != null) {
	    for (Window window : windows) {
		window.cleanup();
	    }
	    windows.clear();
	    windows = null;
	}
    }

    /**
     * Returns the type of the app.
     */      
    public AppType getAppType () {
	return appType;
    }
    
    /**
     * Returns the Control Arbiter for this app.
     * If this is null the app supports fine-grained control swapping.
     * That is, the app accepts user events from different users equally
     * on a first-come first-served basis.
     */      
    public ControlArb getControlArb () {
	return controlArb;
    }

    /**
     * Returns the cell of this app.
     */      
    public AppCell getCell () {
	return cell;
    }

    /**
     * Returns an iterator over all the windows of this app.
     */
    public Iterator<Window> getWindowIterator() {
	return windows.iterator();
    }

    /**
     * Add a new window to this app's list of windows.
     *
     * @param window The window to add.
     */
    synchronized void windowAdd (Window window) {
	windows.add(window);
	
	/* TODO
	Just call window.initControl(controlArb) if controlArb not null?

	if (controlArb != null && controlArb.hasControl()) {
	    String controllingUser = controlArb.getControllingUser();
	    window.setControllingUser(controllingUser);
	    controlArb.addListener(window);
	}
	window.highlightControl();
	*/
    }

    /**
     * Remove a window from this app's list of windows.
     *
     * @param window The window to remove.
     */
    synchronized void windowRemove (Window window) {
	windows.remove(window);

	/* TODO
	window.setControllingUser(null);
 	if (controlArb != null) {
	    controlArb.removeListener(window);
        }

	// No need to highlight control because window should be invisible.
	*/
    }

    /**
     * Calculate and return the current bounds for this window. This is the 
     * union of the bounds of all of the app windows.
     */
    synchronized BoundingVolume getBounds () {
	BoundingVolume union = new BoundingBox();
	for (Window window : windows) {
	    union.merge(window.getBounds());
	}
	return union;
    }

    /**
     * Used to associate this app with the given cell.  May only be called one time.
     *
     * @param cell The world cell containing the app.
     * @throws IllegalArgumentException If the cell already is associated
     * with an app.
     * @throws IllegalStateException If the app is already associated 
     * with a cell.
     */
    public void setCell (AppCell cell) 
	throws IllegalArgumentException, IllegalStateException 
    {
	if (cell == null) {
	    throw new NullPointerException();
	}
	if (this.cell != null) {
	    throw new IllegalStateException("App already has a cell");
	}

	this.cell = cell;
    }

    /** 
     * Launch an instance of the given app type. This creates the server cell for the app and associated
     * client cells.
     * @param appTypeName The name of the app type.
     * @param appName The name of the application instance (Note: doesn't need to be unique).
     * @param command The command (with arguments) to execute on the master machine.
     * @param bestView If true the bounds and transform are configured to place the app in front of the 
     * launching user at a "good" distance.
     * @param bounds The bounding object of the app cell (only used if bestView is false).
     * @param transform The center of the the app cell in World coordinates (not used if bestView is true).
     * @param pixelScale The size of app window pixels (in world coordinates).
     */
    /* TODO
    public static void userLaunchLocalApp (String appTypeName, String appName, String command, boolean bestView,
					   BoundingVolume bounds, CellTransform transform, Vector2f pixelScale) {

	AppType appType = findAppType(appTypeName);
	if (appType == null) {
	    reportLaunchError("Cannot find app type to launch: " + appTypeName);
	    return;
	}

	AppLaunchMethods lm = appType.getLaunchMethods();
	if (lm == null) {
	    reportLaunchError("Cannot determine permitted launch modes for app type " + appTypeName);
	    return;
	}

	if (!lm.containsLauncher(AppLaunchMethods.Launcher.USER)) {
	    reportLaunchError("Instances of app type " + appTypeName + " cannot be launched by the user");
	    return;
	}

	switch (lm.getStyle()) {

	case WONDERLAND:
	    //TODO
	    break;

	case CONVENTIONAL:
	    AppLaunchMethodsConventional lmc = (AppLaunchMethodsConventional) lm;
	    AppTypeConventional act = (AppTypeConventional) appType;
	    if (lmc.containsExecutionSite(AppLaunchMethodsConventional.ExecutionSite.LOCAL)) {
		AppConventional.userLaunchApp(act, appName, command, bestView, bounds, transform, pixelScale);
	    } else {
		reportLaunchError("Instances of app type " + appTypeName + " cannot be launched on the local machine");
	    }
	    break;
	}
    }
    */

    protected static void reportLaunchError (String message) {
	JOptionPane.showMessageDialog(null, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Print debug information for all app windows.
     */
    public void printWindowInfosAll () {
	for (Window window : windows) {
	    window.printWindowInfo();
	}
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString () {
        StringBuffer buf = new StringBuffer();
	buf.append("appType=" + appType.getName());
	buf.append(",bounds=[" + getBounds() + "]");
        return buf.toString();
    }	
}
