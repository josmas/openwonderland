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

import java.net.URLConnection;
import org.jdesktop.wonderland.utils.RunUtil;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLStreamHandler;
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
    public static final String WEBSERVER_HOST_PROP = "wonderland.webserver.host";
    public static final String WEBSERVER_URL_PROP  = "wonderland.web.server.url";

    // port to listen for killswitch connections
    private static final String WEBSERVER_KILLSWITCH_PROPERTY =
            "wonderland.webserver.killswitch";

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

        // start the killswitch
        String killSwitchStr = System.getProperty(WEBSERVER_KILLSWITCH_PROPERTY);
        if (killSwitchStr != null) {
            KillSwitch ks = new KillSwitch(Integer.parseInt(killSwitchStr));
            new Thread(ks).start();
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
                    f.deleteOnExit();
                    
                    URL u = f.toURI().toURL();
                    System.out.println("Adding URL " + u);
                    
                    urls.add(u);
                }
            }
            
            // create a classloader with those files and use it
            // to reflectively instantiate an instance of the 
            // RunAppServer class, and call its run method
            ClassLoader cl = new URLClassLoader(urls.toArray(new URL[0])) {

                @Override
                public URL[] getURLs() {
                    NoProtocolURLStreamHandler npush =
                            new NoProtocolURLStreamHandler();

                    URL[] out = super.getURLs();
                    for (int i = 0; i < out.length; i++) {
                        if (out[i].getProtocol().equals("file")) {
                            try {
                                out[i] = new URL(out[i], "", npush);
                            } catch (MalformedURLException ex) {
                                // ignore, leave URL as is
                            }
                        }
                    }

                    return out;
                }

            };
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
            System.out.println("Web server running on " +
                               SystemPropertyUtil.getProperty(
                                    WebServerLauncher.WEBSERVER_URL_PROP));
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
        } else {
            System.setProperty(WEBSERVER_PORT_PROP, "8080");
        }

        if (directory != null) {
            System.setProperty(RunUtil.RUN_DIR_PROP, directory);
        }

        // set guess the hostname for this server
        if (System.getProperty(WEBSERVER_HOST_PROP) == null) {
            System.setProperty(WEBSERVER_HOST_PROP, getHostname());
        }

        // set the web server URL based on the hostname and port
        if (System.getProperty(WEBSERVER_URL_PROP) == null) {
            System.setProperty(WEBSERVER_URL_PROP,
                "http://" + System.getProperty(WEBSERVER_HOST_PROP) +
                ":" + System.getProperty(WEBSERVER_PORT_PROP) + "/");
        }

        return true;
    }

    // get the hostname to send out
    private static String getHostname() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (IOException ioe) {
            logger.log(Level.WARNING, "Error determining host name", ioe);
            return "localhost";
        }
    }

    /**
     * Workaround for windows issue in Glassfish.  On Windows, Glassfish
     * creates the sytem classpath by appending URLs together separated
     * by the system path separator.  On Windows, that looks like:
     * <code>file:URL1;file:URL2</code>
     * On other platforms, you get:
     * <code>file:URL1:file:URL2</code>
     * This is done in <code>ASClassLoaderUtils.addLibrariesFromLibs()</code>.
     * <p>
     * Later, Jasper (the JSP compiler) tries to figure out the classpath
     * by tokenizing the URL strings on the system path separator, and
     * treating the result as a file.  On Windows, this doesn't work,
     * because the list ends up being:
     * <code>file:URL1, file:URL2</code>
     * Whereas on other platforms it is:
     * <code>file, URL1, file, URL2</code>
     * This is done in <code>org.apache.jasper.Compiler.generateClass()</code>
     * <p>
     * The  short term soltution is to return URLs that don't include the
     * protocol in their toString() method.  This obviously only works for
     * file: URLs, but Glassfish wouldn't support http: URLs anyway.
     */
    static class NoProtocolURLStreamHandler extends URLStreamHandler {
        @Override
        protected URLConnection openConnection(URL u) throws IOException {
            return new FileURLConnection(u);
        }

        @Override
        protected String toExternalForm(URL u) {
            return u.getPath();
        }
    }

    static class FileURLConnection extends URLConnection {
        public FileURLConnection(URL url) {
            super (toFileURL(url));
        }

        @Override
        public void connect() throws IOException {
            // do nothing
        }

        public static URL toFileURL(URL u) {
            try {
                return new URL("file:" + u.toExternalForm());
            } catch (MalformedURLException mue) {
                return u;
            }
        }
    }

    // listen on a particular socket, and exit the server if that
    // socket disconnects
    static class KillSwitch implements Runnable {
        private int port;

        public KillSwitch(int port) {
            this.port = port;
        }

        public void run() {
            try {
                logger.info("[Killswitch]: Initializing killswitch on port " + port);
                ServerSocket server = new ServerSocket(port);
                Socket s = server.accept();
                logger.info("[Killswitch]: accepted connection");
                while (s.getInputStream().read() != -1) {
                    // do nothing, just wait for the stream to close
                }
            } catch (IOException ioe) {
                // an error occured, just ignore it
                logger.log(Level.WARNING, "Error in killswitch", ioe);
            } finally {
                logger.warning("[Killswitch]: disconnected, server shutting " +
                               "down!");
                System.exit(0);
            }
        }
    }
}
