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
package org.jdesktop.wonderland.webserver.launcher;

import org.jdesktop.wonderland.utils.RunUtil;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import org.jdesktop.wonderland.utils.SystemPropertyUtil;

/**
 * Main entry point for Wonderland embedded web server.  This class extracts
 * all the .jar files required to run the web server, and then instantiates
 * the server in a new classloader with all those jars loaded.
 * 
 * @author jkaplan
 */
public class WebServerLauncher {
    // properties
    public static final String WEBSERVER_PORT_PROP = "wonderland.webserver.port";
    
    private static final Logger logger = 
            Logger.getLogger(WebServerLauncher.class.getName());
    
    public static void main(String[] args) {
        // before we do anything, ready the default properties
        try {
            InputStream is = WebServerLauncher.class.getResourceAsStream("/web-default.properties");
            Properties props = new Properties();
            props.load(is);
            
            // copy properties into System properties only if they don't already
            // exist.  The means that people can override the defaults by
            // passing an argument like "-Dmy.prop=xxxx" at the command line.
            for (Object prop : props.keySet()) {
                if (!System.getProperties().containsKey(prop)) {
                    System.setProperty((String) prop, 
                                       props.getProperty((String) prop));
                }
            }
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Error loading default properties", ex);
            System.exit(-1);
        }
        
        // now load in the logging configuration
        if (System.getProperty("java.util.logging.config.file") == null &&
                System.getProperty("java.util.logging.config.class") == null) 
        { 
            try {
                InputStream logConfig;
           
                // substitute the wonderland log directory for the token
                // %w in the FileHandler path
                Properties p = new Properties();
                p.load(WebServerLauncher.class.getResourceAsStream("/web-logging.properties"));
                String filePattern = p.getProperty("java.util.logging.FileHandler.pattern");
                if (filePattern != null && filePattern.contains("%w")) {
                    String logDir = SystemPropertyUtil.getProperty("wonderland.log.dir");
                    p.setProperty("java.util.logging.FileHandler.pattern",
                                  filePattern.replaceAll("%w", logDir));
                    File tmpLog = File.createTempFile("wonderlandlog", ".properties");
                    p.store(new FileOutputStream(tmpLog), null);
                    
                    logConfig = new FileInputStream(tmpLog);
                } else {
                    // nothing to substitute, just read the file directly
                    logConfig = WebServerLauncher.class.getResourceAsStream("/web-logging.properties");
                }
            
                LogManager.getLogManager().readConfiguration(logConfig);
            } catch (IOException ioe) {
                logger.log(Level.WARNING, "Error setting up log config", ioe);
            }
        }
        
        if (!parseArguments(args)) {
            usage();
            System.exit(-1);
        }
         
        try {
            // read the list of web server jar files
            InputStream is = WebServerLauncher.class.getResourceAsStream("/META-INF/webserver.jars");
            BufferedReader in = new BufferedReader(new InputStreamReader(is));
        
            // extract to a webserver directory in the default temp dir
            File webDir = new File(RunUtil.getRunDir(), "webserver");
            webDir.mkdirs();
            
            String line;
            List<URL> urls = new ArrayList<URL>();
            while ((line = in.readLine()) != null) {
                File f = RunUtil.extract(WebServerLauncher.class, line, webDir);
                if (f != null) {
                    System.out.println("Adding URL " + f.toURL());
                    urls.add(f.toURL());
                }
            }
            
            // create a classloader with those files and use it
            // to reflectively instantiate an instance of the 
            // RunAppServer class, and call its run method
            ClassLoader cl = new URLClassLoader(urls.toArray(new URL[0]));
            Thread.currentThread().setContextClassLoader(cl);
            
            Class c = cl.loadClass("org.jdesktop.wonderland.webserver.RunAppServer");
            c.newInstance();
        
            // log that everything is started up
            System.out.println("-----------------------------------------------------------");
            System.out.println("Wonderland web server started successfully.");
            System.out.println("Log files are in " + 
                               SystemPropertyUtil.getProperty("wonderland.log.dir"));
            
            // get the port the web server is running on
            // TODO: get the host too
            System.out.println("Web server running on http://localhost:" +
                               SystemPropertyUtil.getProperty(
                                    WebServerLauncher.WEBSERVER_PORT_PROP, "8080"));
            System.out.println("-----------------------------------------------------------");
        
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Error loading web server", ex);
            System.exit(-1);
        }
    }
    
    private static void usage() {
        System.err.println("Usage: WebServerLauncher [-p port] [-d directory]" +
                           " [properties file]");
        System.err.println(" -p port: the port number to run the server on");
        System.err.println(" -d directory: the directory to install Wonderland in");
        System.err.println(" props: a properties file that overrides default values");
    }
    
    private static boolean parseArguments(String[] args) {
        String port      = null;
        String directory = null;
        String propsFile = null;
        
        Iterator<String> i = Arrays.asList(args).iterator();
        while(i.hasNext()) {
            String s = i.next();
            
            if (s.equalsIgnoreCase("-p")) {
                if (!i.hasNext()) return false;
                
                port = i.next();
            } else if (s.equalsIgnoreCase("-d")) {
                if (!i.hasNext()) return false;
                
                directory = i.next();
            } else {
                if (i.hasNext()) return false;
                
                propsFile = s;
            }
        }
        
        // first load the properties file, if any
        if (propsFile != null) {
            try {
                System.getProperties().load(new FileInputStream(propsFile));
            } catch (IOException ioe) {
                logger.log(Level.WARNING, "Error reading props file " + propsFile,
                           ioe);
                return false;
            }
        }
        
        // override the port and directory if specified
        if (port != null) {
            System.setProperty(WEBSERVER_PORT_PROP, port);
        }
        if (directory != null) {
            System.setProperty(RunUtil.RUN_DIR_PROP, directory);
        }
        
        return true;
    }
}
