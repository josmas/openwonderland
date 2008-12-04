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
package org.jdesktop.wonderland.webserver;

import com.sun.enterprise.deployment.archivist.WebArchivist;
import com.sun.enterprise.web.WebDeployer;
import org.jdesktop.wonderland.utils.RunUtil;
import org.jdesktop.wonderland.webserver.launcher.WebServerLauncher;
import com.sun.hk2.component.InhabitantsParser;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;
import javax.xml.bind.JAXBException;
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
import org.jdesktop.wonderland.common.modules.ModuleInfo;
import org.jdesktop.wonderland.modules.Module;
import org.jdesktop.wonderland.modules.ModuleAttributes;
import org.jdesktop.wonderland.modules.service.ModuleManager;
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
        // check if we need to make any changes
        if (Boolean.parseBoolean(
                System.getProperty(Constants.WEBSERVER_NEWVERSION_PROP)))
        {
            // remove old modules
            uninstallModules();
        
            // install the default modules
            installModules();
            
            // write the web server's document root
            writeDocumentRoot();
            
            // write the updated webapps
            writeWebApps();
        }

        // redeploy any other modules that haven't yet been deployed
        ModuleManager.getModuleManager().redeployAll();
        
        // now deploy web apps
        deployWebApps();
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

    private void uninstallModules() throws IOException {
        ModuleManager mm = ModuleManager.getModuleManager();
        Map<String, Module> system = 
                mm.getInstalledModulesByKey(ModuleAttributes.SYSTEM_MODULE);
        Map<String, String> checksums =
                FileListUtil.readChecksums("META-INF/modules");

        logger.warning("Uninstalling " + system.size() + " modules.");
        
        // add to the list of modules to uninstall
        mm.addToUninstall(system.keySet());
        
        // actually uninstall all modules on the uninstall list
        mm.uninstallAll();
    }
    
    private void installModules() throws IOException {
        // extract modules to a directory, and make a list of the extracted
        // modules
        File moduleDir = RunUtil.createTempDir("module", ".jar");

        // build a collection of File object that we want to add
        Collection<File> modules = new ArrayList<File>();

        Map<String, String> checksums =
                FileListUtil.readChecksums("META-INF/modules");
        for (String file : checksums.keySet()) {
            String fullPath = "/modules/" + file;
            modules.add(RunUtil.extract(getClass(), fullPath, moduleDir));
        }

        // add all modules at once to the module manager.  This will ensure
        // that dependency checks take all modules into account. This can also
        // check return values.
        Collection<Module> added = ModuleManager.getModuleManager().addToInstall(modules);
        
        // now go through each module we added, and tag it with the 
        // system module metadata tag so we can be sure it gets removed
        // next time we install the jar
        for (Module m : added) {
            tagModule(m);
        }
    }
    
    private void tagModule(Module m) throws IOException {
        try {
            File infoFile = new File(m.getFile(), Module.MODULE_INFO);
            ModuleInfo info = ModuleInfo.decode(new FileReader(infoFile));
            info.putAttribute(ModuleAttributes.SYSTEM_MODULE,
                              String.valueOf(true));
            info.encode(new FileWriter(infoFile));
        } catch (JAXBException je) {
            IOException ioe = new IOException("Error writing module info");
            ioe.initCause(je);
            throw ioe;
        }
    }
    
    // get the main instance
    public synchronized static WonderlandAppServer getAppServer() {
        if (appServer == null) {
            appServer = new WonderlandAppServer(port);
        }

        return appServer;
    }
    
    static class WonderlandAppServer extends AppServer {
        public WonderlandAppServer(int port) {
            super (port);
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
