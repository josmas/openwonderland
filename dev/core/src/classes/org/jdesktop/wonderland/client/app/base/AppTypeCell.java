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

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.logging.Logger;
import java.util.UUID;
import com.jme.bounding.BoundingVolume;
import javax.swing.JOptionPane;
import com.jme.math.Vector2f;
import org.jdesktop.wonderland.client.app.base.AppType;
import org.jdesktop.wonderland.common.app.base.AppLaunchMethods;
import org.jdesktop.wonderland.common.app.base.AppLaunchMethodsConventional;
import org.jdesktop.wonderland.common.app.base.AppTypeCellConfig;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.config.CellConfig;
import org.jdesktop.wonderland.client.cell.CellCache;
import org.jdesktop.wonderland.common.cell.CellTransform;
import org.jdesktop.wonderland.common.ExperimentalAPI;

/**
 * TEMPORARY: 0.3 Only: This provides for the plug-in loading of different types
 * of application types. Eventually this will be done as a feature of WFS or the World.
 *
 * @author deronj
 */

@ExperimentalAPI
public class AppTypeCell extends Cell {
    
    private static final Logger logger = Logger.getLogger(AppTypeCell.class.getName());

    /** A name-keyed map of all modular app types which have been loaded */
    private static HashMap<String,AppType> nameToAppType = new HashMap<String,AppType>();

    private String baseUrl;
    private String appTypeClassName;
    private String jar;

    private AppType appType;

    public AppTypeCell (CellID cellID, CellCache cellCache) {
        super(cellID, cellCache);
    }

    @Override
    public void configure (CellConfig configData) {
	System.err.println("*********************** Call AppTypeCell.config");

        AppTypeCellConfig config = (AppTypeCellConfig) configData;

	baseUrl = config.getBaseUrl();
        appTypeClassName = config.getAppTypeClientClassName();
	jar = config.getClientJar();

	logger.severe("Loaded new app type client cell");
	logger.severe("baseUrl = " + baseUrl);
	logger.severe("appTypeClassName = " + appTypeClassName);
	logger.severe("jar = " + jar);

	// Load the client-side classes
	URL jarUrl = null;
	Class appTypeClass = null;
        try {
	    jarUrl = new URL(baseUrl + File.separatorChar + jar);
	    logger.severe("jarUrl = " + jarUrl);
	
	    URLClassLoader classLoader = new URLClassLoader(new URL[] { jarUrl });
    	    appTypeClass = classLoader.loadClass(appTypeClassName);
	    logger.severe("appTypeClass = " + appTypeClass);
	} catch (MalformedURLException ex) {
	    logger.severe("Invalid URL: " + jarUrl);
	} catch (ClassNotFoundException ex) {
	    logger.severe("Class " + appTypeClassName + " not found in " + jarUrl);
	}

	// Get the app type UUID constructor and instantiate the app type
	try {
	    Constructor constructor = appTypeClass.getConstructor();
	    appType = (AppType) constructor.newInstance();
	    logger.severe("******* Created appType = " + appType);
	} catch (IllegalAccessException ex) {
	    logger.severe("Illegal access during creating " + appTypeClassName);
	    // TODO: what is the correct way to handle this error?
	    throw new RuntimeException("Illegal access during app type creation");
	} catch (InvocationTargetException ex) {
	    logger.severe("Invocation target exception during creating " + appTypeClassName);
	    // TODO: what is the correct way to handle this error?
	    throw new RuntimeException("Invocation target exception during app type creation");
	} catch (NoSuchMethodException ex) {
	    logger.severe("Cannot find default constructor for class " + appTypeClassName);
	} catch (InstantiationException ex) {
	    logger.severe("Cannot instantiate class " + appTypeClassName);
	    // TODO: what is the correct way to handle this error?
	    throw new RuntimeException("Cannot instantiate class " + appTypeClassName);
	}

	logger.severe("New app type class has been loaded: " + appTypeClassName);

	String appTypeName = appType.getName();
	if (appTypeName == null || appTypeName.length() <= 0) {
	    // TODO: what is the correct way to handle this error?
	    throw new RuntimeException("Invalid app type name");
	}
	logger.severe("appTypeName = " + appTypeName);

	addAppType(appTypeName, appType);
    }

    // TODO: 0.3 kludge: can't control cell loading order! (type/instance)
    private static Integer lockObject = new Integer(0);

    /** Add this named app type to the name-keyed map */
    private void addAppType (String name, AppType appType) {
	nameToAppType.put(name, appType);

	/*
	// TODO: 0.3 kludge: can't control cell loading order! (type/instance) 
	synchronized (lockObject) {
	    lockObject.notify();
	}
        */
    }

    /**
     * Find an app type by its name. Returns null if app type has not been loaded
     * into this client.
     */
    /* TODO: 0.3 kludge: can't control cell loading order! (type/instance) */
    public static AppType findAppType (String name) {
	return nameToAppType.get(name);
    }
    /**/
    /*
    public static AppType findAppType (String name) {
	AppType appType = nameToAppType.get(name);
	while (appType == null) {
	    synchronized (lockObject) {
		try { lockObject.wait(); } catch (InterruptedException ex) {}
	    }
	    appType = nameToAppType.get(name);
	}
	return appType;
    }
    */

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

    protected static void reportLaunchError (String message) {
	JOptionPane.showMessageDialog(null, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
}
