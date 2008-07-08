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

package org.jdesktop.wonderland.modules.archive;

import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.jdesktop.wonderland.modules.Module;
import org.jdesktop.wonderland.modules.ModuleResource;

/**
 * The ArchiveModule class extends the Module abstract base class and represents
 * all modules that are contained within either a JAR or ZIP archive.
 * <p>
 * @author Jordan Slott <jslott@dev.java.net>
 */
public class ArchiveModule extends Module {
    /* The module's archive file in which it is contained */
    private ZipFile zipFile = null;
    
    /** Default constructor, takes a reference to the archive file */
    public ArchiveModule(ZipFile zipFile) {
        super();
        this.zipFile = zipFile;
    }
    
    /**
     * Returns an input stream for the given resource, null upon error.
     * <p>
     * @param resource A resource contained within the archive
     * @return An input stream to the resource
     */
    public InputStream getInputStream(ModuleResource resource) {
        try {
            ZipEntry entry = this.zipFile.getEntry(resource.getPathName());
            return this.zipFile.getInputStream(entry);
        } catch (java.lang.IllegalStateException excp) {
            // print stack trace
            return null;
        } catch (java.io.IOException excp) {
            // print stack trace
            return null;
        }
    }
}
