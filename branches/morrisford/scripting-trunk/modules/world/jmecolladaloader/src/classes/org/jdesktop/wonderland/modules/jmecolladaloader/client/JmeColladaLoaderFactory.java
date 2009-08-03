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
package org.jdesktop.wonderland.modules.jmecolladaloader.client;

import org.jdesktop.wonderland.client.ClientPlugin;
import org.jdesktop.wonderland.client.jme.artimport.LoaderManager;
import org.jdesktop.wonderland.client.jme.artimport.ModelLoader;
import org.jdesktop.wonderland.client.jme.artimport.ModelLoaderFactory;
import org.jdesktop.wonderland.client.login.ServerSessionManager;

/**
 * LoaderFactory for the JmeColladaLoader.
 * 
 * @author paulby
 */
public class JmeColladaLoaderFactory extends ModelLoaderFactory
    implements ClientPlugin
{

    public void initialize(ServerSessionManager manager) {
        LoaderManager.getLoaderManager().registerLoader(this);
    }
    
    public String getFileExtension() {
        return "dae";
    }

    public ModelLoader getLoader() {
        return (ModelLoader) new JmeColladaLoader();
    }

}