/**
 * Project Wonderland
 *
 * Copyright (c) 2004-2009, Sun Microsystems, Inc., All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * Sun designates this particular file as subject to the "Classpath" 
 * exception as provided by Sun in the License file that accompanied 
 * this code.
 */
package org.jdesktop.wonderland.client.modules;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
import javax.xml.bind.JAXBException;
import org.jdesktop.wonderland.common.checksums.ChecksumList;
import org.jdesktop.wonderland.common.modules.ModuleArtList;
import org.jdesktop.wonderland.common.modules.ModuleInfo;
import org.jdesktop.wonderland.common.modules.ModuleList;
import org.jdesktop.wonderland.common.modules.ModulePluginList;
import org.jdesktop.wonderland.common.modules.ModuleRepository;

/**
 *
 * @author jordanslott
 */
public class ModuleUtils {
    /* Prefixes for the module and asset web services */
    private static final String MODULE_PREFIX = "wonderland-web-modules/modules/";
    private static final String ASSET_PREFIX = "wonderland-web-asset/asset/";
    private static final String CHECKSUM_PREFIX = "wonderland-web-checksums/checksums/";
    
    /* The error logger for this class */
    private static Logger logger = Logger.getLogger(ModuleUtils.class.getName());

    /**
     * Fetches the info for a particular module
     */
    public static ModuleInfo fetchModuleInfo(String serverURL, String moduleName) {
        Reader reader = null;
        try {
            URL url = new URL(new URL(serverURL), MODULE_PREFIX + moduleName + "/info");
            reader = fetchFromURL(url);
            return ModuleInfo.decode(reader);

        } catch (IOException excp) {
            /* Log an error and return null */
            logger.log(Level.WARNING, "[MODULES] FETCH MODULE INFO Failed", excp);
            return null;
        } catch (JAXBException excp) {
            /* Log an error and return null */
            logger.log(Level.WARNING, "[MODULES] FETCH MODULE INFO Failed", excp);
            return null;
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException excp) {
                /* Log an error and return null */
                logger.log(Level.WARNING, "[MODULES] FETCH MODULE INFO Failed", excp);
                return null;
            }
        }
    }
    
    /**
     * Asks the web server for a list of all modules. Returned is a ModuleList
     * object with the basic module information (ModuleInfo) objects for all
     * modules.
     * 
     * @return A list of modules
     */
    public static ModuleList fetchModuleList(String serverURL) {
        Reader reader = null;
        try {
            URL url = new URL(new URL(serverURL), CHECKSUM_PREFIX + "list/get/installed");
            reader = fetchFromURL(url);
            return ModuleList.decode(reader);

        } catch (IOException excp) {
            /* Log an error and return null */
            logger.log(Level.WARNING, "[MODULES] FETCH MODULE INFO Failed", excp);
            return null;
        } catch (JAXBException excp) {
            /* Log an error and return null */
            logger.log(Level.WARNING, "[MODULES] FETCH MODULE INFO Failed", excp);
            return null;
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException excp) {
                /* Log an error and return null */
                logger.log(Level.WARNING, "[MODULES] FETCH MODULE INFO Failed", excp);
                return null;
            }
        }
    }
    
    /**
     * Asks the web server for a list of all artwork assets in a given module.
     * Returned is a ModuleArtList object identifying each object.
     * 
     * @param moduleName The name of the module
     * @return A list of module art
     */
    public static ModuleArtList fetchModuleArtList(String serverURL, String moduleName) {
        Reader reader = null;
        try {
            URL url = new URL(new URL(serverURL), ASSET_PREFIX + moduleName + "/art/get");
            reader = fetchFromURL(url);
            return ModuleArtList.decode(reader);

        } catch (IOException excp) {
            /* Log an error and return null */
            logger.log(Level.WARNING, "[MODULES] FETCH MODULE ART Failed", excp);
            return null;
        } catch (JAXBException excp) {
            /* Log an error and return null */
            logger.log(Level.WARNING, "[MODULES] FETCH MODULE ART Failed", excp);
            return null;
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException excp) {
                /* Log an error and return null */
                logger.log(Level.WARNING, "[MODULES] FETCH MODULE ART Failed", excp);
                return null;
            }
        }
    }
    
    /**
     * Asks the web server for the module's repository information given the
     * unique name of the module, returns null if the module does not exist or
     * upon some general I/O error.
     *
     * @param serverURL The base web server URL
     * @param moduleName The unique name of a module
     * @return The repository information for a module
     */
    public static ModuleRepository fetchModuleRepositories(String serverURL, String moduleName) {
        Reader reader = null;
        try {
            URL url = new URL(new URL(serverURL), ASSET_PREFIX + moduleName + "/repository");
            reader = fetchFromURL(url);
            return ModuleRepository.decode(reader);

        } catch (IOException excp) {
            /* Log an error and return null */
            logger.log(Level.WARNING, "[MODULES] FETCH REPOSITORY LIST Failed", excp);
            return null;
        } catch (JAXBException excp) {
            /* Log an error and return null */
            logger.log(Level.WARNING, "[MODULES] FETCH REPOSITORY LIST Failed", excp);
            return null;
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException excp) {
                /* Log an error and return null */
                logger.log(Level.WARNING, "[MODULES] FETCH REPOSITORY LIST Failed", excp);
                return null;
            }
        }

    }
    
    /**
     * Common method to ensure proper keep alive management and GZip compression
     * @param url URL to be downloaded
     * @return 
     */
    private static Reader fetchFromURL(URL url) throws IOException {

        /* Open an HTTP connection to the Jersey RESTful service */

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        
        /* Allow receive GZipped content */
        connection.setRequestProperty("Accept-Encoding", "gzip");
        
//        System.out.println(url);
//        Map<String, List<String>> requestProperties = connection.getRequestProperties();
//        for (Map.Entry<String, List<String>> entry : requestProperties.entrySet()) {
//            String string = entry.getKey();
//            List<String> list = entry.getValue();
//
//            System.out.println("\tRequest propertirs: " + string + " = " + list);
//
//        }
//
//        Map<String, List<String>> headerFields = connection.getHeaderFields();
//        for (Map.Entry<String, List<String>> entry : headerFields.entrySet()) {
//            String string = entry.getKey();
//            List<String> list = entry.getValue();
//
//            System.out.println("\tHeader: " + string + " = " + list);
//
//        }
        final InputStream openStream = connection.getInputStream();
        final InputStreamReader inputStreamReader;
        
        if( "gzip".equals(connection.getHeaderField("Content-Encoding"))) {
            inputStreamReader = new InputStreamReader(new GZIPInputStream(openStream));
        } else {
            inputStreamReader = new InputStreamReader(openStream);
        }
        
        StringBuilder buffer = new StringBuilder(1024);
        try {
            BufferedReader br = new BufferedReader(inputStreamReader);

            final String lineSeparator = System.getProperty("line.separator");
            String line;
            while ((line = br.readLine()) != null) {
                buffer.append(line);
                buffer.append( lineSeparator);
            }

        } finally {

            inputStreamReader.close();
        }

        return new StringReader(buffer.toString());
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
        Reader reader = null;
        try {
            /* Open an HTTP connection to the Jersey RESTful service */
            URL url = new URL(new URL(serverURL), CHECKSUM_PREFIX + "modules/" + moduleName + "/checksums/get");
            reader = fetchFromURL(url);
            return ChecksumList.decode(reader);
        } catch (IOException excp) {
            /* Log an error and return null */
            logger.log(Level.WARNING, "[MODULES] FETCH CHECKSUMS Failed", excp);
            return null;
        } catch (JAXBException excp) {
            /* Log an error and return null */
            logger.log(Level.WARNING, "[MODULES] FETCH CHECKSUMS Failed", excp);
            return null;
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException excp) {
                /* Log an error and return null */
                logger.log(Level.WARNING, "[MODULES] FETCH CHECKSUMS Failed", excp);
                return null;
            }
        }
    }

    /**
     * Asks the web server for the module's checksum information given the
     * unique name of the module and a particular asset type, returns null if
     * the module does not exist or upon some general I/O error.
     * 
     * @param serverURL The base web server URL
     * @param moduleName The unique name of a module
     * @param assetType The name of the asset type (art, audio, client, etc.)
     * @return The checksum information for a module
     */
    public static ChecksumList fetchAssetChecksums(String serverURL,
            String moduleName, String assetType) {
        Reader reader = null;
        try {
            String uriPart = moduleName + "/checksums/get/" + assetType;
            URL url = new URL(new URL(serverURL), CHECKSUM_PREFIX + "modules/" + uriPart);
            reader = fetchFromURL(url);
            return ChecksumList.decode(reader);

        } catch (IOException excp) {
            /* Log an error and return null */
            logger.log(Level.WARNING, "[MODULES] FETCH CHECKSUMS Failed", excp);
            return null;
        } catch (JAXBException excp) {
            /* Log an error and return null */
            logger.log(Level.WARNING, "[MODULES] FETCH CHECKSUMS Failed", excp);
            return null;
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException excp) {
                /* Log an error and return null */
                logger.log(Level.WARNING, "[MODULES] FETCH CHECKSUMS Failed", excp);
                return null;
            }
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
        Reader reader = null;
        try {
            URL url = new URL(new URL(serverURL), ASSET_PREFIX + "jars/get");
            reader = fetchFromURL(url);
            return ModulePluginList.decode(reader, getServerFromURL(serverURL));

        } catch (IOException excp) {
            /* Log an error and return null */
            logger.log(Level.WARNING, "[MODULES] FETCH JARS Failed", excp);
            return null;
        } catch (JAXBException excp) {
            /* Log an error and return null */
            logger.log(Level.WARNING, "[MODULES] FETCH JARS Failed", excp);
            return null;
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException excp) {
                /* Log an error and return null */
                logger.log(Level.WARNING, "[MODULES] FETCH JARS Failed", excp);
                return null;
            }
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
    private static String getServerFromURL(String serverURL) throws MalformedURLException {
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
