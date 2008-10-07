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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;
import javax.xml.bind.JAXBException;
import org.jdesktop.wonderland.modules.Module;
import org.jdesktop.wonderland.modules.ModuleArtResource;
import org.jdesktop.wonderland.modules.ModulePlugin;

/**
 * The ModuleJarWriter class streams a Module to a JAR archive file. For
 * certain module resources (artwork, plugin JARs), this class relies upon
 * listeners to provide the input streams for each resources
 * 
 * @author Jordan Slott <jslott@dev.java.net>
 */
public class ModuleJarWriter {

    /* Listener that returns an input stream for artwork resources */
    private ModuleResourceListener resourceListener = null;
    
    /* Listener that returns an input stream for plugin JARs */
    private ModulePluginListener pluginListener = null;
    
    /** Default constructor */
    public ModuleJarWriter() {
    }
    
    /**
     * Sets the listener that returns an input stream for art resources.
     * 
     * @param resourceListener The listener for art resources
     */
    public void setResourceListener(ModuleResourceListener resourceListener) {
        this.resourceListener = resourceListener;
    }
    
    /**
     * Sets the listener that returns an input stram for a plugin JAR.
     * 
     * @param pluginListener The listener for plugin JARs
     */
    public void setPluginListener(ModulePluginListener pluginListener) {
        this.pluginListener = pluginListener;
    }
    
        /**
     * Stream this module out to an archive file given its output stream
     * 
     * @param os The output stream to write the module to
     */
    public void writeToJar(Module module, OutputStream os) throws IOException, JAXBException {
        /* Make an output stream for the jar file */
        JarOutputStream jos = new JarOutputStream(os);
        
        /* Write the basic module info, module.xml file */
        if (module.getModuleInfo() != null) {
            jos.putNextEntry(new ZipEntry(Module.MODULE_INFO));
            module.getModuleInfo().encode(jos);
        }
        else {
            throw new IOException("Module info may not be null");
        }
        
        /* Write the module repository info, repository.xml file */
        if (module.getModuleRepository() != null) {
            jos.putNextEntry(new ZipEntry(Module.MODULE_REPOSITORY));
            module.getModuleRepository().encode(jos);
        }
        
        /* Write the module requirements, requires.xml file */
        if (module.getModuleRequires() != null) {
            jos.putNextEntry(new ZipEntry(Module.MODULE_REQUIRES));
            module.getModuleRequires().encode(jos);
        }
 
        /* Write the module checksums, checksums.xml file */
        if (module.getModuleChecksums() != null) {
            jos.putNextEntry(new ZipEntry(Module.MODULE_CHECKSUMS));
            module.getModuleChecksums().encode(jos);
        }

        /* Put an entry for the art/ directory */
        jos.putNextEntry(new ZipEntry(Module.MODULE_ART + "/"));
        
        /*
         * Write out the module artwork resources. Loop through each of the
         * resources, ask the listener for the input stream for each resource
         * and write out to the jar output stream.
         */
        Iterator<String> resourceIterator = module.getModuleArtResources().iterator();
        while (resourceIterator.hasNext() == true) {
            /* Fetch the resource and an input stream to fetch the resource */
            String resourceName = resourceIterator.next();
            ModuleArtResource resource = module.getModuleArtResource(resourceName);
            InputStream is = this.resourceListener.getInputStreamForResource(resourceName);
            
            /* Does every directory need an entry? XXX */
            
            /* Write the entry to the JAR file */
            jos.putNextEntry(new ZipEntry(Module.MODULE_ART + "/" + resourceName));
            this.write(is, jos);
        }
        
        /* Put an entry for the plugin/ directory */
        jos.putNextEntry(new ZipEntry(Module.MODULE_PLUGINS + "/"));
        
        /*
         * Write out each of the plugins. Loop through each of the plugins and
         * ask the listener to write out each jar
         */
        Iterator<String> pluginIterator = module.getModulePlugins().iterator();
        while (pluginIterator.hasNext() == true) {
            /* Write an entry for the directory */
            String pluginName = pluginIterator.next();
            ModulePlugin plugin = module.getModulePlugin(pluginName);
            jos.putNextEntry(new ZipEntry(Module.MODULE_PLUGINS + "/" + pluginName + "/"));
            
            /* Create entries for the client/, server/, and common/ directories */
            jos.putNextEntry(new ZipEntry(Module.MODULE_PLUGINS + "/" + pluginName + "/" + ModulePlugin.CLIENT_JAR));
            jos.putNextEntry(new ZipEntry(Module.MODULE_PLUGINS + "/" + pluginName + "/" + ModulePlugin.SERVER_JAR));
            jos.putNextEntry(new ZipEntry(Module.MODULE_PLUGINS + "/" + pluginName + "/" + ModulePlugin.COMMON_JAR));

            /* Fetch the list of client, server, and common jar file names */
            Collection<String> clientJARs = plugin.getClientJars();
            Collection<String> commonJARs = plugin.getCommonsJars();
            Collection<String> serverJARs = plugin.getServerJars();
            
            /* Loop through all of the client JARs and write out */
            Iterator<String> clientJARNames = clientJARs.iterator();
            while (clientJARNames.hasNext() == true) {
                /* Fetch the jar name and its input stream, write it out */
                String clientName = clientJARNames.next();
                InputStream is = this.pluginListener.getInputStreamForPluginJAR(clientName, ModulePlugin.CLIENT_JAR);
                this.write(is, os);
            }
            
            /* Loop through all of the server JARs and write out */
            Iterator<String> serverJARNames = serverJARs.iterator();
            while (serverJARNames.hasNext() == true) {
                /* Fetch the jar name and its input stream, write it out */
                String serverName = serverJARNames.next();
                InputStream is = this.pluginListener.getInputStreamForPluginJAR(serverName, ModulePlugin.SERVER_JAR);
                this.write(is, os);
            }
                        /* Loop through all of the client JARs and write out */
            Iterator<String> commonJARNames = commonJARs.iterator();
            while (commonJARNames.hasNext() == true) {
                /* Fetch the jar name and its input stream, write it out */
                String commonName = commonJARNames.next();
                InputStream is = this.pluginListener.getInputStreamForPluginJAR(commonName, ModulePlugin.COMMON_JAR);
                this.write(is, os);
            }
        }
        
        /*
         * Write out each of the WFSs. Loop through the map, and write the
         * directory structure to the JAR file
         */
//        Map<String, WFS> wfsMap = module.getModuleWFSs();
//        Iterator<String> it = wfsMap.keySet().iterator();
//        while (it.hasNext() == true) {
//            String wfsName = it.next();
//            WFS wfs = wfsMap.get(wfsName);
//            wfs.writeTo(jos);
//        }
    }
    
    /**
     * Writes all of the data from the given input stream to the given output
     * stream.
     * 
     * @param is The input stream to read data from
     * @param os The output stream to write data to
     * @throw IOException Upon general I/O error
     */
    private void write(InputStream is, OutputStream os) throws IOException {
        int cc = -1;
        byte buf[] = new byte[4 * 1028];
        while ((cc = is.read(buf)) != -1) {
            os.write(buf, 0, cc);
        }
    }
}
