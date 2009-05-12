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
public interface GroupResolver {
    /**
     * Set up this group resolver with the options from the given map.
     * The map is the one passed in to the SAM during intialization.
     * @param opts the map passed in to the initialization of the SAM
     */
    public void initialize(Map opts);
    
    /**
     * Get the set of groups associated with a user id.  At the minimum, this
     * should include a default group as specified in the options.
     * @param userId a non-null user id
     * @return a set of groups that the user is a member of
     */
    public String[] getGroupsForUser(String userId);
}
