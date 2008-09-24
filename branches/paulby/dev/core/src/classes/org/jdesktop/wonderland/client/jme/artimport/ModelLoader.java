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

/**
 * Interface for Model loader code. Provides support for inital import of the 
 * model from its original art files, deployment of the assets into a module 
 * and runtime load of the assets from the module
 * 
 * @author paulby
 */
public interface ModelLoader {

    /**
     * @return the file extensions supported by the loader
     */
    public String getFileExtension();
    
    /**
     * Import the model from it's original source
     * 
     * @param file
     * @return
     */
    public Node importModel(File file);
    
//    public void deployToModule();
    
    /**
     * Runtime load of the model from a module
     */
//    public Node loadModel(URL url);
    
    /**
     * 
     * @return true if this loader is enabled
     */
    public boolean isEnabled();

    /**
     * Set whether or not this loader is enabled.
     * @param b
     */
    public void setEnabled(boolean enabled);
}
