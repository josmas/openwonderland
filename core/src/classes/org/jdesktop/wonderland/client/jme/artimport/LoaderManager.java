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
package org.jdesktop.wonderland.client.jme.artimport;

import java.io.File;
import java.net.URL;
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
    private HashMap<String, ModelLoaderFactory> classnameToLoader = new HashMap();
    private static LoaderManager loaderManager;
    
    private LoaderManager() {
    }
    
    public static LoaderManager getLoaderManager() {
        if (loaderManager==null)
            loaderManager = new LoaderManager();
        return loaderManager;
    }
    
    /**
     * Register the supplied loader with the system.  Loaders must be
     * separately activated to start working.
     * @param loader the loader to register
     */
    public void registerLoader(ModelLoaderFactory loader) {
        loaders.add(loader);
        classnameToLoader.put(loader.getLoaderClassname(), loader);
    }

    /**
     * Unregister a loader from the system.  If the loader is active, it
     * will also be deactivated.
     * @param loader the loader to unregister
     */
    public void unregisterLoader(ModelLoaderFactory loader) {
        loaders.remove(loader);
        classnameToLoader.remove(loader.getLoaderClassname());

        // if the loader is enabled, remove it from the active set.
        if (loader.isEnabled()) {
            activeLoaders.remove(loader.getFileExtension());
        }
    }

    /**
     * Activate a particular loader, and deactivate any loader that was
     * previously registered for the same file type
     * @param loader the loader to activate
     */
    public void activateLoader(ModelLoaderFactory loader) {
        // make sure the loader exists
        if (!loaders.contains(loader)) {
            throw new IllegalStateException("Activating non-existant loader " +
                                            loader);
        }

        loader.setEnabled(true);
        activeLoaders.put(loader.getFileExtension(), loader);
    }

    /**
     * Deactivate a particular loader.
     * @param loader the loader to deactivate
     */
    public void deactivateLoader(ModelLoaderFactory loader) {
        loader.setEnabled(false);

        ModelLoaderFactory current = activeLoaders.get(loader.getFileExtension());
        if (current != null && current.equals(loader)) {
            activeLoaders.remove(loader.getFileExtension());
        }
    }
    
    public ModelLoader getLoader(URL url) {
        ModelLoaderFactory loaderFactory = activeLoaders.get(org.jdesktop.wonderland.common.FileUtils.getFileExtension(url.toExternalForm()).toLowerCase());
        
        return loaderFactory.getLoader();
    }

    public ModelLoader getLoader(DeployedModel model) {
        return classnameToLoader.get(model.getModelLoaderClassname()).getLoader();
    }

    public ModelLoader getLoader(String fileextension) {
        ModelLoaderFactory loaderFactory = activeLoaders.get(fileextension);
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
