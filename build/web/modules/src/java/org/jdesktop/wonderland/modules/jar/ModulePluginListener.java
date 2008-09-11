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
 * The ModulePluginListener class listens for requests to return an input
 * stream for a JAR file contained within a plugin within a module.
 * 
 * @author Jordan Slott <jslott@dev.java.net>
 */
public interface ModulePluginListener {

    /**
     * Returns an input stream for a plugin JAR, given its path.
     * 
     * @param jarPath The path of the plugin JAR in the module
     * @param type The type of the JAR (client/, server/, or common/)
     * @return An input stream to read the plugin JAR data
     */
    public InputStream getInputStreamForPluginJAR(String jarPath, String type);
}
