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
package org.jdesktop.wonderland.client.cell.properties;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import org.jdesktop.wonderland.client.cell.properties.annotation.CellProperties;
import org.jdesktop.wonderland.client.cell.properties.spi.CellPropertiesSPI;
import org.jdesktop.wonderland.client.login.LoginManager;
import org.jdesktop.wonderland.client.login.PrimaryServerListener;
import org.jdesktop.wonderland.client.login.ServerSessionManager;
import org.jdesktop.wonderland.common.InternalAPI;
import org.jdesktop.wonderland.common.utils.ScannedClassLoader;

/**
 * Manages the set of propery panels configuring cells. Cells implement the
 * CellPropertiesSPI interface and register their class with the Java service
 * loader mechanism. This class lists all of these cell properties.
 * 
 * @author Jordan Slott <jslott@dev.java.net>
 */
@InternalAPI
public class CellPropertiesManager implements PrimaryServerListener {

    /* A set of all cell property objects */
    private Set<CellPropertiesSPI> cellPropertiesSet;
    
    /* A map of cell state classes and their cell properties objects */
    private Map<Class, CellPropertiesSPI> cellPropertiesClassMap;

    /* A set of all cell properties associated with the current session */
    private final Set<CellPropertiesSPI> sessionProperties =
            new HashSet<CellPropertiesSPI>();

    /** Default constructor */
    public CellPropertiesManager() {
        cellPropertiesClassMap = new HashMap();
        cellPropertiesSet = new HashSet();

        LoginManager.addPrimaryServerListener(this);
    }
    
    /**
     * Singleton to hold instance of CellRegistry. This holder class is loaded
     * on the first execution of CellRegistry.getMediaManager().
     */
    private static class CellPropertiesHolder {
        private final static CellPropertiesManager cellProperties = new CellPropertiesManager();
    }
    
    /**
     * Returns a single instance of this class
     * <p>
     * @return Single instance of this class.
     */
    public static final CellPropertiesManager getCellPropertiesManager() {
        return CellPropertiesHolder.cellProperties;
    }
    
    /**
     * Registers a CellPropertiesSPI class. This interface is used to generate
     * a GUI to allow editing of a cell's properties on the client-side.
     * 
     * @param properties The CellPropertiesSPI class to register
     */
    public synchronized void registerCellProperties(CellPropertiesSPI properties) {
        // First check to see if the fully-qualified client-side cell class
        // name already exists and print a warning message if so (but we'll
        // still add it later)
        Class clazz = properties.getServerCellStateClass();
        if (cellPropertiesClassMap.containsKey(clazz) == true) {
            Logger logger = Logger.getLogger(CellPropertiesManager.class.getName());
            logger.warning("A CellPropertiesSPI already exist for class " +
                    clazz.getName());
        }

        // Add to the set of all CellPropertiesSPI objects and the map relating
        // the client-side cell class name to the object
        cellPropertiesSet.add(properties);
        cellPropertiesClassMap.put(clazz, properties);
    }

    /**
     * Unregisters a CellPropertiesSPI class.
     *
     * @param properties The CellPropertiesSPI class to register
     */
    public synchronized void unregisterCellProperties(CellPropertiesSPI properties) {
        Class clazz = properties.getServerCellStateClass();

        // remove the SPI object.  Check to make sure the class maps to the same
        // properties object before removing the mapping
        cellPropertiesSet.remove(properties);

        CellPropertiesSPI cur = cellPropertiesClassMap.get(clazz);
        if (cur == properties) {
            cellPropertiesClassMap.remove(clazz);
        }
    }
    
    /**
     * Returns a set of all cell properies objects. If no properties are registered,
     * returns an empty set.
     * 
     * @return A set of registered cell property objects
     */
    public Set<CellPropertiesSPI> getAllCellProperties() {
        return new HashSet(cellPropertiesSet);
    }
    
    /**
     * Returns a cell properties object given the Class of the server-side cell
     * state that the properties supports. If no properties are present for the
     * given cell, returns null.
     * 
     * @param clazz The class of the server-side cell state object
     * @return A CellPropertiesSPI object registered for the cell class
     */
    public CellPropertiesSPI getCellPropertiesByClass(Class clazz) {
        return cellPropertiesClassMap.get(clazz);
    }

    /**
     * Notification that the primary server has changed
     * @param server the current primary server
     */
    public void primaryServer(ServerSessionManager server) {
        // get rid of the current properties
        unregisterProperties();

        // add back the properties from the current server
        if (server != null) {
            registerProperties(server);
        }
    }

    /**
     * Register all Properties associated with the given session manager.
     * @param sessionManager the manager to register
     */
    protected synchronized void registerProperties(ServerSessionManager manager) {
        /* Attempt to load the class names using the service providers */
        ScannedClassLoader cl = manager.getClassloader();

        Iterator<CellPropertiesSPI> it = cl.getAll(
                CellProperties.class, CellPropertiesSPI.class);
        while (it.hasNext() == true) {
            CellPropertiesSPI spi = it.next();
            registerCellProperties(spi);
            sessionProperties.add(spi);
        }
    }

    /**
     * Unregister all Properties associated with the current session
     */
     protected synchronized void unregisterProperties() {
         for (CellPropertiesSPI spi : sessionProperties) {
            unregisterCellProperties(spi);
         }
         sessionProperties.clear();
     }
}
