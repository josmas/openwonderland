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
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import org.jdesktop.wonderland.common.ExperimentalAPI;
import org.jdesktop.wonderland.server.cell.view.AvatarCellMO;

/**
 * This class represensents a real world user. A user can be logged into
 * the system from multiple concurrent clients with different protocols
 * 
 * For example a user may be logged in from a 3D client and a cell phone
 * 
 * @author paulby
 */
@ExperimentalAPI
public class UserMO implements ManagedObject, Serializable {

    private String username;
    private String fullname;
    private ArrayList<String> groups = null;
    
    private Set<ManagedReference<ClientSession>> activeSessions = null;
    private Map<String, Serializable> extendedData = null;
    private Map<ManagedReference<ClientSession>, Map<String, ManagedReference<AvatarCellMO>>> avatars = new HashMap();
    
    protected static Logger logger = Logger.getLogger(UserMO.class.getName());

    /**
     * Create a new User managed object with a unique username
     * 
     * @param username
     */
    UserMO(String username) {
        this.username = username;
    }
    
    /**
     * Get unique username
     * 
     * @return
     */
    public String getUsername() {
        return username;
    }

    /**
     * Get full name of user (may not be unique)
     * @return
     */
    public String getFullname() {
        return fullname;
    }

    /**
     * Set full name of user (may not be unique)
     */
    public void setFullname(String fullname) {
        this.fullname = fullname;
    }
    
    /**
     * Put a named object in the extended data Map for this User
     * 
     * This name/object pairing allows developers to add data to the user class
     * without needing to modify the userMO class.
     * 
     * @param name
     * @param object
     */
    public void putExtendedData(String name, Serializable object) {
        if (extendedData==null) {
            extendedData = new HashMap();
        }
        extendedData.put(name, object);
    }
    
    /**
     * Return the object associated with the name.
     * @param name
     * @return Object, or null if their is no object associated with the given name
     */
    public Object getExtendedData(String name) {
        if (extendedData==null)
            return null;
        return extendedData.get(name);
    }
    
    /**
     * Return the specified avatar for this User and session, or null if that avatar
     * does not exist
     * 
     * @param avatarName
     * @return
     */
    public AvatarCellMO getAvatar(ClientSession session, String avatarName) {
        Map<String, ManagedReference<AvatarCellMO>> sessionAvatars = 
                avatars.get(AppContext.getDataManager().createReference(session));
            
        if (sessionAvatars==null)
            return null;
        
        ManagedReference<AvatarCellMO> avatarRef = sessionAvatars.get(avatarName);
        if (avatarRef == null) {
            return null;
        }
        
        return avatarRef.get();
    }
    
    /**
     * Put the avatarRef and the name in the set of avatars for this user. Each
     * ClientSession can have a set of avatars.
     * 
     * @param avatarName
     * @param avatar
     */
    public void putAvatar(ClientSession session, String avatarName, AvatarCellMO avatar) {
        DataManager dm = AppContext.getDataManager();
        ManagedReference<ClientSession> sessionRef = AppContext.getDataManager().createReference(session);
        Map<String, ManagedReference<AvatarCellMO>> sessionAvatars = avatars.get(sessionRef);
        if (sessionAvatars==null) {
            sessionAvatars = new HashMap();
            avatars.put(sessionRef, sessionAvatars);
        }
        sessionAvatars.put(avatarName, dm.createReference(avatar));
    }
    
    /**
     * User has logged in from specified session with specificed protocol listener
     * @param session
     * @param protocol
     */
    void login(ClientSession session) {
        DataManager dm = AppContext.getDataManager();
                
        if (activeSessions==null) {
            activeSessions = new HashSet<ManagedReference<ClientSession>>();
        }
        
        logger.info("User Login " + username);
        activeSessions.add(dm.createReference(session));
    }
    
    /**
     * User has logged out from specified session
     * @param session
     * @param protocol
     */
    void logout(ClientSession session) {
        DataManager dm = AppContext.getDataManager();        
        activeSessions.remove(dm.createReference(session));
    }
    
    /**
     * Return true if this user is logged in, false otherwise
     * @return
     */
    boolean isLoggedIn() {
        return activeSessions.size()>0;
    }
    
    /**
     * Convenience method that returns the ManagedReference to this ManagedObject
     * @return
     */
    public ManagedReference getReference() {
        return AppContext.getDataManager().createReference(this);
    }
    
    /**
     * Get the user secutiry context associated with this user, or null
     * if there is no security context
     * @return
     */
    public UserSecurityContextMO getUserSecurityContext() {
        return null;
    }
}
