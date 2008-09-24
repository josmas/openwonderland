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

import com.jme.scene.Node;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;

/**
 * Manage the various loaders available to the system
 * 
 * @author paulby
 */
public class LoaderManager {

    private ArrayList<ModelLoader> loaders = new ArrayList();
    private HashMap<String, ModelLoader> activeLoaders = new HashMap();
    private static LoaderManager loaderManager;
    
    private LoaderManager() {
        try {
            Class clazz = Class.forName("org.jdesktop.wonderland.modules.kmzloader.client.LoaderKmz");
            ModelLoader loader = (ModelLoader) clazz.newInstance();
            registerLoader(loader);
        } catch (InstantiationException ex) {
            Logger.getLogger(LoaderManager.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            Logger.getLogger(LoaderManager.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(LoaderManager.class.getName()).log(Level.SEVERE, null, ex);
        }
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
    public void registerLoader(ModelLoader loader) {
        loaders.add(loader);
        
        if (activeLoaders.containsKey(loader.getFileExtension())) {
            loader.setEnabled(false);
        } else {
            activeLoaders.put(loader.getFileExtension(), loader);
        }
        
    }
    
    public Node load(File file) {
        ModelLoader loader = activeLoaders.get(getFileExtension(file.getName()));
        
        if (loader==null) {
            JOptionPane.showMessageDialog(null, "No Loader for "+getFileExtension(file.getName()));
            return null;
        } else {
            return loader.importModel(file);
        }
    }
    
    /**
     *  Return the set of file extensions that can be loaded
     * @return
     */
    public String[] getLoaderExtensions() {
        return activeLoaders.keySet().toArray(new String[activeLoaders.size()]);
    }
    
    /**
     * Returns the extension of the filename
     * @param filename
     * @return
     */
    private String getFileExtension(String filename) {
        try {
            return filename.substring(filename.lastIndexOf('.')+1);        
        } catch(Exception ex) {
            return null;
        }        
    }
}
