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
package org.jdesktop.wonderland.client.cell.registry;

import org.jdesktop.wonderland.client.cell.registry.spi.CellFactorySPI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import org.jdesktop.wonderland.client.cell.registry.annotation.CellFactory;
import org.jdesktop.wonderland.client.login.LoginManager;
import org.jdesktop.wonderland.client.login.PrimaryServerListener;
import org.jdesktop.wonderland.client.login.ServerSessionManager;
import org.jdesktop.wonderland.common.utils.ScannedClassLoader;

/**
 * The cell registry manages the collection of cell types registered with the
 * system. This is used to display them in the palette and also provides the
 * necessary information to create them in-world.
 * 
 * @author Jordan Slott <jslott@dev.java.net>
 */
public class CellRegistry implements PrimaryServerListener {

    /* A set of all cell factories */
    private Set<CellFactorySPI> cellFactorySet = null;
    
    /* A map of cell factories and the extensions the support */
    private Map<String, Set<CellFactorySPI>> cellFactoryExtensionMap = null;

    /* The set of all cell factories associated with the cuurent session */
    private final Set<CellFactorySPI> sessionFactories =
            new LinkedHashSet<CellFactorySPI>();

    /** Default constructor */
    public CellRegistry() {
        cellFactoryExtensionMap = new HashMap();
        cellFactorySet = new HashSet();

        LoginManager.addPrimaryServerListener(this);
    }
    
    /**
     * Singleton to hold instance of CellRegistry. This holder class is loaded
     * on the first execution of CellRegistry.getMediaManager().
     */
    private static class CellRegistryHolder {
        private final static CellRegistry cellRegistry = new CellRegistry();
    }
    
    /**
     * Returns a single instance of this class
     * <p>
     * @return Single instance of this class.
     */
    public static final CellRegistry getCellRegistry() {
        return CellRegistryHolder.cellRegistry;
    }
    
    /**
     * Registers a CellFactory. This cell factory is used to generate cell setup
     * classes, GUI panels to configuration the cell setup information, and
     * information so that the cell type can be used in a world assembly palette
     * of cell types.
     * 
     * @param factory The cell factory
     */
    public synchronized void registerCellFactory(CellFactorySPI factory) {
        // For now, don't check if the factory already exists. We may need to
        // create an entry in cellFactoryExtensionMap for the extension type if it does
        // not yet exist.
        String[] extensions = factory.getExtensions();
        if (extensions != null) {
            for (String extension : extensions) {
                Set<CellFactorySPI> factories = cellFactoryExtensionMap.get(extension);
                if (factories == null) {
                    factories = new HashSet<CellFactorySPI>();
                    cellFactoryExtensionMap.put(extension, factories);
                }
                factories.add(factory);
            }
        }
        
        // Add to the set containing all cell factories
        cellFactorySet.add(factory);
    }

    /**
     * Removes a CellFactory registration.
     * @param factory The cell factory
     */
    public synchronized void unregisterCellFactory(CellFactorySPI factory) {
        // For now, don't check if the factory already exists. We may need to
        // create an entry in cellFactoryExtensionMap for the extension type if it does
        // not yet exist.
        String[] extensions = factory.getExtensions();
        if (extensions != null) {
            for (String extension : extensions) {
                Set<CellFactorySPI> factories = cellFactoryExtensionMap.get(extension);
                if (factories != null) {
                    factories.remove(factory);

                    if (factories.isEmpty()) {
                        cellFactoryExtensionMap.remove(factories);
                    }
                }
            }
        }

        // Add to the set containing all cell factories
        cellFactorySet.remove(factory);
    }
    
    /**
     * Returns a set of all cell factories. If no factories are registered,
     * returns an empty set.
     * 
     * @return A set of registered cell factories
     */
    public Set<CellFactorySPI> getAllCellFactories() {
        return new HashSet(cellFactorySet);
    }
    
    /**
     * Returns a set of cell factories given the extension type. If no factories
     * are present for the given extension, returns null.
     * 
     * @param extension File type extension (e.g. 'jpg', 'dae')
     * @return A set of CellFactory objects registered on the extension
     */
    public Set<CellFactorySPI> getCellFactoriesByExtension(String extension) {
        return cellFactoryExtensionMap.get(extension);
    }

    /**
     * Notification that the primary server has changed.  Update our maps
     * accordingly.
     * @param server the new primary server (may be null)
     */
    public void primaryServer(ServerSessionManager server) {
        // remove any existing entries
        unregisterFactories();

        // find new entries
        if (server != null) {
            registerFactories(server);
        }
    }

    /**
     * Register all factories associated with the given session
     * manager.
     */
    protected synchronized void registerFactories(ServerSessionManager manager) {
        // search annotations
        ScannedClassLoader cl = manager.getClassloader();
        Iterator<CellFactorySPI> it = cl.getAll(CellFactory.class,
                                                CellFactorySPI.class);
        while (it.hasNext()) {
            CellFactorySPI factory = it.next();
            registerCellFactory(factory);
            sessionFactories.add(factory);
        }
    }

    /**
     * Unregister all factories associated with the current session
     */
    protected synchronized void unregisterFactories() {
        for (CellFactorySPI factory : sessionFactories) {
            unregisterCellFactory(factory);
        }

        sessionFactories.clear();
    }
}
