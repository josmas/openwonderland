/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jdesktop.wonderland.webserver;

import org.jdesktop.wonderland.webserver.launcher.WebUtil;
import org.jdesktop.wonderland.webserver.launcher.WebServerLauncher;
import com.sun.hk2.component.InhabitantsParser;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import org.glassfish.api.deployment.archive.ReadableArchive;
import org.glassfish.embed.App;
import org.glassfish.embed.AppServer;
import org.glassfish.server.ServerEnvironmentImpl;
import org.jdesktop.wonderland.modules.service.AddedModule;
import org.jdesktop.wonderland.modules.service.ModuleManager;
import org.jdesktop.wonderland.utils.SystemPropertyUtil;

/**
 *
 * @author jkaplan
 */
public class RunAppServer {
    // singleton instance
    private static AppServer appServer;    // the port to start glassfish on
    private static int port;

    static {
        String portStr = SystemPropertyUtil.getProperty(WebServerLauncher.WEBSERVER_PORT_PROP,
                                                        "8080");
        port = Integer.parseInt(portStr);
    }
    
    public RunAppServer() throws IOException {
        // first deploy any web apps
        //deployWebApps();
        
        // now install the default modules
        installModules();
    }

    private void deployWebApps() throws IOException {
        AppServer as = getAppServer();

        // read the list of .war files to deploy
        InputStream is = WebServerLauncher.class.getResourceAsStream("/META-INF/deploy.jars");
        BufferedReader in = new BufferedReader(new InputStreamReader(is));

        // write to a subdirectory of the default temp directory
        File deployDir = new File(WebUtil.getTempBaseDir(), "deploy");
        deployDir.mkdirs();
        
        String line;
        while ((line = in.readLine()) != null) {
            File f = WebUtil.extractJar(line, deployDir);
            try {
                as.deploy(f);
            } catch (Exception excp) {
                // ignore any exception and continue
            }
        }
    }
    
    private void installModules() throws IOException {
        // read the list of .war files to deploy
        InputStream is = WebServerLauncher.class.getResourceAsStream("/META-INF/module.jars");
        BufferedReader in = new BufferedReader(new InputStreamReader(is));
    
        File moduleDir = ModuleManager.getModuleManager().getModuleRoot(ModuleManager.State.ADD);
        
        String line;
        while ((line = in.readLine()) != null) {
            File f = WebUtil.extract(line, moduleDir);
            
            // Take the ".jar" off the end of the jar name to get the module
            // name.  So for example "/modules/samplemodule.jar" should be
            // "samplemodule".  I think this gets replaced by the actual name later
            // in the process.
            String fileName = line.substring(line.lastIndexOf("/"),
                                             line.lastIndexOf("."));
            AddedModule am = new AddedModule(f.getParentFile(), fileName);
                     
            // add the module to the manager
            ModuleManager.getModuleManager().add(am);
        }
    }
    
    // get the main instance
    private synchronized static AppServer getAppServer() {
        if (appServer == null) {
            appServer = new AppServer(port) {

                @Override
                protected InhabitantsParser decorateInhabitantsParser(InhabitantsParser parser) {
                    InhabitantsParser out = super.decorateInhabitantsParser(parser);
                    parser.replace(ServerEnvironmentImpl.class, DirServerEnvironment.class);
                    return out;
                }

                @Override
                public App deploy(File archive) throws IOException {
                    // override because we know this will always be a
                    // a directory, and we don't want it extracted to
                    // the current working directory
                    ReadableArchive a = archiveFactory.openArchive(archive);
                    return deploy(a);
                }
            };
        }

        return appServer;
    }
}
