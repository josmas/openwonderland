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
package org.jdesktop.wonderland.web.asset.deployer;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.wonderland.common.modules.ModuleChecksums;
import org.jdesktop.wonderland.modules.Module;
import org.jdesktop.wonderland.modules.ModulePart;
import org.jdesktop.wonderland.modules.spi.ModuleDeployerSPI;
import org.jdesktop.wonderland.utils.RunUtil;


/**
 * Manages the deployment of "assets", where assets include jar files, artwork,
 * audio files, etc. Assets are not copied from their modules, rather pointers
 * are created to them. This deployers also generates checksums for each assets
 * and writes out a checksums.xml file.
 * <p>
 * This class implements the ModuleDeployerSPI interface and handles module
 * "parts" defined by the getTypes() method (currently, "art", "client",
 * "common", and "audio").
 * 
 * @author Jordan Slott <jslott@dev.java.net>
 */
public class AssetDeployer implements ModuleDeployerSPI {

    /* The error logger */
    private static Logger logger = Logger.getLogger(AssetDeployer.class.getName());
    
    /* Holds map of deployed asset info and root Files for assets */
    private static Map<DeployedAsset, File> assetMap = new HashMap();
    
    /* Holds map of deployed asset info and their checksums */
    private static Map<DeployedAsset, ModuleChecksums> checksumMap = new HashMap();

    /* The name of the checksums file */
    private static final String CHECKSUMS_FILE = "checksums.xml";

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

    /**
     * @inheritDoc()
     */
    public String getName() {
       return "Asset Deployer";
    }

    /**
     * @inheritDoc()
     */
    public String[] getTypes() {
        return new String[] { "art", "client", "common", "audio" };
    }

    /**
     * @inheritDoc()
     */
    public boolean isDeployable(String type, Module module, ModulePart part) {
        /* Assets is always deployable to here */
        return true;
    }

    /**
     * @inheritDoc()
     */
    public boolean isUndeployable(String type, Module module, ModulePart part) {
        /* Assets is always undeployable to here */
        return true;
    }

    /**
     * @inheritDoc()
     */
    public void deploy(String type, Module module, ModulePart part) {
        // For each module to deploy, we want to generate a checksums file. So
        // we must first get the parent directory of the module part, because
        // that is where the checksums.xml file goes.
        File root = part.getFile();
        File parent = root.getParentFile();
        File checksumFile = new File(root, AssetDeployer.CHECKSUMS_FILE);

        // Next if the checksums file does not exist, then attempt to generate
        // it and write it out. Otherwise, read in the existing checksums file.
        ModuleChecksums checksums = new ModuleChecksums();
        if (checksumFile.exists() == false) {
            // Generate the checksums based upon the files present. If we cannot
            // generate the checksums then just use some blank checksums and
            // log and error
            try {
                // Generate the checksums based upon the files present
                String sha = ModuleChecksums.SHA1_CHECKSUM_ALGORITHM;
                checksums = ModuleChecksums.generate(parent, root, sha, null, null);
            } catch (NoSuchAlgorithmException excp) {
                // Log an error, although this exception should never happen
                logger.log(Level.WARNING, "Unable to generate checksums for" +
                        " module " + module.getName() + " and part " +
                        part.getName(), excp);
                logger.log(Level.WARNING, "Using blank checksums instead.");
            }

            // Write out the newly generated checksums to the file. If we cannot
            // then log an error. Make sure the writer is closed under any event
            // however.
            FileWriter writer = null;
            try {
                // Write the checksums out to a checksums file
                writer = new FileWriter(checksumFile);
                checksums.encode(writer);
            } catch (java.lang.Exception excp) {
                logger.log(Level.WARNING, "Unable to write checksums.xml to " +
                        checksumFile.getAbsolutePath() + " for module name " +
                        module.getName() + " and part " + part.getName(), excp);
            } finally {
                RunUtil.close(writer);
            }
        }
        else {
            // Otherwise, read in the checksums file from disk. If there is an
            // error, just use a blank checksums. Make sure the reader is
            // closed under any event however.
            FileReader reader = null;
            try {
                reader = new FileReader(checksumFile);
                checksums = ModuleChecksums.decode(reader);
            } catch (java.lang.Exception excp) {
                /* Log an error but create a checksum map anyhow */
                logger.log(Level.WARNING, "Unable to reader checksums.xml from " +
                        checksumFile.getAbsolutePath() + " for module name " +
                        module.getName() + " and part " + part.getName(), excp);
                logger.log(Level.WARNING, "Using blank checksums instead.");
            } finally {
                RunUtil.close(reader);
            }
        }
        
        // Add the file root and checksums to the maps of deployed assets
        DeployedAsset asset = new DeployedAsset(module.getName(), type);
        assetMap.put(asset, root);
        checksumMap.put(asset, checksums);
    }

    /**
     * @inheritDoc()
     */
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
