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


package org.jdesktop.wonderland.modules.service;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;
import org.apache.commons.io.FileUtils;

/**
 * The ModuleManagerUtils class contains a collection of static utility methods
 * to help module management.
 * 
 * @author Jordan Slott <jslott@dev.java.net>
 */
public class ModuleManagerUtils {

    /**
     * Creates a directory, and any necessary parent directories, if it does
     * not exist. Throws IOException upon error.
     * 
     * @param root The directory to create
     */
    public static void makeDirectory(File root) throws IOException {
        try {
            /* Create the directory, if false, throw IOException */
            if (root.exists() == false) {
                if (root.mkdirs() == false) {
                    throw new IOException("Failed to create " + root.getAbsolutePath());
                }
            }
        } catch (java.lang.SecurityException excp) {
            throw new IOException(excp.toString());
        }
    }
    
    /**
     * Creates a directory if it does not exist. If it does exist, then remove
     * any exist directory contents. Returns true upon success, false upon
     * failure.
     * 
     * @param root The directory to create
     */
    public static boolean makeCleanDirectory(File root) {
        Logger logger = ModuleManager.getLogger();
        if (root.exists() == true) {
            /* Log an info message, and try to clean the existing directory */
            try {
                FileUtils.cleanDirectory(root);
                return true;
            } catch (java.io.IOException excp) {
                /* If we cannot delete the existing directory, this is fatal */
                logger.warning("[MODULES] MAKE CLEAN Failed " +  excp.toString());
                return false;
            }
        }
        
        /* Now go ahead and recreate the directory */
        try {
            root.mkdir();
        } catch (java.lang.SecurityException excp) {
            logger.warning("[MODULES] MAKE CLEAN Failed " + excp.toString());
            return false;
        }
        return true;
    }
}
