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
package org.jdesktop.wonderland.client.jme.artimport;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Manage the various loaders available to the system
 * 
 * @author paulby
 */
public class LoaderManager {

    private ArrayList<ModelLoaderFactory> loaders = new ArrayList();
    private HashMap<String, ModelLoaderFactory> activeLoaders = new HashMap();
    private static LoaderManager loaderManager;
    
    private LoaderManager() {
    }
    
    public static LoaderManager getLoaderManager() {
        if (loaderManager==null)
            loaderManager = new LoaderManager();
        return loaderManager;
    }
    
    /**
     * Register the supplied loader with the system
     * @param loader
     */
    public void registerLoader(ModelLoaderFactory loader) {
        loaders.add(loader);
        
        if (activeLoaders.containsKey(loader.getFileExtension())) {
            loader.setEnabled(false);
        } else {
            activeLoaders.put(loader.getFileExtension(), loader);
        }
        
    }
    
    public ModelLoader getLoader(File file) {
        ModelLoaderFactory loaderFactory = activeLoaders.get(org.jdesktop.wonderland.common.FileUtils.getFileExtension(file.getName()));
        
        return loaderFactory.getLoader();
    }
    
    /**
     *  Return the set of file extensions that can be loaded
     * @return
     */
    public String[] getLoaderExtensions() {
        return activeLoaders.keySet().toArray(new String[activeLoaders.size()]);
    }
    

}
