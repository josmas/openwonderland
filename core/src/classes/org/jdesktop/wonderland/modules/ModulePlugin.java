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

import java.util.Collection;

/**
 * A ModulePlugin represents a plugin-module that extends the functionality of
 * the Wonderland server and/or client. A module consists of a collection of
 * jar files that are either included in the server classpath, downloaded into
 * the client Java VM, or both.
 * <p>
 * This class enumerates the unique locations within the module for each jar
 * file. (The Wonderland server may then stream the client-side jar files over
 * an http connection knowing their location within the server installation).
 * <p>
 * @author Jordan Slott <jslott@dev.java.net>
 */
public class ModulePlugin {

    /** Default constructor */
    ModulePlugin() {}
    
    /**
     * Returns a collection of server-side component paths within the module.
     * <p>
     * @return A collection of path names to server-side components.
     */
    public Collection<String> getServerJars() {
        return null; // XXX
    }
    
    /**
     * Returns a connection of client-side component paths within the module.
     * <p>
     * @return A collection of path names to client-side components.
     */
    public Collection<String> getClientJars() {
        return null; // XXX
    }
}
