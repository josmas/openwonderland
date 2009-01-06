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
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.wonderland.common.wfs.WorldRoot;
import org.jdesktop.wonderland.utils.SystemPropertyUtil;
import org.jdesktop.wonderland.tools.wfs.WFS;
import org.jdesktop.wonderland.tools.wfs.WFSFactory;
import org.jdesktop.wonderland.utils.RunUtil;

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
     * Get all WFS snapshots
     * @return a list of all WFS snapshots
     */
    public List<WFSSnapshot> getWFSSnapshots() {
        return new ArrayList<WFSSnapshot>(wfsSnapshots.values());
    }

    /**
     * Get a particular WFS snapshot by name
     * @return the name of the snapshot
     */
    public WFSSnapshot getWFSSnapshot(String name) {
        return wfsSnapshots.get(name);
    }

    /**
     * Create a new snapshot wfs given its date. This method assumes the snapshot
     * does not already exist
     */
    public WFSSnapshot createWFSSnapshot(String name) {
        File snapshotDir = new File(snapshotFile, name);

        try {
            WFSSnapshot snapshot = WFSSnapshot.getInstance(snapshotDir);
            if (snapshot.getTimestamp() != null) {
                // uh-oh, snapshot already exists...
                logger.log(Level.WARNING, "[WFS] Snapshot " + name + " exists");
            } else {
                // set the timestamp to now
                snapshot.setTimestamp(new Date());

                // update our internal records
                String path = getSnapshotPath(name, snapshot.getPath());
                wfsSnapshots.put(name, snapshot);
                wfsMap.put(path, snapshot.getWfs());
            }

            return snapshot;
        } catch (java.lang.Exception excp) {
            logger.log(Level.WARNING, "[WFS] Unable to create snapshot", excp);
            return null;
        }
    }

    /**
     * Remove a snapshot from WFS
     * @param name the name of the snapshot to remove
     */
    public void removeWFSSnapshot(String name) {
        WFSSnapshot snapshot = wfsSnapshots.remove(name);
        if (snapshot == null) {
            return;
        }

        String path = getSnapshotPath(name, snapshot.getPath());
        wfsMap.remove(path);

        File snapshotDir = new File(snapshotFile, name);
        if (snapshotDir.exists()) {
            RunUtil.deleteDir(snapshotDir);
        }
    }

    /**
     * Rename a snapshot from one name to another
     * @param oldname the original name
     * @param newname the new name
     */
    void renameSnapshot(String oldname, WFSSnapshot snapshot) {
        // remove information about the old snapshot
        String oldpath = getSnapshotPath(oldname, snapshot.getPath());
        wfsSnapshots.remove(oldname);
        wfsMap.remove(oldpath);

        // add the new information
        String newpath = getSnapshotPath(snapshot.getName(), snapshot.getPath());
        wfsSnapshots.put(snapshot.getName(), snapshot);
        wfsMap.put(newpath, snapshot.getWfs());
    }

    /**
     * Get the path to a snapshot
     * @param name the name of the snapshot
     * @param path the snapshot path
     */
    private String getSnapshotPath(String name, String path) {
        return SNAPSHOT_DIRS + File.separator + name + File.separator + path;
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
        for (File root : this.getSnapshotDirectories()) {
            try {
                WFSSnapshot snapshot = WFSSnapshot.getInstance(root);
                String path = getSnapshotPath(root.getName(), snapshot.getPath());
                
                wfsSnapshots.put(root.getName(), snapshot);
                wfsMap.put(path, snapshot.getWfs());
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
    private File[] getSnapshotDirectories() {
        // return an empty array if the directory doesn't exist, otherwise
        // we get NullPointerExceptions
        File[] res = snapshotFile.listFiles();
        if (res == null) {
            res = new File[0];
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
