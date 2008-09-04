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

package org.jdesktop.wonderland.modules.memory;

import java.io.InputStream;
import org.jdesktop.wonderland.modules.Module;
import org.jdesktop.wonderland.modules.ModuleResource;

/**
 * The MemoryModule class extends the Module abstract base class and represents
 * all modules that exist entirely in memory.
 * <p>
 * @author Jordan Slott <jslott@dev.java.net>
 */
public class MemoryModule extends Module {
    
    /** Default constructor */
    public MemoryModule() {
        super();
    }
    
    /**
     * Override method from Module superclass, but since the module does not
     * exist on disk anywhere, this method always return null.
     * <p>
     * @param resource A resource contained within the archive
     * @return An input stream to the resource
     */
    public InputStream getInputStreamForResource(ModuleResource resource) {
        return null;
    }
    /**
     * Returns an input stream for the given JAR file from a plugin, null
     * upon error
     * 
     * @param name The name of the plugin
     * @param jar The name of the jar file
     * @param type The type of the jar file (CLIENT, SERVER, COMMON)
     */
    public InputStream getInputStreamForPlugin(String name, String jar, String type) {
        return null;
    }
    
    @Override
    protected void open() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
