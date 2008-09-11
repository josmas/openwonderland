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
import java.io.InputStream;
import org.jdesktop.wonderland.modules.ModuleResource;
import org.jdesktop.wonderland.modules.archive.ArchiveModule;

/**
 * A pending module is one that has been compiled, configured, and has had its
 * dependencies checked and verified. The only thing remaining, which should
 * happen during the next restart of the server is that its files are copied
 * into the proper location (typically into the installed/ subdirectory).
 * 
 * @author Jordan Slott <jslott@dev.java.net>
 */
public class PendingModule extends ArchiveModule {
    
    /* The File object of the archive */
    private File file = null;
    
    public PendingModule(File root) throws IOException {
        super(root.toURL());
        this.file = root;
    }
    
    /**
     * Returns the file associated with the archive
     *
     * @return The file associated with the archive
     */
    public File getFile() {
        return this.file;
    }
    
    @Override
    public InputStream getInputStreamForResource(ModuleResource resource) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    /**
     * Opens the module by reading its contents.
     */
    @Override
    public void open() {
        super.open();
    }
    
    /**
     * Opens a pending module given its file, returns a new instance of this
     * class.
     * 
     * @param root The base directory of the module
     * @return The module object
     */
    public static final PendingModule getPendingModule(File root) {
        try {
            PendingModule im = new PendingModule(root);
            im.open();
            return im;
        } catch (java.io.IOException excp) {
            // log an error
        }
        return null;
    }
}
