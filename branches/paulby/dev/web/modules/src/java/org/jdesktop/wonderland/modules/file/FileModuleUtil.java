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

package org.jdesktop.wonderland.modules.file;

import java.io.File;
import java.util.Collection;
import java.util.LinkedList;

/**
 * The ModuleFileUtil class contains a collection is utility routines to help
 * parse the module on disk.
 * <p>
 * @author Jordan Slott <jslott@dev.java.net>
 */
public class FileModuleUtil {
    
    /**
     * Takes a directory and returns a Collection containing all of the files
     * beneath the directory, making a recursive search through the directory
     * structure.
     * <p>
     * The path name returned are with respect to the root directory passed to
     * the first call of this method.
     * <p>
     * @param root The root directory of the search
     * @param dir The directory to recursively enumerate
     * @return A set containing all of the files beneath the directory
     */
    static Collection<String> listModuleArt(File root, File dir) {
        LinkedList<String> list = new LinkedList<String>();
        String rootPathName = root.getAbsolutePath();
        
        /*
         * List all of the files. For those that are directories, recusrively
         * call this method and add the results to what we find here. For all
         * ordinary files, simple add the entry.
         */
        File[] files = dir.listFiles();
        for (File file : files) {
            /* If a directory, then recursively descend and append */
            if (file.isDirectory() == true && file.isHidden() == false) {
                Collection<String> rList = FileModuleUtil.listModuleArt(root, file);
                list.addAll(rList);
            }
            else if (file.isFile() == true && file.isHidden() == false) {
                /*
                 * If a normal, non-hidden file, then add to the set. The name
                 * we use is relative to the root, so we strip that off the
                 * front first
                 */
                String name = file.getAbsolutePath().substring(rootPathName.length() + 1);
                list.addLast(name);
            }
        }
        return list;
    }
}
