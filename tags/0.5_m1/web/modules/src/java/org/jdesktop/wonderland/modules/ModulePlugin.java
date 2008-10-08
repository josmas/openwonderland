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

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;

/**
 * A ModulePlugin represents a plugin-module that extends the functionality of
 * the Wonderland server and/or client. A module consists of a collection of
 * jar files that are either included in the server classpath, downloaded into
 * the client Java VM, or both.
 * <p>
 * This class enumerates the unique locations within the module for each jar
 * file. (The Wonderland server may then stream the client-side jar files over
 * an http connection knowing their location within the server installation).
 *
 * @author Jordan Slott <jslott@dev.java.net>
 */
public class ModulePlugin {
    /* The unique name of the plugin */
    private String name = null;
    
    /* The types of plugins */
    public static final String CLIENT_JAR = "client/";
    public static final String SERVER_JAR = "server/";
    public static final String COMMON_JAR = "common/";
    
    /* Collections of the names of server, client, and common jars */
    private LinkedList<String> serverJars = new LinkedList<String>();
    private LinkedList<String> clientJars = new LinkedList<String>();
    private LinkedList<String> commonJars = new LinkedList<String>();
    
    /** Default constructor, takes collections of JARs */
    public ModulePlugin(String name, String[] client, String[] server, String[] common) {
        this.name = name;
        this.serverJars.addAll(Arrays.asList(server));
        this.clientJars.addAll(Arrays.asList(client));
        this.commonJars.addAll(Arrays.asList(common));
    }
    
    /**
     * Returns the unique name of the plugin.
     * 
     * @return The plugin name
     */
    public String getName() {
        return this.name;
    }
    
    /**
     * Returns a collection of server-side component paths within the module.
     * <p>
     * @return A collection of path names to server-side components
     */
    public Collection<String> getServerJars() {
        return this.serverJars;
    }
    
    /**
     * Returns a collection of client-side component paths within the module.
     * <p>
     * @return A collection of path names to client-side components
     */
    public Collection<String> getClientJars() {
        return this.clientJars;
    }
    
    /**
     * Returns a collection of components common to both the client and server
     * 
     * @return A collection of path names to common components
     */
    public Collection<String> getCommonsJars() {
        return this.commonJars;
    }
    
    /**
     * Returns a string representation of the plugin
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.getName() + " ");
        sb.append(this.getClientJars().toString());
        sb.append(this.getCommonsJars().toString());
        sb.append(this.getServerJars().toString());
        
        return sb.toString();
    }
}
