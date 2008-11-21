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
package org.jdesktop.wonderland.webserver.launcher;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author kaplanj
 */
public class FileListUtil {
    /**
     * Get the set of changes needed to make the dest directory equivalent
     * to the source directory
     * @param src the name of the source directory, assumed to be in
     * the form of a resource available from the current file
     * @param dest the destination directory
     * @param addedFiles a list that will be populated with the set of
     * files to add
     * @param removedFiles a list that will be populated with the set of
     * files to remove
     * @throws IOException if there is an error reading any of the data
     */
    public static void compareDirs(String src, File dest,
                                   List<String> addedFiles,
                                   List<String> removedFiles)
        throws IOException
    {
        Map<String, String> srcFiles = null;
        Map<String, String> destFiles = null;

        srcFiles = readChecksums(src);

        File destFileList = new File(dest, "files.list");
        if (destFileList.exists()) {
            destFiles = readChecksums(new FileInputStream(destFileList));
        }
        if (destFiles == null) {
            destFiles = new HashMap<String, String>();
        }

        // calculate additions
        for (Map.Entry<String, String> e : srcFiles.entrySet()) {
            String checksum = destFiles.remove(e.getKey());
            if (checksum == null || !checksum.equals(e.getValue())) {
                addedFiles.add(e.getKey());
            }
        }

        // anything left in the destFiles map should be removed
        removedFiles.addAll(destFiles.keySet());
    }

    public static Map<String, String> readChecksums(String src)
        throws IOException
    {
        String listFile = "/" + src + "/files.list";
        InputStream is = FileListUtil.class.getResourceAsStream(listFile);
        if (is != null) {
            return readChecksums(is);
        } else {
            return new HashMap<String, String>();
        }
    }

    public static Map<String, String> readChecksums(InputStream is)
        throws IOException
    {
        Map<String, String> out = new HashMap();

        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        String line;
        while ((line = br.readLine()) != null) {
            String[] vals = line.split(" ");
            out.put(vals[0], vals[1]);
        }

        return out;
    }
}
