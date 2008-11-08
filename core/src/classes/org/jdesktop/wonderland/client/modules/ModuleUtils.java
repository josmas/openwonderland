/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.client.modules;

import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author jordanslott
 */
public class ModuleUtils {
    /* Prefixes for the module and asset web services */
    private static final String MODULE_PREFIX = "wonderland-web-modules/modules/";
    private static final String ASSET_PREFIX = "wonderland-web-asset/asset/";
    
    /* The error logger for this class */
    private static Logger logger = Logger.getLogger(ModuleUtils.class.getName());
    
    /**
     * Asks the web server for the module's basic information given its name,
     * returns null if the module does not exist or upon some general I/O
     * error.
     * 
     * @param serverURL The base web server URL
     * @param moduleName The unique name of a module
     * @return The identity information for a module (e.g. version)
     */
//    public static ModuleIdentity fetchModuleIdentity(String serverURL, String moduleName) {
//        try {
//            /* Open an HTTP connection to the Jersey RESTful service */
//            URL url = new URL(new URL(serverURL), MODULE_PREFIX + moduleName + "/info");
//            return ModuleIdentity.decode(new InputStreamReader(url.openStream()));
//        } catch (java.lang.Exception excp) {
//            /* Log an error and return null */
//            logger.log(Level.WARNING, "[MODULES] FETCH MODULE INFO Failed", excp);
//            return null;
//        }
//    }
    
    /**
     * Asks the web server for the module's repository information given the
     * unique name of the module, returns null if the module does not exist or
     * upon some general I/O error.
     *
     * @param serverURL The base web server URL
     * @param moduleName The unique name of a module
     * @return The repository information for a module
     */
    public static RepositoryList fetchModuleRepositoryList(String serverURL, String moduleName) {
        try {
            /* Open an HTTP connection to the Jersey RESTful service */
            URL url = new URL(new URL(serverURL), ASSET_PREFIX + moduleName + "/repository");
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
     * @param serverURL The base web server URL
     * @param moduleName The unique name of a module
     * @return The checksum information for a module
     */
    public static ChecksumList fetchModuleChecksums(String serverURL, String moduleName) {
        try {
            /* Open an HTTP connection to the Jersey RESTful service */
            URL url = new URL(new URL(serverURL), ASSET_PREFIX + moduleName + "/checksums/get");
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
    public static ModulePluginList fetchPluginJars(String serverURL) {
        try {
            /* Open an HTTP connection to the Jersey RESTful service */
            URL url = new URL(new URL(serverURL), ASSET_PREFIX + "jars/get");
            Reader r = new InputStreamReader(url.openStream());
            return ModulePluginList.decode(r, getServerFromURL(serverURL));
        } catch (java.lang.Exception excp) {
            /* Log an error and return null */
            logger.log(Level.WARNING, "[MODULES] FETCH JARS Failed", excp);
            return null;
        }
    }
    
    /**
     * Given a base URL of the server (e.g. http://localhost:8080) returns
     * the server name and port as a string (e.g. localhost:8080). Returns null
     * if the host name is not present.
     * 
     * @return <server name>:<port>
     * @throw MalformedURLException If the given string URL is invalid
     */
    public static String getServerFromURL(String serverURL) throws MalformedURLException {
        URL url = new URL(serverURL);
        String host = url.getHost();
        int port = url.getPort();
        
        if (host == null) {
            return null;
        }
        else if (port == -1) {
            return host;
        }
        return host + ":" + port;
    }
}
