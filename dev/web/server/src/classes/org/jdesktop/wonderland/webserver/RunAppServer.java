/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jdesktop.wonderland.webserver;

import org.jdesktop.wonderland.webserver.launcher.WebUtil;
import org.jdesktop.wonderland.webserver.launcher.WebServerLauncher;
import com.sun.enterprise.v3.server.ServerEnvironmentImpl;
import com.sun.hk2.component.InhabitantsParser;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import org.glassfish.api.deployment.archive.ReadableArchive;
import org.glassfish.embed.App;
import org.glassfish.embed.AppServer;

/**
 *
 * @author jkaplan
 */
public class RunAppServer {
    // singleton instance
    private static AppServer appServer;    // the port to start glassfish on
    private static int port = 8080;

    public RunAppServer() throws IOException {
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
            as.deploy(f);
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
