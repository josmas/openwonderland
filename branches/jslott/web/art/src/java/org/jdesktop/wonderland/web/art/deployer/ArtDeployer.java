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

package org.jdesktop.wonderland.web.art.deployer;

import java.io.File;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.wonderland.modules.Module;
import org.jdesktop.wonderland.modules.ModuleChecksums;
import org.jdesktop.wonderland.modules.ModulePart;
import org.jdesktop.wonderland.modules.spi.ModuleDeployerSPI;


/**
 * Manages the deployment of artwork.
 * 
 * @author Jordan Slott <jslott@dev.java.net>
 */
public class ArtDeployer implements ModuleDeployerSPI {

    /* Holds map of module names and root Files */
    private static Map<String, File> artMap = new HashMap();
    
    public String getName() {
       return "Artwork Deployer";
    }

    public String[] getTypes() {
        return new String[] { "art" };
    }

    public boolean isDeployable(String type, Module module, ModulePart part) {
        /* Art is always deployable */
        return true;
    }

    public boolean isUndeployable(String type, Module module, ModulePart part) {
        /* Art is always undeployable */
        return true;
    }

    public void deploy(String type, Module module, ModulePart part) {
        /* Check if the checksum file exists, if not generate it */
        File root = part.getFile();
        File checksum = new File(root, "checksums.xml");
        if (checksum.exists() == false) {
            try {
                ModuleChecksums cks = ModuleChecksums.generate(root,
                        ModuleChecksums.SHA1_CHECKSUM_ALGORITHM, null, null);
                cks.encode(new FileWriter(checksum));
            } catch (Exception excp) {
                Logger logger = Logger.getLogger(ArtDeployer.class.getName());
                logger.log(Level.WARNING, "[ART] Unable to write checksums.xml", excp);
            }
        }
        
        /* Add to thte map */
        artMap.put(module.getName(), root);
    }

    public void undeploy(String type, Module module, ModulePart part) {
        /* Just remove from the map */
        artMap.remove(module.getName());
    }
    
    /**
     * Returns (a copy of) a map of module names to their File roots
     */
    public static Map<String, File> getFileMap() {
        return new HashMap(artMap);
    }
    
    /**
     * Returns the file root for the module name, or null if it does not exist.
     * 
     * @param moduleName The module name
     * @return The File directory of the module part
     */
    public static File getFile(String moduleName) {
        return artMap.get(moduleName);
    }
}
