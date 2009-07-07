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

import com.jme.scene.Node;
import java.io.File;
import java.io.IOException;
import org.jdesktop.wonderland.common.cell.state.CellServerState;

/**
 * Interface for Model loader code. Provides support for inital import of the 
 * model from its original art files, deployment of the assets into a module 
 * and runtime load of the assets from the module
 * 
 * @author paulby
 */
public interface ModelLoader {

    /**
     * Import the model from it's original source
     * 
     * @param file
     * @return
     */
    public Node importModel(File file) throws IOException;
    
    /**
     * Deploy the art content to the module.
     * @param rootDir the art root directory of the module (usually <module>/art)
     */
    public ModelDeploymentInfo deployToModule(File moduleRootDir, ImportedModel model) throws IOException;
    
    /**
     * Runtime load of the model from a module
     */
//    public Node loadModel(URL url);
    

    public class ModelDeploymentInfo {
//        private String assetURL;
//
//        /**
//         * @return the assetURL
//         */
//        public String getAssetURL() {
//            return assetURL;
//        }
//
//        /**
//         * @param assetURL the assetURL to set
//         */
//        public void setAssetURL(String assetURL) {
//            this.assetURL = assetURL;
//        }
        
        private CellServerState setup;
        
        public CellServerState getCellSetup() {
            return setup;
        }
        
        public void setCellSetup(CellServerState setup) {
            this.setup = setup;
        }
    }
}
