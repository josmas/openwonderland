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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
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
import java.util.logging.Logger;

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
    public static final String RUN_DIR_PROP = "wonderland.run.dir";
    
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
            for (String prop : props.stringPropertyNames()) {
                if (!System.getProperties().containsKey(prop)) {
                    System.setProperty(prop, props.getProperty(prop));
                }
            }
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Error loading default properties", ex);
            System.exit(-1);
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
            File webDir = new File(WebUtil.getTempBaseDir(), "webserver");
            webDir.mkdirs();
            
            String line;
            List<URL> urls = new ArrayList<URL>();
            while ((line = in.readLine()) != null) {
                File f = WebUtil.extract(line, webDir);
                System.out.println("Adding URL " + f.toURL());
                urls.add(f.toURL());
            }
            
            // create a classloader with those files and use it
            // to reflectively instantiate an instance of the 
            // RunAppServer class, and call its run method
            ClassLoader cl = new URLClassLoader(urls.toArray(new URL[0]));
            Thread.currentThread().setContextClassLoader(cl);
            
            Class c = cl.loadClass("org.jdesktop.wonderland.webserver.RunAppServer");
            c.newInstance();
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Error loading web server", ex);
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
                System.getProperties().load(new FileReader(propsFile));
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
            System.setProperty(RUN_DIR_PROP, directory);
        }
        
        return true;
    }
}
