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
package org.jdesktop.wonderland.front.admin;

import java.util.List;
import javax.servlet.ServletContext;

/**
 * Used to register new entries on the Wonderland admin page
 * @author jkaplan
 */
public class AdminRegistration {
    public static final String ADMIN_REGISTRY_PROP = "AdminRegistry";
    private static final String ADMIN_CONTEXT = "/wonderland-web-front";
   
    private String shortName;
    private String displayName;
    private String url;
   
    
    /**
     * Register a new admin entry
     * @param reg the registration to register
     * @param context the servlet context
     */
    public static void register(AdminRegistration reg, ServletContext context) {
        List<AdminRegistration> registry = getRegistry(context);
        registry.add(reg);
    }
    
    /**
     * Unregister an admin entry
     * @param reg the registration to unregister
     * @param context the servlet context
     */
    public static void unregister(AdminRegistration reg, ServletContext context) {
        List<AdminRegistration> registry = getRegistry(context);
        registry.remove(reg);
    }
    
    public static List<AdminRegistration> getRegistry(ServletContext context) {
        ServletContext adminContext = context.getContext(ADMIN_CONTEXT);
        if (adminContext == null) {
            throw new IllegalStateException("Unable to find context " + 
                                            ADMIN_CONTEXT);
        }
        
        List<AdminRegistration> registry = (List<AdminRegistration>)
                adminContext.getAttribute(ADMIN_REGISTRY_PROP);
        if (registry == null) {
            throw new IllegalStateException("Unable to find property " +
                                            ADMIN_REGISTRY_PROP);
        }
        
        return registry;
    }
    
    /**
     * Create a new registration for the given name and URL
     * @param displayName the registration name to display on the admin page
     * @param url the URL to link to
     */
    public AdminRegistration(String displayName, String url) {
        this (null, displayName, url);
    }
    
    /**
     * Create a new registration for the given name and URL
     * @param shortName an optional short name for this registration
     * @param displayName the registration name to display on the admin page
     * @param url the URL to link to
     */
    public AdminRegistration(String shortName, String displayName, String url) {
        this.shortName = shortName;
        this.displayName = displayName;
        this.url = url;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
