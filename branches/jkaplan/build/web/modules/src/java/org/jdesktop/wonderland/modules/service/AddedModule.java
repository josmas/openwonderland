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
import java.util.zip.ZipFile;
import org.jdesktop.wonderland.modules.archive.ArchiveModule;

/**
 * TBD
 * 
 * @author Jordan Slott <jslott@dev.java.net>
 */
public class AddedModule extends ArchiveModule {

    /* The File object of the archive */
    private File file = null;
    
    /**
     * Constructor TBD
     * @param root
     */
    public AddedModule(File file) throws IOException {
        super(file.toURL());
        this.file = file;
    }
    
    /**
     * Returns the file associated with the archive
     *
     * @return The file associated with the archive
     */
    public File getFile() {
        return this.file;
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
    public static final AddedModule getAddModule(File file) {
        try {
            AddedModule im = new AddedModule(file);
            im.open();
            return im;
        } catch (java.io.IOException excp) {
            // log an error XXX
            return null;
        }
    }
}
