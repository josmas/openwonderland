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

package org.jdesktop.wonderland.web.wfs;

import java.io.File;
import java.io.FilenameFilter;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.wonderland.common.wfs.WorldRoot;
import org.jdesktop.wonderland.utils.SystemPropertyUtil;
import org.jdesktop.wonderland.tools.wfs.WFS;
import org.jdesktop.wonderland.tools.wfs.WFSFactory;

/**
 *TBD
 * 
 * @author Jordan Slott <jslott@dev.java.net>
 */
public class WFSManager {

    /* The location (beneath the wfs root) of the main wfs directories */
    public static final String WORLD_DIRS = "worlds";
    
    /* The location (beneath the wfs root) of the wfs snapshot directories */
    public static final String SNAPSHOT_DIRS = "snapshots";
    
    /* For each snapshot, the wfs has this named beneath a time-stamped directory */
    public static final String SNAPSHOT_WFS = "world-wfs";
    
    /* The File objects to hold the root of the wfs and snapshot directories */
    private File wfsFile = null;
    private File snapshotFile = null;
     
    /* The property storing the base directory for all wfs information */
    private static final String WFS_ROOT_PROPERTY = "wonderland.webserver.wfs.root";
    
    /* A map of WFS worlds (given by name and WFS object) */
    private Map<String, WFS> wfsRoots = new HashMap();

    /* A map of all snapshots of worlds (given by name and snapshot object) */
    // NEED TO MAKE MT SAFE XXXX
    private Map<String, WFSSnapshot> wfsSnapshots = new HashMap();
    
    /*
     * A map of all wfs, both worlds and snapshots of worlds, where the key
     * is the root path. These are of the form "worlds/default-wfs" and
     * "snapshots/<date>/world-wfs". Note the "-wfs" suffix is present in these
     * names.
     */
    // NEED TO MAKE MT SAFE XXX
    private Map<String, WFS> wfsMap = new HashMap();

    /* The logger for the module manager */
    private static final Logger logger = Logger.getLogger(WFSManager.class.getName());

    /**
     * Represents a collection of snapshot wfs directories, contained the name
     * of the wfs and the date of the shapshot and the WFS object that points to
     * it.
     */
    public static class WFSSnapshot {
        private String wfsName = null;
        private String wfsDate = null;
        private WFS wfs = null;
        
        /** Constructor, takes the wfs name, date, and WFS object */
        public WFSSnapshot(String wfsName, String wfsDate, WFS wfs) {
            this.wfsName = wfsName;
            this.wfsDate = wfsDate;
            this.wfs = wfs;
        }

        public WFS getWfs() { return wfs; }
        public String getWfsDate() { return wfsDate; }
        public String getWfsName() { return wfsName; }
    }
    
    /** Constructor */
    private WFSManager() {
        /* Load in all of the Wonderland file systems */
        this.createDirectories();
        this.loadWFSs();
        this.loadSnapshots();
    }
    
    /**
     * Singleton to hold instance of ModuleManager. This holder class is loaded
     * on the first execution of ModuleManager.getModuleManager().
     */
    private static class WFSManagerHolder {
        private final static WFSManager wfsManager = new WFSManager();
    }
    
    /**
     * Returns a single instance of this class
     * <p>
     * @return Single instance of this class.
     */
    public static final WFSManager getWFSManager() {
        return WFSManagerHolder.wfsManager;
    }
    
    /**
     * Returns the error logger associated with this class.
     * 
     * @return The error logger
     */
    public static Logger getLogger() {
        return WFSManager.logger;
    }
    
    /**
     * Gets the WFS for the given name (without the '-wfs' extension) or null
     * if it does not exist.
     * 
     * @param wfsName The unique name of the file system (without the '-wfs' extension)
     * @return The corresponding WFS
     */
    public WFS getWFS(String wfsName) {
        this.loadWFSs();
        return this.wfsRoots.get(wfsName);
    }
    
    /**
     * Returns the WFS object representing the given world root path. Returns
     * null if it does not exist. Examples of world root paths include
     * "worlds/default-wfs" and "snapshots/<date>/world-wfs".
     * 
     * @param worldPath The path to the wfs world
     * @return the WFS object
     */
    public WFS getWFS(WorldRoot worldRoot) {
        return wfsMap.get(worldRoot.getRootPath());
    }
    
    /**
     * Returns an array of WFS root names. If there are no roots, returns null.
     * 
     * @return An array of WFS root names, or null if there are none
     */
    public String[] getWFSRoots() {
        this.loadWFSs();
        return this.wfsRoots.keySet().toArray(new String[] {});
    }
    
    /**
     * Create a new snapshot wfs given its date. This method assumes the snapshot
     * does not already exist
     */
    public WFS createWFSSnapshot(String dateString) {
        File file = new File(this.snapshotFile, dateString + File.separator + SNAPSHOT_WFS);
        try {
            WFS wfs = WFSFactory.create(file.toURL());
            WFSSnapshot wfsSnapshot = new WFSSnapshot(wfs.getName(), dateString, wfs);
            wfsSnapshots.put(dateString, wfsSnapshot);

            String path = SNAPSHOT_DIRS + File.separator + dateString + File.separator + SNAPSHOT_WFS;
            wfsMap.put(path, wfs);
            return wfs;
        } catch (java.lang.Exception excp) {
            logger.log(Level.WARNING, "[WFS] Unable to create snapshot", excp);
            return null;
        }
    }
    
    /**
     * Takes a URL and returns the final name in the path (without the -wfs
     * extension.
     */
    private String getWFSName(String root) {
        if (root.endsWith(WFS.WFS_DIRECTORY_SUFFIX) == true) {
            return root.substring(0, root.length() - WFS.WFS_DIRECTORY_SUFFIX.length());
        }
        return root;
    }
    
    /**
     * Loads in all of the Wonderland file systems specified in the roots. Adds
     * to the internal list.
     */
    private void loadWFSs() {
        /* Clear out the existing list */
        this.wfsRoots.clear();
        
        for (String root : this.getWFSRootDirectories()) {
            try {
                File file = new File(this.wfsFile, root);
                WFS wfs = WFSFactory.open(file.toURL());
                this.wfsRoots.put(this.getWFSName(root), wfs);
                
                String path = WORLD_DIRS + File.separator + root;
                wfsMap.put(path, wfs);
            } catch (java.lang.Exception excp) {
                logger.log(Level.WARNING, "[WFS] Unable to create WFS", excp);
            }
        }   
    }
    
    /**
     * Loads in all of the snapshots of wfs. Adds to the internal list
     */
    private void loadSnapshots() {
        //XXX when do we clear out the existing lists?
        
        for (String root : this.getSnapshotDirectories()) {
            try {
                File file = new File(this.snapshotFile, root + File.separator + SNAPSHOT_WFS);
                WFS wfs = WFSFactory.open(file.toURL());
                WFSSnapshot wfsSnapshot = new WFSSnapshot(wfs.getName(), root, wfs);
                this.wfsSnapshots.put(root, wfsSnapshot);
                
                String path = SNAPSHOT_DIRS + File.separator + root + File.separator + SNAPSHOT_WFS;
                wfsMap.put(path, wfs);
            } catch (java.lang.Exception excp) {
                logger.log(Level.WARNING, "[WFS] Unable to create WFS", excp);
            }
        }
    }
    
    /**
     * Returns an array of URL object that represent the base directories for
     * each root WFS in the system.
     */
    private String[] getWFSRootDirectories() {
        /*
         * Search through the directory and get all of the files with a -wfs
         * extension. We check whether the directory is readable and whether
         * it is a valid WFS in the loadWFSs() method.
         */
        String[] res = wfsFile.list(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.endsWith(WFS.WFS_DIRECTORY_SUFFIX);
            }
        });
        
        // return an empty array if the directory doesn't exist, otherwise
        // we get NullPointerExceptions
        if (res == null) {
            res = new String[0];
        }
        
        return res;
    }
 
    /**
     * Returns an array of file names that represent the base directories for
     * each wfs in the snapshots directory
     */
    private String[] getSnapshotDirectories() {        
        // return an empty array if the directory doesn't exist, otherwise
        // we get NullPointerExceptions
        String[] res = snapshotFile.list();
        if (res == null) {
            res = new String[0];
        }
        
        return res;
    }
    
    /**
     * Creates all of the important WFS directories, if they do not already
     * exists. Open File objects for each for later use.
     */
    private void createDirectories() {
        /* Fetch the WFS base-level directory from the property */
        String baseDir = null;
        if ((baseDir = getBaseWFSDirectory()) == null) {
            logger.severe("[WFS] Invalid WFS Base Directory! Will not load WFS");
            logger.severe("[WFS] Make sure " + WFS_ROOT_PROPERTY + " property is set");
            return;
        }
        
        makeDirectory(baseDir);
        this.wfsFile = makeDirectory(baseDir + File.separator + WORLD_DIRS);
        this.snapshotFile = makeDirectory(baseDir + File.separator + SNAPSHOT_DIRS);
    }
    
    /**
     * Makes a directory if it does not exist and returns the file object of
     * the directory. Returns null upon error creating the directory. 
     */
    private File makeDirectory(String path) {
        try {
            File file = new File(path);
            if (file.exists() == true) {
                return file;
            }
            else if (file.mkdirs() == true) {
                return file;
            }
        } catch (java.lang.Exception excp) {
            logger.log(Level.SEVERE, "[WFS] Failed to create directory " + path, excp);
            return null;
        }
        
        logger.severe("[WFS] Failed to create directory " + path);
        logger.severe("[WFS] Make sure " + WFS_ROOT_PROPERTY + " points to a valid location");
        return null;
    }
    
    /**
     * Returns the base directory for all WFS information, or null if the
     * property is not set.
     */
    private String getBaseWFSDirectory() {
        return SystemPropertyUtil.getProperty(WFS_ROOT_PROPERTY);
    }
    
    public static void main(String args[]) {
        WFSManager wfsm = WFSManager.getWFSManager();
        System.out.println(wfsm.getWFS("default"));
    }
}
