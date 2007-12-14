/**
 * Project Looking Glass
 *
 * $RCSfile: UserManager.java,v $
 *
 * Copyright (c) 2004-2007, Sun Microsystems, Inc., All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * $Revision: 1.11 $
 * $Date: 2007/10/02 17:17:01 $
 * $State: Exp $
 */
package org.jdesktop.wonderland.server;

import com.sun.sgs.app.ClientSessionId;
import com.sun.sgs.app.AppContext;
import com.sun.sgs.app.ManagedObject;
import com.sun.sgs.app.ManagedReference;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;

/**
 * Manages the entire set of users logged into the system.
 *
 * In the future this should provide a spacial hierarchy of users
 * avatars so we can search for users who are close to each other.
 *
 * @author paulby
 */
public class UserManager implements ManagedObject, Serializable {
    
    private HashMap<ClientSessionId, ManagedReference> uidToUserRef =
	    new HashMap<ClientSessionId,ManagedReference>();
    
    private HashMap<ClientSessionId, ManagedReference> uidToAvatarRef =
	    new HashMap<ClientSessionId,ManagedReference>();
        
    /**
     * Name used in binding this object in DataManager
     **/
    static final String BINDING_NAME="USER_MANAGER";
    
    private int userLimit = Integer.MAX_VALUE;

    /**
     * Creates a new instance of UserManager
     */
    public UserManager() {
        AppContext.getDataManager().setBinding(BINDING_NAME, this);
    }

    /**
     * Add a user to the set of logged in users
     */
    public void addUser(ClientSessionId userID, ManagedReference userRef) {
        uidToUserRef.put(userID, userRef);
        uidToAvatarRef.put(userID, userRef.get(UserGLO.class).getAvatarCellRef());
//        System.out.println("Adding User "+userID+"  total users "+uidToUserRef.size());
    }
    
    /**
     * Remove the user from the set of logged in users.
     *
     * @return reference to the UserGLO
     */
    public ManagedReference removeUser(ClientSessionId userID) {
        uidToAvatarRef.remove(userID);
        ManagedReference userRef = uidToUserRef.remove(userID);
        return userRef;
    }
    
    /**
     * Return the user with the given userID
     *
     * @return reference to the UserGLO
     */
    public ManagedReference getUser(ClientSessionId userID) {
//        System.out.println("Get User "+userID+"  "+uidToUserRef.get(userID));
        return uidToUserRef.get(userID);
    }
    
    /**
     * Return the UserGLO object associated with the unique userName
     *
     * @return reference to the UserGLO
     */
    private ManagedReference getUser(String userName) {
        return UserGLO.getUserGLORef(userName);
    }
    
    /**
     * Returns true if the user with the specified userName is currently logged in, false otherwise.
     */
    public boolean isLoggedIn(String userName) {
        ManagedReference userRef = getUser(userName);
        if (userRef==null)
            return false;
        
        return userRef.get(UserGLO.class).isLoggedIn();
    }
    
    /**
      * Return a Collection of all users currently logged in
     *
     * @return collection of ManagedReferences to UserGLO's
     */
    public Collection<ManagedReference> getAllUsers() {
        return uidToUserRef.values();
    }
    
    /**
     * Return a Collection of all avatars for currently logged in users
     *
     * @return Collection of ManagedReferences to AvatarCellGLO's
     */
    public Collection<ManagedReference> getAllAvatars() {
        return uidToAvatarRef.values();
    }
    
    /**
     *  Return total number of users currently logged in
     **/
    public int getUserCount() {
        return uidToUserRef.size();
    }
    
    /**
     *  Get the maximum number of users allowed on the server
     */
    public int getUserLimit() {
        return userLimit;
    }

    /**
     *  Set the maximum number of users allowed on the server
     */
    public void setUserLimit(int userLimit) {
        this.userLimit = userLimit;
    }
    
}
