/**
 * Open Wonderland
 *
 * Copyright (c) 2012, Open Wonderland Foundation, All Rights Reserved
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
package org.jdesktop.wonderland.front.admin;

import java.util.List;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author JagWire
 */
public class StatusPageRegistration {
    public static final String STATUS_PAGE_REGISTRY_PROP = "StatusPageRegistry";
    private static final String ADMIN_CONTEXT = "/wonderland-web-front";
   
    private String context;
    private String url;
    private boolean absolute;
    private RegistrationFilter filter;
    private int position = Integer.MAX_VALUE / 2;

    /**
     * Register a new admin entry
     * @param reg the registration to register
     * @param context the servlet context
     */
    public static void register(StatusPageRegistration reg, ServletContext context) {
        List<StatusPageRegistration> registry = getRegistry(context);
        registry.add(reg);
    }
    
    /**
     * Unregister an admin entry
     * @param reg the registration to unregister
     * @param context the servlet context
     */
    public static void unregister(StatusPageRegistration reg, ServletContext context) {
        List<StatusPageRegistration> registry = getRegistry(context);
        registry.remove(reg);
    }
    
    public static List<StatusPageRegistration> getRegistry(ServletContext context) {
        ServletContext statusPageContext = context.getContext(ADMIN_CONTEXT);
        if (statusPageContext == null) {
            throw new IllegalStateException("Unable to find context " + 
                                            ADMIN_CONTEXT);
        }
        
        List<StatusPageRegistration> registry = (List<StatusPageRegistration>)
                statusPageContext.getAttribute(STATUS_PAGE_REGISTRY_PROP);
        if (registry == null) {
            throw new IllegalStateException("Unable to find property " +
                                            STATUS_PAGE_REGISTRY_PROP);
        }
        
        return registry;
    }
    

    /**
     * Create a new registration for the given name and URL
     * @param shortName the short version of this registration's name
     * @param displayName the registration name to display on the admin page
     * @param url the URL to link to
     */
    public StatusPageRegistration(String context, String url) {

        this.context = context;
        this.url = url;
    }
    
    public String getContext() {
        return context;
    }
    
    public void setContext(String context) {
        this.context = context;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public boolean isAbsolute() {
        return absolute;
    }

    public void setAbsolute(boolean absolute) {
        this.absolute = absolute;
    }

    public StatusPageRegistration.RegistrationFilter getFilter() {
        return filter;
    }

    public void setFilter(StatusPageRegistration.RegistrationFilter filter) {
        this.filter = filter;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    /**
     * A filter that determines if the given menu item is visible for the
     * given request and response. The request will be for the admin
     * page, and will have username and group information if the user
     * is logged in.
     */
    public interface RegistrationFilter {
        /**
         * Return true if the menu item should be visible, or false if not.
         */
        public boolean isVisible(HttpServletRequest request,
                                 HttpServletResponse response);
    }

    /** a filter that is only visible to admins */
    public static final StatusPageRegistration.RegistrationFilter STATUS_PAGE_FILTER = new StatusPageRegistration.RegistrationFilter() {
        public boolean isVisible(HttpServletRequest request,
                                 HttpServletResponse response)
        {
            return request.isUserInRole("admin");
        }
    };

    /** a filter that is only visible to logged in users */
    public static final StatusPageRegistration.RegistrationFilter LOGGED_IN_FILTER = new StatusPageRegistration.RegistrationFilter() {
        public boolean isVisible(HttpServletRequest request,
                                 HttpServletResponse response)
        {
            return (request.getUserPrincipal() != null);
        }
    };
}
