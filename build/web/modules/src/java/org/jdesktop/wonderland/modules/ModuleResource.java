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

package org.jdesktop.wonderland.modules;

/**
 * The ModuleResource abstract base class represents a downloadable resource
 * within a module, such as an artwork resource, a jar plugin, etc. This class
 * keeps the unique path within the module. This class is typically subclassed
 * by resource-specific subclasses for artwork, plugins, etc. 
 * <p>
 * @author Jordan Slott <jslott@dev.java.net>
 */
public abstract class ModuleResource {

    /* The path name of the resource */
    private String pathName = null;
    
    /** Constructor, takes path name as an argument */
    ModuleResource(String pathName) {
        this.pathName = pathName;
    }
    
    /**
     * Returns the unique path name within the module of this resource.
     * <p>
     * @return The path name of the resource within the module
     */
    public String getPathName() {
        return this.pathName;
    }
            
    /**
     * Returns a string representation of this resource
     * <p>
     * @return A string representation
     */
    @Override
    public String toString() {
        return this.getPathName();
    }
}
