/**
 * Open Wonderland
 *
 * Copyright (c) 2011 - 2012, Open Wonderland Foundation, All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * The Open Wonderland Foundation designates this particular file as
 * subject to the "Classpath" exception as provided by the Open Wonderland
 * Foundation in the License file that accompanied this code.
 */
package org.jdesktop.wonderland.modules.errorreport.web;

import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import org.jdesktop.wonderland.front.admin.AdminRegistration;
import org.jdesktop.wonderland.modules.contentrepo.common.ContentCollection;
import org.jdesktop.wonderland.modules.contentrepo.common.ContentNode;
import org.jdesktop.wonderland.modules.contentrepo.common.ContentRepositoryException;
import org.jdesktop.wonderland.modules.contentrepo.web.spi.WebContentRepository;
import org.jdesktop.wonderland.modules.contentrepo.web.spi.WebContentRepositoryRegistry;
import org.jdesktop.wonderland.modules.errorreport.common.ErrorReport;

/**
 * Register and unregister the error report menu item
 * @author Jonathan Kaplan <jonathankap@gmail.com>
 */
public class ErrorReportContextListener implements ServletContextListener {
    private static final Logger LOGGER =
            Logger.getLogger(ErrorReportContextListener.class.getName());
    
    private static final String ERROR_REPORT_KEY = "__errorReportLogRegistration";
    private static final String TIMER_KEY = "__errorReportTimer";
    
    private ServletContext context;

    public void contextInitialized(ServletContextEvent sce) {
        context = sce.getServletContext();

        // register with the UI
        AdminRegistration ar = new AdminRegistration("Error Reports",
                                                     "/error-report/error-report/ErrorReports.html");
        ar.setFilter(AdminRegistration.ADMIN_FILTER);
        AdminRegistration.register(ar, context);
        context.setAttribute(ERROR_REPORT_KEY, ar);
        
        // try to create the library path
        tryRepo();
    }

    public void contextDestroyed(ServletContextEvent sce) {
        AdminRegistration ar = (AdminRegistration) context.getAttribute(ERROR_REPORT_KEY);
        if (ar != null) {
            AdminRegistration.unregister(ar, context);
        }
        
        Timer timer = (Timer) context.getAttribute(TIMER_KEY);
        if (timer != null) {
            timer.cancel();
        }
    }
    
    /**
     * Try to create the content repo directory. Schedule a task to retry in 5 
     * seconds if not found
     */
    private void tryRepo() {
        Timer timer = (Timer) context.getAttribute(TIMER_KEY);
                
        try {
            if (createRepoPath()) {
                // create succeeded, cancel any timers
                if (timer != null) {
                    timer.cancel();
                    context.removeAttribute(TIMER_KEY);
                }                
            } else {
                // try again later
                if (timer == null) {
                    timer = new Timer();
                    context.setAttribute(TIMER_KEY, timer);
                }
                
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        tryRepo();
                    }                    
                }, 5000);
            }
        } catch (ContentRepositoryException ce) {
            LOGGER.log(Level.WARNING, "Error creating content repository", ce);
        }
    }
    
    /**
     * Guarantee that the content repo path we need is available.
     * @return false if the content repository is not available yet
     */
    private boolean createRepoPath() throws ContentRepositoryException {
        WebContentRepositoryRegistry reg = WebContentRepositoryRegistry.getInstance();
        WebContentRepository repo = reg.getRepository(context);
    
        if (repo == null) {
            // not ready yet
            return false;
        }
        
        // create directory if it doesn't exist
        ContentCollection groups = (ContentCollection) repo.getRoot().getChild("groups");
        if (groups == null) {
            groups = (ContentCollection) 
                    repo.getRoot().createChild("groups", ContentNode.Type.COLLECTION);
        }

        ContentCollection users = (ContentCollection) groups.getChild("users");
        if (users == null) {
            users = (ContentCollection)
                    groups.createChild("users", ContentNode.Type.COLLECTION);
        }

        ContentCollection dir = (ContentCollection) users.getChild(ErrorReport.DIR_NAME);
        if (dir == null) {
            users.createChild(ErrorReport.DIR_NAME, ContentNode.Type.COLLECTION);
        }
        
        return true;
    }
    
}
