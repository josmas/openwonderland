/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.client.modules;

import java.io.InputStreamReader;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author jordanslott
 */
public class ModuleUtils {
    /* The base URL of the web server */
    private static final String BASE_URL = "http://localhost:8080/";
    
    /* The error logger for this class */
    private static Logger logger = Logger.getLogger(ModuleUtils.class.getName());
    
    /**
     * Asks the web server for the module's basic information given its name,
     * returns null if the module does not exist or upon some general I/O
     * error.
     * 
     * @param uniqueName The unique name of a module
     * @return The identity information for a module (e.g. version)
     */
    public static ModuleIdentity fetchModuleIdentity(String uniqueName) {
        try {
            /* Open an HTTP connection to the Jersey RESTful service */
            String base = BASE_URL + "wonderland-web-modules/modules/";
            URL url = new URL(base + uniqueName + "/info");
            return ModuleIdentity.decode(new InputStreamReader(url.openStream()));
        } catch (java.lang.Exception excp) {
            /* Log an error and return null */
            logger.log(Level.WARNING, "[MODULES] FETCH MODULE INFO Failed", excp);
            return null;
        }
    }
    
    /**
     * Asks the web server for the module's repository information given the
     * unique name of the module, returns null if the module does not exist or
     * upon some general I/O error.
     *
     * @param uniqueName The unique name of a module
     * @return The repository information for a module
     */
    public static RepositoryList fetchModuleRepositoryList(String uniqueName) {
        try {
            /* Open an HTTP connection to the Jersey RESTful service */
            String base = BASE_URL + "wonderland-web-asset/asset/";
            URL url = new URL(base + uniqueName + "/repository");
            logger.info("[MODULE] Fetching Repository list from " + url.toString());
            return RepositoryList.decode(new InputStreamReader(url.openStream()));
        } catch (java.lang.Exception excp) {
            /* Log an error and return null */
            logger.log(Level.WARNING, "[MODULES] FETCH REPOSITORY LIST Failed", excp);
            return null;
        }
    }
    
    /**
     * Asks the web server for the module's checksum information given the
     * unique name of the module, returns null if the module does not exist or
     * upon some general I/O error.
     * 
     * @param uniqueName The unique name of a module
     * @return The checksum information for a module
     */
    public static ChecksumList fetchModuleChecksums(String uniqueName) {
        try {
            /* Open an HTTP connection to the Jersey RESTful service */
            String base = BASE_URL + "wonderland-web-asset/asset/";
            URL url = new URL(base + uniqueName + "/checksums/get");
            logger.info("[MODULES] Fetch modules from " + url.toString());
            return ChecksumList.decode(new InputStreamReader(url.openStream()));
        } catch (java.lang.Exception excp) {
            /* Log an error and return null */
            logger.log(Level.WARNING, "[MODULES] FETCH CHECKSUMS Failed", excp);
            return null;
        }
    }
    
    /**
     * Asks the web server for the module's plugin jar information that is
     * necessary for the client. This include the "client" and "common" jar
     * files. Returns a ModulePluginList object upon succes, null upon error.
     * 
     * @return The list of client and common plugin jars in all modules.
     */
    public static ModulePluginList fetchPluginJars() {
        try {
            /* Open an HTTP connection to the Jersey RESTful service */
            URL url = new URL(BASE_URL + "wonderland-web-asset/asset/jars/get");
            return ModulePluginList.decode(new InputStreamReader(url.openStream()));
        } catch (java.lang.Exception excp) {
            /* Log an error and return null */
            logger.log(Level.WARNING, "[MODULES] FETCH JARS Failed", excp);
            return null;
        }
    }
}
