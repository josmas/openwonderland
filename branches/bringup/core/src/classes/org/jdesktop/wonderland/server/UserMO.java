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
import com.sun.sgs.app.ManagedObject;
import com.sun.sgs.app.ManagedReference;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Logger;

/**
 * This class represensents a real world user. A user can be logged into
 * the system from multiple concurrent clients with different protocols
 * 
 * For example a user may be logged in from a 3D client and a cell phone
 * 
 * @author paulby
 */
public class UserMO implements ManagedObject, Serializable {

    private String username;
    private String fullname;
    private ArrayList<String> groups = null;
    
    private HashMap<ClientSession, ProtocolSessionListener> activeSessions = null;
    private HashMap<String, Serializable> extendedData = null;
    
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
     * User has logged in from specified session with specificed protocol listener
     * @param session
     * @param protocol
     */
    void login(ClientSession session, ProtocolSessionListener protocolListener) {
        if (activeSessions==null) {
            activeSessions = new HashMap();
        }
        logger.info("User Login "+username+" with protocol "+protocolListener.getClass().getCanonicalName());
        activeSessions.put(session, protocolListener);
    }
    
    /**
     * User has logged out from specified session
     * @param session
     * @param protocol
     */
    void logout(ClientSession session, ProtocolSessionListener protocolListener) {
        activeSessions.remove(session);
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
}
