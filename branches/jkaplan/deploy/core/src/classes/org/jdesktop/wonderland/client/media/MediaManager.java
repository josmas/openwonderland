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

package org.jdesktop.wonderland.client.media;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import org.jdesktop.wonderland.client.media.cell.CellFactory;

/**
 * The media manager manages the entire set of cell types in the system. XXX
 * TBD
 * 
 * @author Jordan Slott <jslott@dev.java.net>
 */
public class MediaManager {

    /* A map of cell factories and the extensions the support */
    private Map<String, Set<CellFactory>> cellFactoryMap;
    
    /** Default constructor */
    public MediaManager() {
        cellFactoryMap = new HashMap();
    }
    
    /**
     * Singleton to hold instance of MediaManager. This holder class is loaded
     * on the first execution of MediaManager.getMediaManager().
     */
    private static class MediaManagerHolder {
        private final static MediaManager MediaManager = new MediaManager();
    }
    
    /**
     * Returns a single instance of this class
     * <p>
     * @return Single instance of this class.
     */
    public static final MediaManager getMediaManager() {
        return MediaManagerHolder.MediaManager;
    }
    
    /**
     * Registers a CellFactory given an array of media file name extensions.
     * This cell factory is used to generate cell setup classes, GUI panels
     * to configuration the cell setup information, and information so that
     * the cell type can be used in a world assembly palette of cell types.
     * 
     * @param extensions An array of file extensions (e.g. "jpg", "dae")
     * @param factory The cell factory
     */
    public void registerCellFactory(String[] extensions, CellFactory factory) {
        // For now, don't check if the factory already exists. We may need to
        // create an entry in cellFactoryMap for the extension type if it does
        // not yet exist.
        for (String extension : extensions) {
            Set<CellFactory> factories = cellFactoryMap.get(extension);
            if (factories == null) {
                factories = new HashSet<CellFactory>();
                cellFactoryMap.put(extension, factories);
            }
            factories.add(factory);
        }
    }
    
    /**
     * Returns a set of all cell factories. If no factories are registered,
     * returns an empty collection.
     * 
     * @return A set of registered cell factories
     */
    public Set<CellFactory> getAllCellFactories() {
        Set<CellFactory> factories = new HashSet();
        
        // Puts factories for all extensions into a single hash set. The set
        // will take care of making sure no duplicates exist.
        Iterator<Map.Entry<String, Set<CellFactory>>> it = cellFactoryMap.entrySet().iterator();
        while (it.hasNext() == true) {
            Map.Entry<String, Set<CellFactory>> entry = it.next();
            factories.addAll(entry.getValue());
        }
        return factories;
    }
    
    /**
     * Returns a set of cell factories given the extension type. If no factories
     * are present for the given extension, returns null.
     * 
     * @param extension File type extension (e.g. 'jpg', 'dae')
     * @return A set of CellFactory objects registered on the extension
     */
    public Set<CellFactory> getCellFactoriesByExtension(String extension) {
        return cellFactoryMap.get(extension);
    }
}
