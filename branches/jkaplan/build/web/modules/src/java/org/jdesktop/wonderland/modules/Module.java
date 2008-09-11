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

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import org.jdesktop.wonderland.client.modules.ModuleChecksums;
import org.jdesktop.wonderland.wfs.WFS;

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
    public static final String MODULE_CHECKSUMS  = MODULE_ART + "/checksums.xml";
    public static final String MODULE_WFS        = "wfs";
    public static final String MODULE_PLUGINS    = "plugins";
    
    private ModuleInfo       moduleInfo       = null; /* Basic module info   */
    private ModuleRequires   moduleRequires   = null; /* Module dependencies */
    private ModuleRepository moduleRepository = null; /* Module repository   */
    
    /* A map of unique artwork resource names to their resource objects */
    private HashMap<String, ModuleArtResource> moduleArtwork = null;
    
    /* A map of unique WFS names to their WFS objects */
    private HashMap<String, WFS> moduleWFS = null;
    
    /* A map of plugin names to the plugin object */
    private HashMap<String, ModulePlugin> modulePlugins = null;
    
    /* A single table of checkums for all of the resources */
    private ModuleChecksums checksums = null;
    
    /** Default constructor */
    protected Module() {}
    
    /**
     * Opens the module by reading its contents. This method is overridden by
     * subclasses.
     */
    protected abstract void open();
    
    /**
     * Returns the basic information about a module: its name and version.
     * <p>
     * @return The basic module information
     */
    public ModuleInfo getModuleInfo() {
        return this.moduleInfo;
    }
    
    /**
     * Sets the basic information about a module, assumes the given argument
     * is not null.
     * <p>
     * @param moduleInfo The basic module information
     */
    public void setModuleInfo(ModuleInfo moduleInfo) {
        this.moduleInfo = moduleInfo;
    }
    
    /**
     * Returns the module's dependencies.
     * <p>
     * @return The module's dependencies
     */
    public ModuleRequires getModuleRequires() {
        return this.moduleRequires;
    }
    
    /**
     * Sets the module's dependencies, assumes the given argument is not null.
     * <p>
     * @param moduleRequires The module dependencies
     */
    public void setModuleRequires(ModuleRequires moduleRequires) {
        this.moduleRequires = moduleRequires;
    }
    
    /**
     * Returns the module's repository information.
     * <p>
     * @return The module's repository information
     */
    public ModuleRepository getModuleRepository() {
        return this.moduleRepository;
    }
    
    /**
     * Sets the module's repository information, assumes the argument is not
     * null.
     * <p>
     * @param moduleRepository The module's repository information
     */
    public void setModuleRepository(ModuleRepository moduleRepository) {
        this.moduleRepository = moduleRepository;
    }
    
    /**
     * Sets the module's collection of artwork resources, assumes the argument
     * is not null.
     * <p>
     * @param moduleArtwork The module's artwork
     */
    public void setModuleArtwork(HashMap<String, ModuleArtResource> moduleArtwork) {
        this.moduleArtwork = moduleArtwork;
    }
    
    /**
     * Returns the module's collection of artwork resources.
     * <p>
     * @return The module's collection of artwork
     */
    public HashMap<String, ModuleArtResource> getModuleArtwork() {
        return this.moduleArtwork;
    }

    /**
     * Returns the module artwork resource given its relative path, or null if
     * it does not exist
     * 
     * @return The module's artwork or null if it does not exist
     */
    public ModuleArtResource getModuleArtResource(String path) {
        return this.moduleArtwork.get(path);
    }

    /**
     * Returns a map of WFS objects for all WFSs contained within the module.
     * The key for each entry of the map is the name of the WFS (without the
     * -wfs extension) and the value is its WFS object. If no WFSs exist within
     * the module, this method returns an empty map.
     *
     * @return A map of WFS entries within the module
     */
    public Map<String, WFS> getModuleWFSs() {
        return this.moduleWFS;
    }
    
    /**
     * Sets the map of WFS objects for. This method assumes that the argument
     * is not null; for no WFS objects, pass an empty set.
     * 
     * @param wfs A map of WFS entries within the module
     */
    public void setModuleWFSs(HashMap<String, WFS> wfs) {
        this.moduleWFS = wfs;
    }

    /**
     * Returns a map of ModulePlugin objects for all plugins contained within
     * the module. The key for each entry of the map is the name of the plugin
     * and the value is its ModulePlugin object. If no plugins exist within
     * the module, this method returns an empty map.
     *
     * @return A map of plugin entries within the module
     */
    public Map<String, ModulePlugin> getModulePlugins() {
        return this.modulePlugins;
    }
    
    /**
     * Sets the map of plugin objects for this module. This method assumes that
     * the argument is not null; for no plugin objects, pass an empty set.
     * 
     * @param plugins A map of WFS entries within the module
     */
    public void setModulePlugins(HashMap<String, ModulePlugin> plugins) {
        this.modulePlugins = plugins;
    }
    
    /**
     * Sets the list of checksums for the resources in the module.
     * 
     * @param checksums The collection of checksums for the resources
     */
    public void setModuleChecksums(ModuleChecksums checksums) {
        this.checksums = checksums;
    }
    
    /**
     * Returns a collection of checksums for the resources in the module.
     * 
     * @returns The collection of resource checksums
     */
    public ModuleChecksums getModuleChecksums() {
        return this.checksums;
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
        return this.getModuleInfo().toString() + this.getModuleRequires().toString() +
            this.getModuleRepository().toString()/* + this.getModuleArtwork().toString()*/;
    }
}
