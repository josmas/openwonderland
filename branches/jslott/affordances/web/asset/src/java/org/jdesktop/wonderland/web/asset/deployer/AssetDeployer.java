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

package org.jdesktop.wonderland.web.asset.deployer;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.wonderland.common.modules.ModuleChecksums;
import org.jdesktop.wonderland.modules.Module;
import org.jdesktop.wonderland.modules.ModulePart;
import org.jdesktop.wonderland.modules.spi.ModuleDeployerSPI;


/**
 * Manages the deployment of artwork.
 * 
 * @author Jordan Slott <jslott@dev.java.net>
 */
public class AssetDeployer implements ModuleDeployerSPI {

    /* Holds map of deployed asset info and root Files for assets */
    private static Map<DeployedAsset, File> assetMap = new HashMap();
    
    /* Holds map of deployed asset info and their checksums */
    private static Map<DeployedAsset, ModuleChecksums> checksumMap = new HashMap();
    
    /**
     * A DeployedAsset represents the <module name, asset type> pair and
     * uniquely identifies a collection of assets managed by this deployer. It
     * contains equals() and hashCode() so that is can be used as a key in a
     * hashtable.
     */
    public static class DeployedAsset {
        public String moduleName = null;
        public String assetType = null;
        
        /** Constructor, takes both arguments */
        public DeployedAsset(String moduleName, String assetType) {
            this.moduleName = moduleName;
            this.assetType = assetType;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final DeployedAsset other = (DeployedAsset) obj;
            if (this.moduleName != other.moduleName && (this.moduleName == null || !this.moduleName.equals(other.moduleName))) {
                return false;
            }
            if (this.assetType != other.assetType && (this.assetType == null || !this.assetType.equals(other.assetType))) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 43 * hash + (this.moduleName != null ? this.moduleName.hashCode() : 0);
            hash = 43 * hash + (this.assetType != null ? this.assetType.hashCode() : 0);
            return hash;
        }
    }
    
    public String getName() {
       return "Asset Deployer";
    }

    public String[] getTypes() {
        return new String[] { "art", "client", "common", "audio" };
    }

    public boolean isDeployable(String type, Module module, ModulePart part) {
        /* Assets is always deployable to here */
        return true;
    }

    public boolean isUndeployable(String type, Module module, ModulePart part) {
        /* Assets is always undeployable to here */
        return true;
    }

    public void deploy(String type, Module module, ModulePart part) {
        /* Check if the checksum file exists, if not generate it */
        File root = part.getFile();
        File parent = root.getParentFile();
        File checksumFile = new File(root, "checksums.xml");
        ModuleChecksums cks = null;
        if (checksumFile.exists() == false) {
            try {
                String sha = ModuleChecksums.SHA1_CHECKSUM_ALGORITHM;
                cks = ModuleChecksums.generate(parent, root, sha, null, null);
                cks.encode(new FileWriter(checksumFile));
            } catch (Exception excp) {
                Logger logger = Logger.getLogger(AssetDeployer.class.getName());
                logger.log(Level.WARNING, "[ART] Unable to write checksums.xml", excp);
            }
        }
        else {
            try {
                cks = ModuleChecksums.decode(new FileReader(checksumFile));
            } catch (Exception excp) {
                /* Log an error but create a checksum map anyhow */
                Logger logger = Logger.getLogger(AssetDeployer.class.getName());
                logger.log(Level.WARNING, "[ART] Unable to read checksums.xml", excp);
                cks = new ModuleChecksums();
            }
        }
        
        /* Add the file root and checksums to the maps */
        DeployedAsset asset = new DeployedAsset(module.getName(), type);
        assetMap.put(asset, root);
        checksumMap.put(asset, cks);
    }

    public void undeploy(String type, Module module, ModulePart part) {
        DeployedAsset asset = new DeployedAsset(module.getName(), type);
        assetMap.remove(asset);
        checksumMap.remove(asset);
    }
    
    /**
     * Returns (a copy of) a map of module assets to their File roots.
     */
    public static Map<DeployedAsset, File> getFileMap() {
        return new HashMap(assetMap);
    }
    
    /**
     * Returns (a copy of) a map of module assets to their checksums.
     */
    public static Map<DeployedAsset, ModuleChecksums> getChecksumMap() {
        return new HashMap(checksumMap);
    }
    
    /**
     * Returns the file root for the module name, or null if it does not exist.
     * 
     * @param type The assset type
     * @param moduleName The module name
     * @return The File directory of the module part
     */
    public static File getFile(String type, String moduleName) {
        return assetMap.get(new DeployedAsset(moduleName, type));
    }
}
