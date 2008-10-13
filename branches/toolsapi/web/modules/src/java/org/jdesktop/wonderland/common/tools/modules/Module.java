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

package org.jdesktop.wonderland.common.tools.modules;

import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import org.jdesktop.wonderland.common.tools.wfs.WFS;

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

    /* Support URL protocols for file systems */
    public static final String FILE_PROTOCOL = "file";
    public static final String JAR_PROTOCOL  = "jar";
    
    /* Useful names of files within the archive */
    public static final String MODULE_INFO       = "module.xml";
    public static final String MODULE_REQUIRES   = "requires.xml";
    public static final String MODULE_REPOSITORY = "repository.xml";
    public static final String MODULE_ART        = "art";
    public static final String MODULE_CHECKSUMS  = "checksums.xml";
    public static final String MODULE_WFS        = "wfs";
    public static final String MODULE_PLUGINS    = "plugins";
 
    private ModuleInfo moduleInfo = null; /* Basic module info   */
    private ModuleRequires moduleRequires = null; /* Module dependencies */
    private ModuleRepository moduleRepository = null; /* Module repository   */
    private ModuleChecksums moduleChecksums = null; /* The checksums for stuff */
    
    /* A map of unique artwork resource names to their resource objects */
    private HashMap<String, ModuleArtResource> moduleArtwork = null;

    /* A map of unique plugin names to their plugin objects */
    private HashMap<String, ModulePlugin> modulePlugins = null;
    
    /* A map of unique wfs names to their WFS objects */
    private HashMap<String, WFS> moduleWFSs = null;
    
    /** Default constructor */
    protected Module() {}
    
    /**
     * Returns the name of the module.
     * 
     * @return The module's name
     */
    public String getName() {
        return this.moduleInfo.getName();
    }
    
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
    public ModuleInfo getInfo() {
        return this.moduleInfo;
    }
    
    /**
     * Sets the basic information about a module, assumes the given argument
     * is not null.
     * <p>
     * @param moduleInfo The basic module information
     */
    public void setInfo(ModuleInfo moduleInfo) {
        this.moduleInfo = moduleInfo;
    }
    
    /**
     * Returns the module's dependencies.
     * <p>
     * @return The module's dependencies
     */
    public ModuleRequires getRequires() {
        return this.moduleRequires;
    }
    
    /**
     * Sets the module's dependencies, assumes the given argument is not null.
     * <p>
     * @param moduleRequires The module dependencies
     */
    public void setRequires(ModuleRequires moduleRequires) {
        this.moduleRequires = moduleRequires;
    }
    
    /**
     * Returns the module's repository information.
     * <p>
     * @return The module's repository information
     */
    public ModuleRepository getRepository() {
        return this.moduleRepository;
    }

    /**
     * Sets the module's repository information, assumes the argument is not
     * null.
     * <p>
     * @param moduleRepository The module's repository information
     */
    public void setRepository(ModuleRepository moduleRepository) {
        this.moduleRepository = moduleRepository;
    }
    
    /**
     * Returns the module's collection of artwork resource names
     * <p>
     * @return The module's collection of artwork
     */
    public Collection<String> getArtResources() {
        return this.moduleArtwork.keySet();
    }

    /**
     * Returns the module artwork resource given its relative path, or null if
     * it does not exist.
     * 
     * @return The module's artwork or null if it does not exist
     */
    public ModuleArtResource getArtResource(String path) {
        return this.moduleArtwork.get(path);
    }
    
    /**
     * Adds a module artwork resource given its relative path. If the resource
     * already exists, replaces the existing resource with the new resource.
     * 
     * @param path The path of the new resource
     * @param resource The new artwork resource
     */
    public void addArtResource(String path, ModuleArtResource resource) {
        this.moduleArtwork.put(path, resource);
    }
    
    /**
     * Removes an existing artwork resource given its relative path. If the
     * resource does not exist, this method does nothing.
     * 
     * @param path The path of the resource to remove
     */
    public void removeArtResource(String path) {
        this.moduleArtwork.remove(path);
    }

    /**
     * Returns a collection of WFS names contained within the module (without the
     * -wfs extension). If no WFSs exist within the module, this method returns
     * an empty map.
     *
     * @return A map of WFS entries within the module
     */
    public Collection<String> getWFSs() {
        return this.moduleWFSs.keySet();
    }
    
    /**
     * Returns a WFS object for the given name, or null if a WFS of that name
     * does not exist.
     * 
     * @param name The name of the WFS
     * @return The WFS object
     */
    public WFS getWFS(String name) {
        return this.moduleWFSs.get(name);
    }
    
    /**
     * Adds a WFS object given its name to the module. If an existing WFS with
     * the same name exist, this method replaces that WFS.
     * 
     * @param name The name of the WFS
     * @param wfs The new WFS
     */
    public void addWFS(String name, WFS wfs) {
        this.moduleWFSs.put(name, wfs);
    }
    
    /**
     * Removes an existing WFS object given its name. If the WFS does not exist,
     * this method does nothing.
     * 
     * @param name The name of the WFS
     */
    public void removeWFS(String name) {
        this.moduleWFSs.remove(name);
    }
    
    /**
     * Returns a collection of unique plugin names contained within the module.
     *
     * @return A collection of plugin names within the module
     */
    public Collection<String> getPlugins() {
        return this.modulePlugins.keySet();
    }

    /**
     * Returns a module's plugin given its name, null if the plugin does not
     * exists.
     * 
     * @param name The unique name of the plugin
     * @return The ModulePlugin object
     */
    public ModulePlugin getPlugin(String name) {
        return this.modulePlugins.get(name);
    }

    /**
     * Adds a new module plugin given its name and plugin object. If the plugin
     * already exist, replaces the existing object.
     * 
     * @param name The unique name of the plugin
     * @param plugin The plugin object
     */
    public void addPlugin(String name, ModulePlugin plugin) {
        this.modulePlugins.put(name, plugin);
    }
    
    /**
     * Removes an existing module plugin given its unique name. If the plugin
     * does not exist, this method does nothing.
     * 
     * @param name The unique name of the plugin
     */
    public void removePlugin(String name) {
        this.modulePlugins.remove(name);
    }
    
    /**
     * Returns a collection of checksums for the resources in the module.
     * 
     * @returns The collection of resource checksums
     */
    public ModuleChecksums getChecksums() {
        return this.moduleChecksums;
    }
    
    /**
     * Sets the collection of checksums for the resources in the module.
     * 
     * @param checksums The new collection of checksums
     */
    public ModuleChecksums setChecksums() {
        return this.moduleChecksums;
    }
    
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
        sb.append(this.getInfo().toString() + "\n");
        sb.append("Depends upon " + this.getRequires().toString() + "\n");
        sb.append("Repositories\n" + this.getRepository().toString() + "\n");
        sb.append("\nPlugins\n");
        Iterator<String> it = this.getPlugins().iterator();
        while (it.hasNext() == true) {
            ModulePlugin plugin = this.getPlugin(it.next());
            sb.append("\t" + plugin.toString() + "\n");
        }
        sb.append("\nArtwork resources\n");
        Iterator<String> it2 = this.getArtResources().iterator();
        while (it2.hasNext() == true) {
            ModuleArtResource art = this.getArtResource(it2.next());
            sb.append("\t" + art.getPathName() + "\n");
        }
        sb.append("\nWFS\n");
        Iterator<String> it3 = this.getWFSs().iterator();
        while (it3.hasNext() == true) {
            String name = it3.next();
            sb.append("\t" + name + "\n");
        }
        sb.append("---------------------------------------------------------------------------\n");
        return sb.toString();
    }
}
