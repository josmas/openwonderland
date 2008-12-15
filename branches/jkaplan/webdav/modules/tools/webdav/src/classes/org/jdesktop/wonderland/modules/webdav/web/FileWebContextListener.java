/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.modules.webdav.web;

import java.io.File;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import org.jdesktop.wonderland.modules.contentrepo.web.spi.WebContentRepositoryRegistry;
import org.jdesktop.wonderland.utils.RunUtil;

/**
 *
 * @author jkaplan
 */
public class FileWebContextListener implements ServletContextListener {
    public void contextInitialized(ServletContextEvent sce) {
        ServletContext sc = sce.getServletContext();

        // make sure base repository directories exist
        File contentDir = new File(RunUtil.getRunDir(), "content");
        File systemDir = new File(contentDir, "system");
        systemDir.mkdirs();
        File usersDir = new File(contentDir, "users");
        usersDir.mkdirs();

        WebContentRepositoryRegistry.getInstance().registerRepository(sc,
                new FileWebContentRepository(contentDir));
    }

    public void contextDestroyed(ServletContextEvent sce) {
        ServletContext sc = sce.getServletContext();
        WebContentRepositoryRegistry.getInstance().unregisterRepository(sc);
    }
}
