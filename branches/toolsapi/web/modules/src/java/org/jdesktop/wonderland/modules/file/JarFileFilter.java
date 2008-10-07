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
import java.io.FilenameFilter;

/**
 * Takes a directory and returns the names of all of the files ending in
 * .jar.
 * 
 * @author Jordan Slott <jslott@dev.java.net>
 */
public class JarFileFilter implements FilenameFilter {

    /** Constructor */
    public JarFileFilter() {
    }

    /**
     * Tests whether or not the specified abstract pathname should be
     * included in a pathname list.
     */
    public boolean accept(File dir, String name) {
        File file = new File(dir, name);
        return file.isFile() == true && file.isHidden() == false && name.endsWith(".jar") == true;
    }
}