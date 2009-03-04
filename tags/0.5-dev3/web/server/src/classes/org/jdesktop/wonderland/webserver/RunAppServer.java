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
package org.jdesktop.wonderland.webserver;

import com.sun.enterprise.deployment.archivist.WebArchivist;
import com.sun.enterprise.web.WebDeployer;
import org.jdesktop.wonderland.utils.AppServerMonitor;
import org.jdesktop.wonderland.utils.RunUtil;
import org.jdesktop.wonderland.webserver.launcher.WebServerLauncher;
import com.sun.hk2.component.InhabitantsParser;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import org.glassfish.api.deployment.archive.ReadableArchive;
import org.glassfish.embed.App;
import org.glassfish.embed.AppServer;
import org.glassfish.embed.EmbeddedException;
import org.glassfish.embed.EmbeddedHttpListener;
import org.glassfish.embed.EmbeddedVirtualServer;
import org.glassfish.server.ServerEnvironmentImpl;
import org.jdesktop.wonderland.client.jme.WonderlandURLStreamHandlerFactory;
import org.jdesktop.wonderland.common.NetworkAddress;
import org.jdesktop.wonderland.common.modules.ModuleInfo;
import org.jdesktop.wonderland.modules.Module;
import org.jdesktop.wonderland.modules.ModuleAttributes;
import org.jdesktop.wonderland.modules.service.ModuleManager;
import org.jdesktop.wonderland.modules.service.ModuleManager.TaggedModule;
import org.jdesktop.wonderland.utils.Constants;
import org.jdesktop.wonderland.utils.SystemPropertyUtil;
import org.jdesktop.wonderland.utils.FileListUtil;
import org.jvnet.hk2.component.Habitat;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *
 * @author jkaplan
 */
public class RunAppServer {
    // logger
    private static final Logger logger =
            Logger.getLogger(RunAppServer.class.getName());
    
    // singleton instance
    private static WonderlandAppServer appServer;
    private static int port;

    static {
        String portStr = SystemPropertyUtil.getProperty(Constants.WEBSERVER_PORT_PROP,
                                                        "8080");
        port = Integer.parseInt(portStr);
    }
    
    public RunAppServer() throws IOException {
        // set up URL handlers for Wonderland types
        URL.setURLStreamHandlerFactory(new WonderlandURLStreamHandlerFactory()); 

        // now load the properties
        setupProperties();

        // check if we need to make any changes
        if (Boolean.parseBoolean(
                System.getProperty(Constants.WEBSERVER_NEWVERSION_PROP)))
        {
            // replace old versions of modules with newer versions
            updateModules();
            
            // write the web server's document root
            writeDocumentRoot();
            
            // write the updated webapps
            writeWebApps();
        }

        // deploy built-in web apps
        deployWebApps();

        // mark the app server as started
        getAppServer().setStarted(true);

        // redeploy any other modules, including web modules,
        // that haven't yet been deployed.  This will also
        // install all pending modules
        ModuleManager.getModuleManager().redeployAll();

        // now that all the modules are deployed, notify anyone waiting
        // for startup
        AppServerMonitor.getInstance().fireStartupComplete();
    }

    /**
     * Set up some important properties needed everywhere, like the hostname
     * and webserver URL
     */
    private void setupProperties() {
    // set the public hostname for this server based on a lookup
        System.setProperty(Constants.WEBSERVER_HOST_PROP,
                resolveAddress(System.getProperty(Constants.WEBSERVER_HOST_PROP)));

        // set the web server URL based on the hostname and port
        if (System.getProperty(Constants.WEBSERVER_URL_PROP) == null) {
            System.setProperty(Constants.WEBSERVER_URL_PROP,
                "http://" + System.getProperty(Constants.WEBSERVER_HOST_PROP).trim() +
                ":" + System.getProperty(Constants.WEBSERVER_PORT_PROP).trim() + "/");
        }
    }

    /**
     * Resolve the host address property into a hostname.  The mechanics of
     * this are mostly encapsulated in NetworkAddress
     * @param prop the property value to base our lookup on (may be null)
     * @return the public address we found
     */
    private static String resolveAddress(String prop) {
        String hostAddress = null;

        try {
            hostAddress = NetworkAddress.getPrivateLocalAddress(prop).getHostAddress();

            if (prop == null || prop.length() == 0) {
                logger.info("Local address " + hostAddress +
                            " was chosen from the list of interfaces");
            } else {
                logger.info("Local address " + hostAddress +
                            " was determined by using " + prop);
            }

            return hostAddress;
        } catch (UnknownHostException e) {
            logger.log(Level.WARNING, "Unable to get Local address using " +
                       prop, e);

            try {
                hostAddress = NetworkAddress.getPrivateLocalAddress().getHostAddress();
                logger.info("chose private local address " + hostAddress +
                            " from the list of interfaces: " + e.getMessage());
            } catch (UnknownHostException ee) {
                logger.log(Level.WARNING, "Unable to determine private " +
                           "local address, using localhost", ee);

                hostAddress = "localhost";
            }
        }

        return hostAddress;
    }


    private void deployWebApps() throws IOException {
        WonderlandAppServer as = getAppServer();

        // deploy all webapps
        File deployDir = new File(RunUtil.getRunDir(), "deploy");
        for (File war : deployDir.listFiles()) {
            try {
                as.deploy(war);
            } catch (Exception excp) {
                // ignore any exception and continue
            }
        }
    }

    private void writeDocumentRoot() throws IOException {
        File docDir = new File(RunUtil.getRunDir(), "docRoot");
        docDir.mkdirs();

        // figure out the set of files to add or remove
        List<String> addFiles = new ArrayList<String>();
        List<String> removeFiles = new ArrayList<String>();
        FileListUtil.compareDirs("META-INF/docroot", docDir,
                                 addFiles, removeFiles);

        for (String removeFile : removeFiles) {
            File file = new File(docDir, removeFile);
            file.delete();
        }

        for (String addFile : addFiles) {
            String fullPath = "/docroot/" + addFile;
            InputStream fileIs =
                    WebServerLauncher.class.getResourceAsStream(fullPath);

            RunUtil.writeToFile(fileIs, new File(docDir, addFile));
            fileIs.close();
        }

        // write the updated checksum list
        RunUtil.extract(getClass(), "/META-INF/docroot/files.list", docDir);
    }

    private void writeWebApps() throws IOException {
        // write to a subdirectory of the default temp directory
        File deployDir = new File(RunUtil.getRunDir(), "deploy");
        deployDir.mkdirs();

        // figure out the set of files to add or remove
        List<String> addFiles = new ArrayList<String>();
        List<String> removeFiles = new ArrayList<String>();
        FileListUtil.compareDirs("META-INF/deploy", deployDir,
                                 addFiles, removeFiles);

        // remove the files to remove
        for (String removeFile : removeFiles) {
            File remove = new File(deployDir, removeFile);

            // files have been extracted into directories
            if (remove.isDirectory()) {
                RunUtil.deleteDir(remove);
            }
        }

        for (String addFile : addFiles) {
            String fullPath = "/deploy/" + addFile;
            RunUtil.extractJar(getClass(), fullPath, deployDir);
        }

        // write the updated checksum list
        RunUtil.extract(getClass(), "/META-INF/deploy/files.list", deployDir);
    }

    /**
     * Update any system-installed module to be the latest version from
     * the Wonderland.jar file.
     * @throws IOException if there is an error reading or writing modules
     */
    private void updateModules() throws IOException {
        ModuleManager mm = ModuleManager.getModuleManager();

        // create the directory to extract modules to, if it doesn't already
        // exist
        File moduleDir = RunUtil.createTempDir("module", ".jar");

        // read the list of modules and their checksums from the jar file
        Map<String, String> checksums =
                FileListUtil.readChecksums("META-INF/modules");

        // get the list of all installed module with the "system-installed"
        // key set.  This is set on all modules installed by the system
        Map<String, Module> installed =
                mm.getInstalledModulesByKey(ModuleAttributes.SYSTEM_MODULE);

        // get the checksum of any module that has a checksum.  As a
        // side-effect, any module with a checksum is removed from the
        // list of installed modules, so that list can be used to decide
        // which modules to uninstall
        Map<String, String> installedChecksums = getChecksums(installed);

        // add all modules remaining in the installed list to the
        // uninstall list.  These are modules that were installed by an
        // old version of Wonderland and do not have a filename or
        // checksum attribute set.
        Collection<String> uninstall = new ArrayList<String>(installed.keySet());

        // now go through the checksums of old and new modules to determine
        // which modules need to be installed and which are unchanged
        List<TaggedModule> install = new ArrayList<TaggedModule>();
        for (Map.Entry<String, String> checksum : checksums.entrySet()) {

            // compare an existing checksum to an old checksum. If the
            // old checksum doesn't exist or is different than the new
            // checksum, install the new file.  This will overwrite the old
            // checksum in the process.
            String installedChecksum = installedChecksums.remove(checksum.getKey());
            if (installedChecksum == null ||
                    !installedChecksum.equals(checksum.getValue()))
            {
                install.add(createTaggedModule(checksum.getKey(),
                                               checksum.getValue(),
                                               moduleDir));
            }
        }

        // any modules not removed from the installedChecksums list are
        // old modules that were installed by a previous version of Wonderland
        // (not by the user) and aren't in the new module list.  We need to
        // remove these modules
        uninstall.addAll(installedChecksums.keySet());

        // uninstall any modules on the uninstall list
        logger.warning("Uninstall: " + uninstall);
        mm.addToUninstall(uninstall);
        
        // install any modules on the install list
        String installList = "";
        for (TaggedModule tm : install) {
            installList += " " + tm.getFile().getName();
        }
        logger.warning("Install: " + installList);
        mm.addTaggedToInstall(install);
    }

    private Map<String, String> getChecksums(Map<String, Module> modules) {
        Map<String, String> out = new HashMap<String, String>();

        for (Iterator<Module> i = modules.values().iterator(); i.hasNext();) {
            ModuleInfo info = i.next().getInfo();
            String filename = info.getAttribute(ModuleAttributes.FILENAME);
            String checksum = info.getAttribute(ModuleAttributes.CHECKSUM);

            if (filename != null) {
                // add to the list of files with checksums
                out.put(filename, checksum);

                // remove from the installed list
                i.remove();
            }
        }

        return out;
    }

    private TaggedModule createTaggedModule(String file, String checksum,
                                            File moduleDir)
        throws IOException
    {
        // extract the file
        String fullPath = "/modules/" + file;
        File extracted = RunUtil.extract(getClass(), fullPath, moduleDir);

        // now generate the attributes
        Map<String, String> attributes = new HashMap<String, String>();
        attributes.put(ModuleAttributes.SYSTEM_MODULE, String.valueOf(true));
        attributes.put(ModuleAttributes.FILENAME, file);
        attributes.put(ModuleAttributes.CHECKSUM, checksum);

        // create the tagged module
        return new TaggedModule(extracted, attributes);
    }

    // get the main instance
    synchronized static WonderlandAppServer getAppServer() {
        if (appServer == null) {
            appServer = new WonderlandAppServer(port);
        }

        return appServer;
    }
    
    static class WonderlandAppServer extends AppServer {
        private boolean started = false;

        public WonderlandAppServer(int port) {
            super (port);
        }

        public synchronized boolean isStarted() {
            return started;
        }

        synchronized void setStarted(boolean started) {
            this.started = started;
        }

        public Habitat getHabitat() {
            return habitat;
        }
        
        public DirServerEnvironment getServerEnvironment() {
            return (DirServerEnvironment) this.env;
        }
        
        @Override
        protected InhabitantsParser decorateInhabitantsParser(InhabitantsParser parser) {
            InhabitantsParser out = super.decorateInhabitantsParser(parser);

            // replace the default ServerEvironmentImpl with one of our own
            // that uses the Wonderland temp directory instead of "."
            parser.replace(ServerEnvironmentImpl.class, DirServerEnvironment.class);
            
            // repace the web archivist with one that ignores external
            // DTDs
            parser.replace(WebArchivist.class, LocalOnlyWebArchivist.class);
            parser.replace(WebDeployer.class, LocalOnlyWebDeployer.class);
            
            return out;
        }

        @Override
        public App deploy(File archive) throws IOException {
            return deploy(archive, null);
        }
        
        public App deploy(File archive, Properties props) throws IOException {
            // override because we know this will always be a
            // a directory, and we don't want it extracted to
            // the current working directory
            ReadableArchive a = archiveFactory.openArchive(archive);
            return deploy(a, props);
        }

        @Override
        public EmbeddedVirtualServer createVirtualServer(final EmbeddedHttpListener listener) {
            EmbeddedVirtualServer out = super.createVirtualServer(listener);

            // override the document root to forward to the
            // Wonderland front page.
            try {
                File docRoot = new File(RunUtil.getRunDir(), "docRoot");
                docRoot.mkdirs();

                DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                Document d = dbf.newDocumentBuilder().parse(domainXmlUrl.toExternalForm());
                XPath xpath = XPathFactory.newInstance().newXPath();

                Element v = (Element) xpath.evaluate("//http-service/virtual-server/property[@name='docroot']", d, XPathConstants.NODE);
                v.setAttribute("value", docRoot.getCanonicalPath());

                /**
                 * Write domain.xml to a temporary file. UGLY UGLY UGLY.
                 */
                File domainFile = File.createTempFile("wonderlanddomain", "xml");
                domainFile.deleteOnExit();
                Transformer t = TransformerFactory.newInstance().newTransformer();
                t.transform(new DOMSource(d), new StreamResult(domainFile));
                domainXmlUrl = domainFile.toURI().toURL();
            } catch (Exception ex) {
                throw new EmbeddedException(ex);
            }

            return out;
        }
    }
}
