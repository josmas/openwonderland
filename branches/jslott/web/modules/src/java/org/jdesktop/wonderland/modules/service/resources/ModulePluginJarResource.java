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

package org.jdesktop.wonderland.modules.service.resources;

import java.io.InputStream;
import java.io.StringWriter;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.ProduceMime;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import org.jdesktop.wonderland.client.modules.ModulePluginList;
import org.jdesktop.wonderland.modules.ModulePlugin;
import org.jdesktop.wonderland.modules.service.InstalledModule;
import org.jdesktop.wonderland.modules.service.ModuleManager;
import org.jdesktop.wonderland.modules.service.ModuleManager.State;

/**
 * The ModulePluginJarResource class is a Jersey RESTful service that returns a
 * JAR file that belongs to a plugin. Depending upon whether it is a client,
 * server, or common jar, the getModuleClientPluginJar(), getModuleServerPluginJar(),
 * or getModuleCommonPluginJar() method handles the HTTP RESTful request.
 * 
 * @author Jordan Slott <jslott@dev.java.net>
 */
@Path(value="/{modulename}/plugins/{pluginname}", limited=true)
public class ModulePluginJarResource {
    
    /**
     * Returns an input stream for the requested client-side JAR file, given
     * the name of the plugin, and the name of the jar file. The format of the
     * URI is:
     * <p>
     * /module/{modulename}/plugins/{pluginame}/client/{jarname}
     * <p>
     * where {modulename} is the name of the module, {pluginame} is the name
     * of the plugin, and {jarname} is the name of the jar file. All spaces in
     * the module, plugin, and jar file names must be encoded to %20. Returns
     * BAD_REQUEST to the HTTP connection if the module name, plugin name, or
     * JAR file name is invalid or if there was an error fetching the JAR file.
     * 
     * @param moduleName The unique name of the module
     * @param pluginName The unique name of the plugin
     * @param jarName The name of the JAR file (including the .jar extension)
     */
    @GET @Path(value="/client/{jarname}")
    public Response getModuleClientPluginJar(@PathParam("modulename") String moduleName,
            @PathParam("pluginname") String pluginName, @PathParam("jarname") String jarName) {
        
        /* Fetch the input stream for the jar, return error if null */
        InputStream is = this.getModulePlugin(moduleName, pluginName, jarName, ModulePlugin.CLIENT_JAR);
        if (is == null) {
            /* Log an error and return an error response */
            ModuleManager.getLogger().warning("[MODULES] REST GET PLUGIN " +
                    " Unable to locate plugin " + moduleName + " " + pluginName +
                    " " + jarName);
            ResponseBuilder rb = Response.status(Response.Status.BAD_REQUEST);
            return rb.build();
        }
        
        /* Otherwise, return a response with the input stream */
        ResponseBuilder rb = Response.ok(is);
        return rb.build();
    }

    /**
     * Returns an input stream for the requested common JAR file, given
     * the name of the plugin, and the name of the jar file. The format of the
     * URI is:
     * <p>
     * /module/{modulename}/plugins/{pluginame}/common/{jarname}
     * <p>
     * where {modulename} is the name of the module, {pluginame} is the name
     * of the plugin, and {jarname} is the name of the jar file. All spaces in
     * the module, plugin, and jar file names must be encoded to %20. Returns
     * BAD_REQUEST to the HTTP connection if the module name, plugin name, or
     * JAR file name is invalid or if there was an error fetching the JAR file.
     * 
     * @param moduleName The unique name of the module
     * @param pluginName The unique name of the plugin
     * @param jarName The name of the JAR file (including the .jar extension)
     */
    @GET @Path(value="/common/{jarname}")
    public Response getModuleCommonPluginJar(@PathParam("modulename") String moduleName,
            @PathParam("pluginname") String pluginName, @PathParam("jarname") String jarName) {
        
        /* Fetch the input stream for the jar, return error if null */
        InputStream is = this.getModulePlugin(moduleName, pluginName, jarName, ModulePlugin.COMMON_JAR);
        if (is == null) {
            /* Log an error and return an error response */
            ModuleManager.getLogger().warning("[MODULES] REST GET PLUGIN " +
                    " Unable to locate plugin " + moduleName + " " + pluginName +
                    " " + jarName);
            ResponseBuilder rb = Response.status(Response.Status.BAD_REQUEST);
            return rb.build();
        }
        
        /* Otherwise, return a response with the input stream */
        ResponseBuilder rb = Response.ok(is);
        return rb.build();
    }
 
    /**
     * Returns an input stream for the requested sever-side JAR file, given
     * the name of the plugin, and the name of the jar file. The format of the
     * URI is:
     * <p>
     * /module/{modulename}/plugins/{pluginame}/server/{jarname}
     * <p>
     * where {modulename} is the name of the module, {pluginame} is the name
     * of the plugin, and {jarname} is the name of the jar file. All spaces in
     * the module, plugin, and jar file names must be encoded to %20. Returns
     * BAD_REQUEST to the HTTP connection if the module name, plugin name, or
     * JAR file name is invalid or if there was an error fetching the JAR file.
     * 
     * @param moduleName The unique name of the module
     * @param pluginName The unique name of the plugin
     * @param jarName The name of the JAR file (including the .jar extension)
     */
    @GET @Path(value="/server/{jarname}")
    public Response getModuleServerPluginJar(@PathParam("modulename") String moduleName,
            @PathParam("pluginname") String pluginName, @PathParam("jarname") String jarName) {
        
        /* Fetch the input stream for the jar, return error if null */
        InputStream is = this.getModulePlugin(moduleName, pluginName, jarName, ModulePlugin.SERVER_JAR);
        if (is == null) {
            /* Log an error and return an error response */
            ModuleManager.getLogger().warning("[MODULES] REST GET PLUGIN " +
                    " Unable to locate plugin " + moduleName + " " + pluginName +
                    " " + jarName);
            ResponseBuilder rb = Response.status(Response.Status.BAD_REQUEST);
            return rb.build();
        }
        
        /* Otherwise, return a response with the input stream */
        ResponseBuilder rb = Response.ok(is);
        return rb.build();
    }
    
    /**
     * Returns an input stream given the module name, plugin name, and jar file
     * name, or null if the module, plugin, or jar does not exist. The type
     * string is either CLIENT, SERVER, or COMMON.
     */
    private InputStream getModulePlugin(String moduleName, String pluginName, String jarName, String type) {
        /* Fetch thhe error logger for use in this method */
        Logger logger = ModuleManager.getLogger();
        
        /* Fetch the module from the module manager */
        ModuleManager mm = ModuleManager.getModuleManager();
        InstalledModule im = (InstalledModule)mm.getModule(moduleName, State.INSTALLED);
        if (im == null) {
            /* Log an error and return null */
            logger.warning("[MODULES] REST GET PLUGIN Unable to locate module " + moduleName);
            return null;
        }
        
        /* Fetch the input stream */
        return im.getInputStreamForPlugin(pluginName, jarName, type);
    }
}
