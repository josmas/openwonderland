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

import java.util.Iterator;

/**
 * The ModuleUtils class contains a collection of static utility routines.
 * 
 * @author Jordan Slott <jslott@dev.java.net>
 */
public class ModuleUtils {

    /**
     * Returns a string listing all of the plugin names for a module, or an
     * empty string if no plugin names exist.
     * 
     * @return The names of all plugins
     */
    public static String getPluginNames(Module module) {
        StringBuilder sb = new StringBuilder();
        if (module.getModulePlugins() != null) {
            Iterator<String> it = module.getModulePlugins().iterator();
            while (it.hasNext() == true) {
                sb.append(it.next() + " ");
            }
        }
        return sb.toString();
    }
    
    /**
     * Returns a string listing all of the wfs names for a module, or an
     * empty string if no wfs names exist.
     * 
     * @return The names of all wfs
     */
    public static String getWFSNames(Module module) {
        StringBuilder sb = new StringBuilder();
        if (module.getModuleWFSs() != null) {
            Iterator<String> it = module.getModuleWFSs().iterator();
            while (it.hasNext() == true) {
                sb.append(it.next() + " ");
            }
        }
        return sb.toString();
    }
}
