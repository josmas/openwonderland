/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.client.modules;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

/**
 *
 * @author jordanslott
 */
public class ModuleUtils {
    /* The base URL of the web server */
    private static final String BASE_URL = "http://localhost:8080/wonderland-web-modules/modules/";
    
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
            URL url = new URL(BASE_URL + uniqueName + "/info");
            return ModuleIdentity.decode(new InputStreamReader(url.openStream()));
        } catch (java.lang.Exception excp) {
            System.out.println(excp.toString());
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
            URL url = new URL(BASE_URL + uniqueName + "/repository");
            System.out.println("url = " + url.toExternalForm());
            return RepositoryList.decode(new InputStreamReader(url.openStream()));
        } catch (java.lang.Exception excp) {
            // log an error
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
            URL url = new URL(BASE_URL + uniqueName + "/checksums");
            return ChecksumList.decode(new InputStreamReader(url.openStream()));
        } catch (java.lang.Exception excp) {
            // log an error
            System.out.println(excp.toString());
            return null;
        }
    }
}
