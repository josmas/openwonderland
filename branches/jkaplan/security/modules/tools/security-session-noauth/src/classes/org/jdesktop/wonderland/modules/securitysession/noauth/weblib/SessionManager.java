/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.modules.securitysession.noauth.weblib;

/**
 *
 * @author jkaplan
 */
public interface SessionManager {
    /**
     * Get a user record by name.  The record will not be created if it
     * doesn't exist.  Identical to calling <code>get(userId, false)</code>.
     * @param userId the name of the user to get
     * @return the user with the given name, or null if no users exist with
     * the given name
     */
    public UserRecord get(String userId);

    /**
     * Get a user record by name, optionally creating it if it doesn't
     * exist.  A newly created user will always have a new, unique token.
     * @param userId the name of the user to get
     * @param create true to create the user record if one doesn't exist
     * for the given id, or false not to
     * @return the user with the given name, or null if no users exist with
     * the given name and create is false.
     */
    public UserRecord get(String userId, boolean create);

    /**
     * Get the user corresponding to the given token
     * @param token the user's id
     * @return the user with the given token, or null if no user maps to the
     * given token
     */
    public UserRecord getByToken(String token);

    /**
     * Remove the given user
     * @param token the session token to invalidate
     * @return the user associated with the given token, or null if no
     * user is associated with the token
     */
    public UserRecord remove(String token);
}
