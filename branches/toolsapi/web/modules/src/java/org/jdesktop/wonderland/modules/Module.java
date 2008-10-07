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

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Iterator;

/**
 * The Module class represents a single module within Wonderland. A module
 * consists of several possible subcomponents: artwork, WFSs, and plugins.
 * Artwork either includes textures, images, and 3D geometry. Modules may also
 * contain Wonderland Filesystems (WFSs) that assemble the artwork resources
 * into a subworld component. Plugins are runnable code that extend the
 * functionality of the server and/or client.
 * <p>
 * A module is stored within a jar/zip archive file. To open an existing module
 * archive file, use the ModuleFactory.open() method. Once open, users of this
 * class may query for the module's artwork, WFSs, and plugins.
 * <p>
 * Modules also have major.minor version numbers and a list of other modules
 * upon which this module depends.
 * <p>
 * This is an abstract class -- it is typically subclassed to handle whether
 * the module was loaded on disk or from an archive file.
 * <p>
 * @author Jordan Slott <jslott@dev.java.net>
 */
public abstract class Module {

    /* Useful names of files within the archive */
    public static final String MODULE_INFO       = "module.xml";
    public static final String MODULE_REQUIRES   = "requires.xml";
    public static final String MODULE_REPOSITORY = "repository.xml";
    public static final String MODULE_ART        = "art";
    public static final String MODULE_CHECKSUMS  = "checksums.xml";
    public static final String MODULE_WFS        = "wfs";
    public static final String MODULE_PLUGINS    = "plugins";
    
    /** Default constructor */
    protected Module() {}
    
    /**
     * Returns the name of the module.
     * 
     * @return The module's name
     */
    public abstract String getName();
    
    /**
     * Removes the module entirely. Returns true upon success, false upon
     * failure.
     * 
     * @return True if the module was successfully deleted, false if not.
     */
    public abstract boolean delete();
    
    /**
     * Returns the basic information about a module: its name and version.
     * <p>
     * @return The basic module information
     */
    public abstract ModuleInfo getModuleInfo();
    
    /**
     * Returns the module's dependencies.
     * <p>
     * @return The module's dependencies
     */
    public abstract ModuleRequires getModuleRequires();

    /**
     * Returns the module's repository information.
     * <p>
     * @return The module's repository information
     */
    public abstract ModuleRepository getModuleRepository();

    /**
     * Returns the module's collection of artwork resources.
     * <p>
     * @return The module's collection of artwork
     */
    public abstract Collection<String> getModuleArtResources();

    /**
     * Returns the module artwork resource given its relative path, or null if
     * it does not exist
     * 
     * @return The module's artwork or null if it does not exist
     */
    public abstract ModuleArtResource getModuleArtResource(String path);

    /**
     * Returns a map of WFS objects for all WFSs contained within the module.
     * The key for each entry of the map is the name of the WFS (without the
     * -wfs extension) and the value is its WFS object. If no WFSs exist within
     * the module, this method returns an empty map.
     *
     * @return A map of WFS entries within the module
     */
    public abstract Collection<String> getModuleWFSs();

    /**
     * Returns a map of ModulePlugin objects for all plugins contained within
     * the module. The key for each entry of the map is the name of the plugin
     * and the value is its ModulePlugin object. If no plugins exist within
     * the module, this method returns an empty map.
     *
     * @return A map of plugin entries within the module
     */
    public abstract Collection<String> getModulePlugins();

    /**
     * Returns a module's plugin given its name, null if the plugin does not
     * exists.
     * 
     * @param name The unique name of the plugin
     * @return The ModulePlugin object
     */
    public abstract ModulePlugin getModulePlugin(String name);

    /**
     * Returns a collection of checksums for the resources in the module.
     * 
     * @returns The collection of resource checksums
     */
    public abstract ModuleChecksums getModuleChecksums();
    
    /**
     * Returns an input stream for the given resource, null upon error.
     * <p>
     * @param resource A resource contained within the archive
     * @return An input stream to the resource
     */
    public abstract InputStream getInputStreamForResource(ModuleResource resource);

    /**
     * Returns an input stream for the given JAR file from a plugin, null
     * upon error
     * 
     * @param name The name of the plugin
     * @param jar The name of the jar file
     * @param type The type of the jar file (CLIENT, SERVER, COMMON)
     */
    public abstract InputStream getInputStreamForPlugin(String name, String jar, String type);
        
    /**
     * Returns a string representing this module.
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("---------------------------------------------------------------------------\n");
        sb.append(this.getModuleInfo().toString() + "\n");
        sb.append("Depends upon " + this.getModuleRequires().toString() + "\n");
        sb.append("Repositories\n" + this.getModuleRepository().toString() + "\n");
        sb.append("\nPlugins\n");
        Iterator<String> it = this.getModulePlugins().iterator();
        while (it.hasNext() == true) {
            ModulePlugin plugin = this.getModulePlugin(it.next());
            sb.append("\t" + plugin.toString() + "\n");
        }
        sb.append("\nArtwork resources\n");
        Iterator<String> it2 = this.getModuleArtResources().iterator();
        while (it2.hasNext() == true) {
            ModuleArtResource art = this.getModuleArtResource(it2.next());
            sb.append("\t" + art.getPathName() + "\n");
        }
        sb.append("\nWFS\n");
        Iterator<String> it3 = this.getModuleWFSs().iterator();
        while (it3.hasNext() == true) {
            String name = it3.next();
            sb.append("\t" + name + "\n");
        }
        sb.append("---------------------------------------------------------------------------\n");
        return sb.toString();
    }
}
