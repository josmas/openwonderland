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

import org.jdesktop.wonderland.utils.RunUtil;
import org.jdesktop.wonderland.webserver.launcher.WebServerLauncher;
import com.sun.hk2.component.InhabitantsParser;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
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
        // install the default modules
        installModules();
        
        // now deploy web apps
        deployWebApps();
    }

    private void deployWebApps() throws IOException {
        AppServer as = getAppServer();

        // read the list of .war files to deploy
        InputStream is = WebServerLauncher.class.getResourceAsStream("/META-INF/deploy.jars");
        BufferedReader in = new BufferedReader(new InputStreamReader(is));

        // write to a subdirectory of the default temp directory
        File deployDir = new File(RunUtil.getRunDir(), "deploy");
        deployDir.mkdirs();
        
        String line;
        while ((line = in.readLine()) != null) {
            File f = RunUtil.extractJar(getClass(), line, deployDir);
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
    
        // extract modules to a directory, and make a list of the extracted
        // modules
        File moduleDir = ModuleManager.getModuleManager().getModuleStateDirectory(ModuleManager.State.ADD);
        Collection<AddedModule> modules = new ArrayList<AddedModule>();
        
        String line;
        while ((line = in.readLine()) != null) {
            File f = RunUtil.extract(getClass(), line, moduleDir);
            modules.add(new AddedModule(f));
        }
        
        // add all modules at once to the module manager.  This will ensure
        // that dependency checks take all modules into account.
        ModuleManager.getModuleManager().addAll(modules, true);
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
