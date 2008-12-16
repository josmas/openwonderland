/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.modules.webdav.web;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import org.jdesktop.wonderland.modules.contentrepo.web.spi.WebContentRepositoryRegistry;
import org.jdesktop.wonderland.utils.Constants;
import org.jdesktop.wonderland.utils.RunUtil;

/**
 *
 * @author jkaplan
 */
public class FileWebContextListener implements ServletContextListener {
    private static final Logger logger =
            Logger.getLogger(FileWebContextListener.class.getName());

    public void contextInitialized(ServletContextEvent sce) {
        ServletContext sc = sce.getServletContext();

        // make sure base repository directories exist
        File contentDir = new File(RunUtil.getRunDir(), "content");
        File systemDir = new File(contentDir, "system");
        systemDir.mkdirs();
        File usersDir = new File(contentDir, "users");
        usersDir.mkdirs();

        String baseURLStr = System.getProperty(Constants.WEBSERVER_URL_PROP);
        baseURLStr += "/webdav/content/";
        URL baseURL = null;
        try {
            baseURL = new URL(baseURLStr);
        } catch (MalformedURLException mue) {
            logger.log(Level.WARNING, "Error parsing URL " + baseURLStr, mue);
        }

        WebContentRepositoryRegistry.getInstance().registerRepository(sc,
                    new FileWebContentRepository(contentDir, baseURL));
    }

    public void contextDestroyed(ServletContextEvent sce) {
        ServletContext sc = sce.getServletContext();
        WebContentRepositoryRegistry.getInstance().unregisterRepository(sc);
    }
}
