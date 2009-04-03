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
package org.jdesktop.wonderland.modules.webdav.web.content;

import java.io.File;
import java.util.logging.Logger;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import org.apache.catalina.Globals;
import org.apache.naming.resources.BaseDirContext;
import org.apache.naming.resources.ProxyDirContext;
import org.jdesktop.wonderland.utils.RunUtil;

/**
 * Set the default directory for webdav
 * @author jkaplan
 */
public class WebdavContextListener implements ServletContextListener {
    private static final Logger logger =
            Logger.getLogger(WebdavContextListener.class.getName());

    // This is a hack to set the context directory that the catalina
    // WebDAV servlet writes to.  The webdav servlet uses the built-in
    // javax.naming.DirContext object to actually find the files that
    // WebDAV is referring to.  In this method, we change the default
    // directory of the underlying DirContext that is actually in use.
    //
    // It is critical that no classes other than this and the WebDAV servlet
    // (which is built into Glassfish) be included in the .war.  By
    // changing the docRoot of the DirContext, we change not only where
    // WebDAV files go, but also where classes are loaded from using
    // the WebappClassLoader in Glassfish.  Therefore any classes loaded
    // after this one will not work if they are defined in the .war file.
    public void contextInitialized(ServletContextEvent sce) {
        ServletContext sc = sce.getServletContext();
        ProxyDirContext orig = (ProxyDirContext) sc.getAttribute(Globals.RESOURCES_ATTR);

        // replace docroot
        if (orig.getDirContext() instanceof BaseDirContext) {
            File contentDir = RunUtil.getContentDir();

            logger.info("Setting document base to " + contentDir.getPath());

            ((BaseDirContext) orig.getDirContext()).setDocBase(contentDir.getPath());
        } else {
            logger.warning("Unable to set base directory for " + orig.getDirContext());
        }
    }

    public void contextDestroyed(ServletContextEvent sce) {
    }
}
