/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
public class FrontPageRegistration {
    public static final String FRONT_PAGE_REGISTRY_PROP = "FrontPageRegistry";
    private static final String ADMIN_CONTEXT = "/wonderland-web-front";
   
    private String context;
    private String url;
    private boolean absolute;
    private FrontPageRegistration.RegistrationFilter filter;
    private int position = Integer.MAX_VALUE / 2;

    /**
     * Register a new admin entry
     * @param reg the registration to register
     * @param context the servlet context
     */
    public static void register(FrontPageRegistration reg, ServletContext context) {
        List<FrontPageRegistration> registry = getRegistry(context);
        registry.add(reg);
    }
    
    /**
     * Unregister an admin entry
     * @param reg the registration to unregister
     * @param context the servlet context
     */
    public static void unregister(FrontPageRegistration reg, ServletContext context) {
        List<FrontPageRegistration> registry = getRegistry(context);
        registry.remove(reg);
    }
    
    public static List<FrontPageRegistration> getRegistry(ServletContext context) {
        ServletContext frontPageContext = context.getContext(ADMIN_CONTEXT);
        if (frontPageContext == null) {
            throw new IllegalStateException("Unable to find context " + 
                                            ADMIN_CONTEXT);
        }
        
        List<FrontPageRegistration> registry = (List<FrontPageRegistration>)
                frontPageContext.getAttribute(FRONT_PAGE_REGISTRY_PROP);
        if (registry == null) {
            throw new IllegalStateException("Unable to find property " +
                                            FRONT_PAGE_REGISTRY_PROP);
        }
        
        return registry;
    }
    

    /**
     * Create a new registration for the given name and URL
     * @param shortName the short version of this registration's name
     * @param displayName the registration name to display on the admin page
     * @param url the URL to link to
     */
    public FrontPageRegistration(String context, String url) {

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

    public FrontPageRegistration.RegistrationFilter getFilter() {
        return filter;
    }

    public void setFilter(FrontPageRegistration.RegistrationFilter filter) {
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
    public static final FrontPageRegistration.RegistrationFilter FRONT_PAGE_FILTER = new FrontPageRegistration.RegistrationFilter() {
        public boolean isVisible(HttpServletRequest request,
                                 HttpServletResponse response)
        {
            return request.isUserInRole("admin");
        }
    };

    /** a filter that is only visible to logged in users */
    public static final FrontPageRegistration.RegistrationFilter LOGGED_IN_FILTER = new FrontPageRegistration.RegistrationFilter() {
        public boolean isVisible(HttpServletRequest request,
                                 HttpServletResponse response)
        {
            return (request.getUserPrincipal() != null);
        }
    };
}
