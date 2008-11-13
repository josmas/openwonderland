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
package org.jdesktop.wonderland.common;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 *
 * @author paulby
 */
public class FileUtils {

    /**
     * Returns the extension of the filename, or null if there is no extension
     * @param filename
     * @return
     */
    public static String getFileExtension(String filename) {
        try {
            return filename.substring(filename.lastIndexOf('.')+1);        
        } catch(Exception ex) {
            return null;
        }        
    }
    
    /**
     * Copy the file from in to out
     * @param in
     * @param out
     * @throws java.io.IOException
     */
    public static void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buf = new byte[1024*1024];

        int size;
        while ((size = in.read(buf))!=-1) {
            out.write(buf, 0, size);
        }

    }
}
