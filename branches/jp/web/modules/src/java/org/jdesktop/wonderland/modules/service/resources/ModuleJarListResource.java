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

import java.io.StringWriter;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.ProduceMime;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import org.jdesktop.wonderland.client.modules.ModulePluginList;
import org.jdesktop.wonderland.modules.ModulePlugin;
import org.jdesktop.wonderland.modules.service.InstalledModule;
import org.jdesktop.wonderland.modules.service.ModuleManager;
import org.jdesktop.wonderland.modules.service.ModuleManager.State;

/**
 * The ModuleJarListResource class is a Jersey RESTful service that returns a
 * list of all plugins that are needed on the client.
 * 
 * @author Jordan Slott <jslott@dev.java.net>
 */
@Path(value="/plugins/jars")
public class ModuleJarListResource {
    
    /**
     * Returns a list of URIs that describe jar files needed for the client.
     * This includes jar files of type "client" and "common". An XML encoding
     * of the ModuleJarList class is returned.
     */
    @GET
    @ProduceMime("text/plain")
    public Response getModuleClientAndCommonJars() {
        Logger logger = ModuleManager.getLogger();
        Collection<String> jarURIs = new LinkedList<String>();
        ModuleManager manager = ModuleManager.getModuleManager();
        
        /* Fetch the entire list of installed modules, loop through each */
        Iterator<String> it = manager.getModules(State.INSTALLED).iterator();
        while (it.hasNext() == true) {
            String moduleName = it.next();
            InstalledModule im = (InstalledModule) manager.getModule(moduleName, State.INSTALLED);
            if (im == null) {
                /* Log an error and return null */
                logger.warning("[MODULES] REST GET PLUGINS Unable to locate module " + moduleName);
                continue;
            }
        
            /* Fetch the list of plugins */
            Iterator<String> it2 = im.getModulePlugins().iterator();
            while (it2.hasNext() == true) {
                /*
                 * For each plugin, fetch the list of client-side and common jar
                 * names
                 */
                String pluginName = it2.next();
                ModulePlugin plugin = im.getModulePlugin(pluginName);
                if (plugin == null) {
                    logger.warning("[MODULES] REST GET PLUGINS Unable to fetch " +
                            "plugin " + pluginName + " in " + moduleName);
                    continue;
                }

                Collection<String> clientJars = plugin.getClientJars();
                jarURIs.addAll(this.getPluginJarURIs(moduleName, pluginName, clientJars, ModulePlugin.CLIENT_JAR));
                Collection<String> commonJars = plugin.getCommonsJars();
                jarURIs.addAll(this.getPluginJarURIs(moduleName, pluginName, commonJars, ModulePlugin.COMMON_JAR));
            }
        }
        
        /* Otherwise, return a response with the input stream */
        ModulePluginList mpl = new ModulePluginList();
        mpl.setJarURIs(jarURIs.toArray(new String[] {}));

        /* Write the XML encoding to a writer and return it2 */
        StringWriter sw = new StringWriter();
        try {
            mpl.encode(sw);
            ResponseBuilder rb = Response.ok(sw.toString());
            return rb.build();
        } catch (javax.xml.bind.JAXBException excp) {
            /* Log an error and return an error response */
            logger.log(Level.WARNING, "[MODULES] REST GET PLUGINS Unable to encode", excp);
            ResponseBuilder rb = Response.status(Response.Status.BAD_REQUEST);
            return rb.build();
        }
    }
    
    /**
     * Takes a collection of module names, plugin names, and jar file names and
     * returns a collection of string URIs for each.
     */
    private Collection<String> getPluginJarURIs(String moduleName, String pluginName, Collection<String> jarNames, String type) {
        Collection<String> uris = new LinkedList<String>();
        Iterator<String> it = jarNames.iterator();
        while (it.hasNext() == true) {
            String jarName = it.next();
            uris.add(new String("wlj://" + moduleName + "/" + pluginName + "/" + type + jarName));
        }
        return uris;
    }
}
