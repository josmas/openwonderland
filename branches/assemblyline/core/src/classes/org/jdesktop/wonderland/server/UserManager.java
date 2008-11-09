/**
 * Project Wonderland
 *
 * Copyright (c) 2004-2008, Sun Microsystems, Inc., All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * $Revision$
 * $Date$
 * $State$
 */
package org.jdesktop.wonderland.server;

import com.sun.sgs.app.AppContext;
import com.sun.sgs.app.ClientSession;
import com.sun.sgs.app.DataManager;
import com.sun.sgs.app.ManagedObject;
import com.sun.sgs.app.ManagedReference;
import com.sun.sgs.app.NameNotBoundException;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import org.jdesktop.wonderland.common.ExperimentalAPI;

/**
 * Manages the entire set of users logged into the system.
 *
 * @author paulby
 */
@ExperimentalAPI
public class UserManager implements ManagedObject, Serializable {
    
    private HashMap<ManagedReference<ClientSession>, ManagedReference<UserMO>> sessionToUser =
	    new HashMap<ManagedReference<ClientSession>, ManagedReference<UserMO>>();
    
    /**
     * Name used in binding this object in DataManager
     **/
    private static final String BINDING_NAME="USER_MANAGER";
    
    private int userLimit = Integer.MAX_VALUE;

    /**
     * Creates a new instance of UserManager
     */
    private UserManager() {
    }
    
    static void initialize() {
        UserManager mgr = new UserManager();
        AppContext.getDataManager().setBinding(BINDING_NAME, mgr);
    }
    
    /**
     * Return singleton user manager
     * @return the user manager
     */
    public static UserManager getUserManager() {
        return (UserManager) AppContext.getDataManager().getBinding(UserManager.BINDING_NAME);                
    }

    /**
     * Add a user to the set of logged in users
     */
    public void addUser(ClientSession session, UserMO user) {
        DataManager dm = AppContext.getDataManager();
        sessionToUser.put(dm.createReference(session), dm.createReference(user));
    }
    
    /**
     * Remove the user from the set of logged in users.
     *
     * @return reference to the UserGLO
     */
    public UserMO removeUser(ClientSession session) {
        DataManager dm = AppContext.getDataManager();
        
        ManagedReference<UserMO> userRef = 
                sessionToUser.remove(dm.createReference(session));
        if (userRef == null) {
            return null;
        }
        
        return userRef.get();
    }
    
    /**
     * Return the user with the given userID
     *
     * @return reference to the UserGLO
     */
    public UserMO getUser(ClientSession session) {
        DataManager dm = AppContext.getDataManager();
                
        ManagedReference<UserMO> userRef = 
                sessionToUser.get(dm.createReference(session));
        if (userRef == null) {
            return null;
        }
        
        return userRef.get();
    }
    
    /**
     * Return the UserMO object associated with the unique userName
     *
     * @return UserMO object for username, or null if no such user
     */
    public static UserMO getUserMO(String username) {
        String userObjName = "user_"+username;
        UserMO user=null;
        
        DataManager dataMgr = AppContext.getDataManager();
        try {
            user = (UserMO) dataMgr.getBinding(userObjName);
        } catch(NameNotBoundException ex) {
            user = null;
        }
        
        return user;
    }
    
    /**
     * Find or create and return the user managed object 
     */
//    public ManagedReference getOrCreateUserMO(String username) {
//        
//        String userObjName = "user_"+username;
//        UserMO user=null;
//        
//        user = getUserMO(username);
//        if (user==null) {
//            user = new UserMO(username);
//            AppContext.getDataManager().setBinding(userObjName, user);
//        }
//        
//        ManagedReference userRef = AppContext.getDataManager().createReference(user);
//                
//        return userRef;
//    }
    
    private UserMO createUserMO(String username) {
        UserMO ret = new UserMO(username);
        AppContext.getDataManager().setBinding("user_"+username, ret);
        return ret;
    }

    /**
     * Returns true if the user with the specified userName is currently logged in, false otherwise.
     */
    public boolean isLoggedIn(String userName) {
        UserMO user = getUserMO(userName);
        if (user==null) {
            return false;
        }
        
        return user.isLoggedIn();
    }
    
    /**
      * Return a Collection of all users currently logged in
     *
     * @return collection of ManagedReferences to UserGLO's
     */
    public Collection<ManagedReference<UserMO>> getAllUsers() {
        return sessionToUser.values();
    }
    
    /**
     * Log the user in from the specificed session. 
     * @param session 
     */
    public void login(ClientSession session) {
        DataManager dm = AppContext.getDataManager();
        
        // find the user object from the database, create it if necessary
        UserMO user = getUserMO(session.getName());
        if (user==null) {
            user = createUserMO(session.getName());
        }
        
        // user is now logged in
        user.login(session);
        
        // add this session to our map
        sessionToUser.put(dm.createReference(session), user.getReference());
    }
    
    /**
     * Log user out of s÷pecified session
     * @param session
     */
    public void logout(ClientSession session) {
        DataManager dm = AppContext.getDataManager();
        
        // make sure there is a user
        UserMO user = getUser(session);
        assert(user!=null);
        if (user != null) {
            user.logout(session);
        }
        
        sessionToUser.remove(dm.createReference(session));
   }
    
    /**
     * Return a Collection of all avatars for currently logged in users
     *
     * @return Collection of ManagedReferences to AvatarCellGLO's
     */
//    public Collection<ManagedReference> getAllAvatars() {
//        return uidToAvatarRef.values();
//    }
    
    /**
     *  Return total number of users currently logged in
     * 
     *  @return total number of users currently logged in
     **/
    public int getUserCount() {
        return sessionToUser.size();
    }
    
    /**
     *  Get the maximum number of users allowed on the server
     * 
     *  @return the maximum number of concurrent users this world will
     * allow to log in.
     */
    public int getUserLimit() {
        return userLimit;
    }

    /**
     *  Set the maximum number of users allowed on the server
     * 
     * @param userLimit the maximum number of concurrent users this world will
     * allow to log in.
     */
    public void setUserLimit(int userLimit) {
        this.userLimit = userLimit;
    }
    
}
