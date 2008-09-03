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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
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
    private static final Logger logger = 
            Logger.getLogger(WebServerLauncher.class.getName());
    
    public static void main(String[] args) {
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
}
