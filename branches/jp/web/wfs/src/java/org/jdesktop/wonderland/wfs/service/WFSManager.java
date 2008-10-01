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

package org.jdesktop.wonderland.wfs.service;

import java.io.File;
import java.io.FilenameFilter;
import java.net.URL;
import java.util.HashMap;
import java.util.logging.Logger;
import org.jdesktop.wonderland.utils.SystemPropertyUtil;
import org.jdesktop.wonderland.wfs.WFS;
import org.jdesktop.wonderland.wfs.WFSFactory;

/**
 *TBD
 * 
 * @author Jordan Slott <jslott@dev.java.net>
 */
public class WFSManager {
    
    /* A collection of roots for all WFSs managed herein */
    private HashMap<String, WFS> wfsRoots = new HashMap<String, WFS>();

    /* The logger for the module manager */
    private static final Logger logger = Logger.getLogger(WFSManager.class.getName());

    
    /** Constructor */
    private WFSManager() {
        /* Load in all of the Wonderland file systems */
        this.loadWFSs();
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
        return this.wfsRoots.get(wfsName);
    }
    
    /**
     * Returns an array of WFS root names. If there are no roots, returns null.
     * 
     * @return An array of WFS root names, or null if there are none
     */
    public String[] getWFSRoots() {
        return this.wfsRoots.keySet().toArray(new String[] {});
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
        /* Find the directory in which the roots exist */
        String baseDir = SystemPropertyUtil.getProperty("wonderland.webserver.wfs.root");
        if (baseDir == null) {
            logger.warning("WFSManager: No value for wonderland.webserver.wfs.root");
            return;
        }
        logger.info("WFSManager: wonderland.webserver.wfs.root=" + baseDir);
        
        for (String root : this.getWFSRootDirectories(baseDir)) {
            try {
                WFS wfs = WFSFactory.open(new URL("file://" + baseDir + "/" + root));
                this.wfsRoots.put(this.getWFSName(root), wfs);
            } catch (java.io.FileNotFoundException excp) {
                logger.warning("WFSManager: Unable to find WFS root: " + excp.toString());
            } catch (java.io.IOException excp) {
                logger.warning("WFSManager: Error opening WFS root: " + excp.toString());
            } catch (org.jdesktop.wonderland.wfs.InvalidWFSException excp) {
                logger.warning("WFSManager: Invalid WFS: " + excp.toString());
            } catch (javax.xml.bind.JAXBException excp) {
                logger.warning("WFSManager: Parse error: " + excp.toString());
            }
        }   
    }
    
    /**
     * Returns an array of URL object that represent the base directories for
     * each root WFS in the system.
     */
    private String[] getWFSRootDirectories(String baseDir) {
        /*
         * Search through the directory and get all of the files with a -wfs
         * extension. We check whether the directory is readable and whether
         * it is a valid WFS in the loadWFSs() method.
         */
        File rootDir = new File(baseDir);
        String[] res = rootDir.list(new FilenameFilter() {
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
    
    public static void main(String args[]) {
        WFSManager wfsm = WFSManager.getWFSManager();
        System.out.println(wfsm.getWFS("default"));
    }
}
