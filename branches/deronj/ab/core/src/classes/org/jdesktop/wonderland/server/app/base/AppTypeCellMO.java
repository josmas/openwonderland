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
package org.jdesktop.wonderland.server.app.base;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import com.jme.math.Vector3f;
import org.jdesktop.wonderland.common.app.base.AppTypeCellConfig;
import org.jdesktop.wonderland.server.cell.CellMO;
import org.jdesktop.wonderland.common.ExperimentalAPI;
import org.jdesktop.wonderland.server.cell.setup.BasicCellSetup;
import com.jme.bounding.BoundingVolume;
import org.jdesktop.wonderland.common.cell.CellTransform;
import org.jdesktop.wonderland.server.setup.BasicCellSetupHelper;
import com.sun.sgs.app.ClientSession;
import org.jdesktop.wonderland.common.cell.ClientCapabilities;
import org.jdesktop.wonderland.common.cell.config.CellConfig;

/**
 * TEMPORARY: 0.3 Only: This class is a way to get around the fact that WFS doesn't support 
 * non-spatial data. An AppType is a non-spatial property of a world.
 * A world can support various app types. In order to get this app type
 * loaded into the server and the client we wrap a stationary cell around
 * it and make it the size of the known universe around the MPK20 building, 
 * in order to guarantee that it it is always loaded.
 *
 * TODO: WFS: replace this with WFS non-spatial data.
 *
 * @author deronj
 */

@ExperimentalAPI
public class AppTypeCellMO extends CellMO {

    // A name-keyed map of all modular app types which have been loaded
    private static HashMap<String,AppTypeMO> nameToAppType = new HashMap<String,AppTypeMO>();

    private String appTypeName;
    private String appTypeServerClassName;
    private String appTypeClientClassName;
    private String serverJar;
    private String clientJar;

    // The effective base URL. 
    private String effBaseUrl;

    // The app type loaded
    private AppTypeMO appType;

    /** 
     * Default constructor, used when the cell is created via WFS.
     * Note: this cell is only ever created via WFS.
     */
    public AppTypeCellMO() {
	super();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected String getClientCellClassName(ClientSession clientSession, ClientCapabilities capabilities) {
        return "org.jdesktop.wonderland.client.app.base.AppTypeCell";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CellConfig getClientStateData(ClientSession clientSession, ClientCapabilities capabilities) {
	// Only client information is needed by client cells
	return new AppTypeCellConfig(effBaseUrl, null, appTypeClientClassName, null, clientJar);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setupCell (BasicCellSetup setupData) {
        super.setupCell(setupData);

	AppTypeCellSetup setup = (AppTypeCellSetup) setupData;
        appTypeServerClassName = setup.getAppTypeServerClassName();
        appTypeClientClassName = setup.getAppTypeClientClassName();
	serverJar = setup.getServerJar();
	clientJar = setup.getClientJar();

	// This is only for testing app modules using a local WFS world 
	String baseUrlDebug = setup.getBaseUrl();
	if (baseUrlDebug != null) {
	    effBaseUrl = baseUrlDebug;
	}

	logger.severe("Loaded new app type server cell");
	
       	logger.severe("appTypeServerClassName = " + appTypeServerClassName);
	logger.severe("appTypeClientClassName = " + appTypeClientClassName);
	logger.severe("effBaseUrl = " + effBaseUrl);
	logger.severe("serverJar = " + serverJar);
	logger.severe("clientJar = " + clientJar);
	
	// Load the server-side classes
	URL jarUrl = null;
	Class appTypeClass = null;
        try {
	    jarUrl = new URL(effBaseUrl + File.separatorChar + serverJar);
	    logger.severe("server jarUrl = " + jarUrl);
	
	    URLClassLoader classLoader = new URLClassLoader(new URL[] { jarUrl });
    	    appTypeClass = classLoader.loadClass(appTypeServerClassName);
	    logger.severe("appTypeClass = " + appTypeClass);
	} catch (MalformedURLException ex) {
	    logger.severe("Invalid URL: " + jarUrl);
	} catch (ClassNotFoundException ex) {
	    logger.severe("Class " + appTypeServerClassName + " not found in " + jarUrl);
	}
	logger.severe("New server app type class has been loaded: " + appTypeServerClassName);

	// Get the app type default constructor (to randomly but uniquely generate the app 
	// type ID) and then instantiate the app type
	try {
	    Constructor constructor = appTypeClass.getConstructor(null);
	    appType = (AppTypeMO) constructor.newInstance();
	} catch (IllegalAccessException ex) {
	    logger.severe("Illegal access during creating " + appTypeServerClassName);
	    return;
	} catch (InvocationTargetException ex) {
	    logger.severe("Invocation target exception during creating " + appTypeServerClassName);
	    return;
	} catch (NoSuchMethodException ex) {
	    logger.severe("Cannot find default constructor for class " + appTypeServerClassName);
	    return;
	} catch (InstantiationException ex) {
	    logger.severe("Cannot instantiate class " + appTypeServerClassName);
	    return;
	}

	logger.severe("Created app type class name " + appTypeServerClassName + " with app type name" + appType.getName());
        
	appTypeName = appType.getName();
	if (appTypeName == null || appTypeName.length() <= 0) {
	    // TODO: what is the correct way to handle this error?
	    throw new RuntimeException("Invalid app type name");
	}
	logger.severe("appTypeName = " + appTypeName);

	AppTypeMO atg = nameToAppType.get(appTypeName);
	if (atg != null) {
	    // TODO: what is the correct way to handle this error?
	    throw new RuntimeException("App type " + appTypeName + " is already defined");
	}

	System.err.println("************ appTypeName = " + appTypeName);
	System.err.println("************ appType = " + appType);
	nameToAppType.put(appTypeName, appType);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void reconfigureCell(BasicCellSetup setup) {
        super.reconfigureCell(setup);
        setupCell(setup);
    }

    /**
     * {@inheritDoc}
     */
    public BasicCellSetup getCellMOSetup() {

        /* Create a new BasicCellMOSetup and populate its members */
        AppTypeCellSetup setup = new AppTypeCellSetup();
	setup.setAppTypeServerClassName(appTypeServerClassName);
	setup.setAppTypeClientClassName(appTypeClientClassName);
	setup.setServerJar(serverJar);
	setup.setClientJar(clientJar);
	setup.setBaseUrl(effBaseUrl);

        /* Set the bounds of the cell */
        BoundingVolume bounds = this.getLocalBounds();
        if (bounds != null) {
            setup.setBounds(BasicCellSetupHelper.getSetupBounds(bounds));
        }

        /* Set the origin, scale, and rotation of the cell */
        CellTransform transform = this.getLocalTransform(null);
        if (transform != null) {
            setup.setOrigin(BasicCellSetupHelper.getSetupOrigin(transform));
            setup.setRotation(BasicCellSetupHelper.getSetupRotation(transform));
            setup.setScaling(BasicCellSetupHelper.getSetupScaling(transform));
        }
        return setup;
    }

    public static AppTypeMO findAppType (String appTypeName) {
	return nameToAppType.get(appTypeName);
    }
}
