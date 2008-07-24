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
package org.jdesktop.wonderland.wfs.memory;

import org.jdesktop.wonderland.wfs.WFS;
import org.jdesktop.wonderland.wfs.WFSRootDirectory;

/**
 * The MemoryWFS class extends the WFS abstract class and represents a Wonderland
 * File System that resides entire in memory.
 * <p>
 * @author Jordan Slott <jslott@dev.java.net>
 */
public class MemoryWFS extends WFS {
    
    /* The directory object associated with the root of the file system */
    private WFSRootDirectory directory = null;
    
    /**
     * Creates a new instance of WFS.
     */
    public MemoryWFS(String name) {
        super();
        this.directory = new WFSMemoryRootDirectory(name + WFS.WFS_DIRECTORY_SUFFIX);
    }
    
    /**
     * Returns the root cell directory class representing of the WFS.
     * 
     * @return The directory containing the children in the root of the WFS
     */
    public WFSRootDirectory getRootDirectory() {
        return this.directory;
    }
        
    /**
     * Writes the entire WFS to the underlying medium, including the meta-
     * information contains within the root directory, the cells containing
     * within the root directory, and any child directories.
     */
    @Override
    public void write() {
        // Not supported, since nothing to write to!
        throw new UnsupportedOperationException("Not supported. Please use WFS.writeTo()");
    }
}
