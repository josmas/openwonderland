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

package org.jdesktop.wonderland.modules.jar;

import java.io.InputStream;

/**
 * The ModuleResourceListener class listens for requests to return an input
 * stream for a resource that will be written to a module JAR archive file.
 * 
 * @author Jordan Slott <jslott@dev.java.net>
 */
public interface ModuleResourceListener {

    /**
     * Returns an input stream for an artwork resource, given its path.
     * 
     * @param resourcePath The path of the resource in the module
     * @return An input stream to read the resource data
     */
    public InputStream getInputStreamForResource(String resourcePath);
}
