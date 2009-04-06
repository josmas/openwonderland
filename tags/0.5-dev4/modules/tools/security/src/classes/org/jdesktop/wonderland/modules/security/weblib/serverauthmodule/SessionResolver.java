/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.modules.security.weblib.serverauthmodule;

import java.util.Map;

/**
 *
 * @author jkaplan
 */
public interface SessionResolver {
    public static final String COOKIE_NAME = "WonderlandAuthCookie";

    /**
     * Set up this session resolver with the options from the given map.
     * The map is the one passed in to the SAM during intialization
     * @param opts the map passed in to the initialization of the SAM
     */
    public void initialize(Map opts);

    /**
     * Get the userId associated with the given token
     * @param token the token to use
     * @return the user id for the given token or null if the token is
     * invalid or null
     */
    public String getUserId(String token);
}
