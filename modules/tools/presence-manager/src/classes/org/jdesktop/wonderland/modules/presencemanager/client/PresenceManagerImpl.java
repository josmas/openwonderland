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
package org.jdesktop.wonderland.modules.presencemanager.client;

import java.util.ArrayList;
import java.util.HashMap;

import java.math.BigInteger;

import java.util.logging.Logger;

import org.jdesktop.wonderland.client.ClientContext;

import org.jdesktop.wonderland.client.cell.Cell;

import org.jdesktop.wonderland.client.comms.WonderlandSession;

import org.jdesktop.wonderland.common.auth.WonderlandIdentity;

import org.jdesktop.wonderland.common.cell.CellID;

import org.jdesktop.wonderland.modules.presencemanager.common.PresenceInfo;

import com.jme.bounding.BoundingBox;
import com.jme.bounding.BoundingSphere;
import com.jme.bounding.BoundingVolume;

public class PresenceManagerImpl implements PresenceManager {

    private static final Logger logger = Logger.getLogger(PresenceManagerImpl.class.getName());

    private class CellInfo {
	public BigInteger sessionID;
	public WonderlandIdentity userID;

	public CellInfo(BigInteger sessionID, WonderlandIdentity userID) {
	    this.sessionID = sessionID;
	    this.userID = userID;
	}

	public String toString() {
	    return "sessionID=" + sessionID + ", userID=" + userID;
	}
    }

    private class SessionInfo {
	public CellID cellID;
	public WonderlandIdentity userID;

	public SessionInfo(CellID cellID, WonderlandIdentity userID) {
	    this.cellID = cellID;
	    this.userID = userID;
	}

	public String toString() {
	    return "cellID=" + cellID + ", userID=" + userID;
	}
    }

    private class UserInfo {
	public CellID cellID;
	public BigInteger sessionID;

	public UserInfo(CellID cellID, BigInteger sessionID) {
	    this.cellID = cellID;
	    this.sessionID = sessionID;
	}

	public String toString() {
	    return "cellID=" + cellID + ", sessionID=" + sessionID;
	}
    }

    private HashMap<CellID, CellInfo> cellIDMap = new HashMap();

    private HashMap<BigInteger, SessionInfo> sessionIDMap = new HashMap();

    private HashMap<WonderlandIdentity, UserInfo> userIDMap = new HashMap();

    private ArrayList<PresenceManagerListener> listeners = new ArrayList();

    private WonderlandSession session;

    public PresenceManagerImpl(WonderlandSession session) {
	this.session = session;
    }

    public void addSession(CellID cellID, BigInteger sessionID, WonderlandIdentity userID) {
	synchronized (cellIDMap) {
	    synchronized(sessionIDMap) {
		synchronized(userIDMap) {
		    CellInfo cellInfo = cellIDMap.get(cellID);

		    if (cellInfo != null && cellInfo.sessionID.equals(sessionID) == false
			    && cellInfo.userID.equals(userID) == false) {

			logger.warning("cellIDMap already has entry for cellID " 
			    + cellID + ": " + cellInfo + " new sessionID=" + sessionID
			    + ", new userID=" + userID);
		    }

	    	    cellIDMap.put(cellID, new CellInfo(sessionID, userID));

		    SessionInfo sessionInfo = sessionIDMap.get(sessionID);

		    if (sessionInfo != null && sessionInfo.userID.equals(userID) == false
			    && sessionInfo.cellID.equals(cellID) == false) {

			logger.warning("sessionIDMap already has entry for sessionID "
			    + sessionID + ": " + sessionInfo + " new cellID=" + cellID
			    + ", new userID=" + userID);
		    }

	    	    sessionIDMap.put(sessionID, new SessionInfo(cellID, userID));

		    UserInfo userInfo = userIDMap.get(userID);

		    if (userInfo != null && userInfo.sessionID.equals(sessionID) == false
			    && userInfo.cellID.equals(cellID) == false) {

			logger.warning("userIDMap already has entry for userID "
			    + userID + ": " + userInfo + " new cellID=" + cellID
			    + ", new sessionID=" + sessionID);
		    }

	    	    userIDMap.put(userID, new UserInfo(cellID, sessionID));
		}
	    }
	}

	for (PresenceManagerListener listener : listeners) {
	    listener.userAdded(userID);
	}
    }

    public void removeSession(CellID cellID, BigInteger sessionID, 
	    WonderlandIdentity userID) {

	synchronized (cellIDMap) {
	    synchronized(sessionIDMap) {
		synchronized(userIDMap) {
	            cellIDMap.remove(cellID);
	    	    sessionIDMap.remove(sessionID);
	    	    userIDMap.remove(userID);
		}
	    }
	}

	for (PresenceManagerListener listener : listeners) {
	    listener.userRemoved(userID);
	}
    }

    /**
     * Get a WonderlandIdentity associated with a Wonderland session.
     * @param BigInteger the Wonderland session ID
     * @return WonderlandIdentity the WonderlandIdentity associated with the sessionID.
     */
    public WonderlandIdentity getUserID(BigInteger sessionID) {
	synchronized (sessionIDMap) {
	    SessionInfo info = sessionIDMap.get(sessionID);

	    if (info == null) {
		return null;
	    }

	    return info.userID;
	}
    }

    /**
     * Get a WonderlandIdentity from a cellID.  The cellID must be for a ViewCell.
     * @param CellID the CellID of the ViewCell
     * @return WonderlandIdentity the WonderlandIdentity assoicated with the CellID.
     */
    public WonderlandIdentity getUserID(CellID cellID) throws IllegalArgumentException {
	synchronized (cellIDMap) {
	    CellInfo info = cellIDMap.get(cellID);

	    if (info == null) {
		return null;
	    }

	    return info.userID;
	}
    }

    /**
     * Get a Wonderland session ID from a WonderlandIdentity.
     * @param WonderlandIdentity the WonderlandIdentity
     * @return BigInteger the Wonderland session ID associated with the WonderlandIdentity.
     */
    public BigInteger getSessionID(WonderlandIdentity userID) {
	synchronized (userIDMap) {
	    UserInfo info = userIDMap.get(userID);

	    if (info == null) {
		return null;
	    }

	    return info.sessionID;
	}
    }

    /**
     * Get a Wonderland session ID from a cellID.  The cellID must be for a ViewCell.
     * @param CellID the CellID of the ViewCell.
     * @ return BigInteger the Wonderland session ID associated with the CellID.
     */
    public BigInteger getSessionID(CellID cellID) throws IllegalArgumentException {
	synchronized (cellIDMap) {
	    CellInfo info = cellIDMap.get(cellID);

	    if (info == null) {
		return null;
	    }

	    return info.sessionID;
	}
    }

    /**
     * Get a CellID from a WonderlandIdentity.
     *@param WonderlandIdentity the WonderlandIdentity
     * @return CellID the CellID associated with the WonderlandIdentity.     
     */
    public CellID getCellID(WonderlandIdentity userID) {
	synchronized (userIDMap) {
	    UserInfo info = userIDMap.get(userID);

	    if (info == null) {
		return null;
	    }

	    return info.cellID;
	}
    }

    /**
     * Get a CellID from a Wonderland session ID.
     * @param BigInteger the Wonderland session ID
     * @return CellID the cellID associated with the Wonderland session ID.
     */
    public CellID getCellID(BigInteger sessionID) {
	synchronized (sessionIDMap) {
	    SessionInfo info = sessionIDMap.get(sessionID);

	    if (info == null) {
		return null;
	    }

	    return info.cellID;
	}
    }

    /**
     * Get the WonderlandIdentity list of cells in range of the specified cellID.
     * @param CellID the CellID of the requestor
     * @param BoundingVolume The BoundingBox or BoundingSphere specifying the range.
     * @return WonderlandIdentity[] the array of user ID's.
     */
    public WonderlandIdentity[] getUsersInRange(CellID cellID, BoundingVolume bounds) {
	// TODO:  Return only users in range.
	return getAllUsers();
    }

    /**
     * Get the ID's of all users.
     * @return WonderlandIdentity[] the array of user ID's.
     */
    public WonderlandIdentity[] getAllUsers() {
	synchronized (userIDMap) {
	    return userIDMap.keySet().toArray(new WonderlandIdentity[0]);
	}
    }
	
    /**
     * Get PresenceInfo for a given username.  If there is more than one user
     * with the username, all of them are returned;
     */
    public PresenceInfo[] getPresenceInfo(String username) {
	WonderlandIdentity[] users;

	synchronized (userIDMap) {
	    users = userIDMap.keySet().toArray(new WonderlandIdentity[0]);
	}

	ArrayList<PresenceInfo> userList = new ArrayList();

	for (int i = 0; i < users.length; i++) {
	    if (users[i].getUsername().equals(username) == false) {
		continue;
	    }

	    UserInfo info = userIDMap.get(users[i]);

	    if (info == null) {
		logger.warning("userIDMap does not have an entry for " + users[i]);
		return null;
	    }

	    userList.add(new PresenceInfo(info.sessionID, users[i], info.cellID));   
	}

	if (userList.size() > 0) {
	    return userList.toArray(new PresenceInfo[0]);
	}

	return null;
    }

    /**
     * Get the PresenceInfo for the given CellID.
     */
    public PresenceInfo getPresenceInfo(CellID cellID) {
	CellInfo info = cellIDMap.get(cellID);

	if (info == null) {
	    logger.warning("cellIDMap does not have an entry for " + cellID);
	    return null;
	}

	return new PresenceInfo(info.sessionID, info.userID, cellID);
    }

    /**
     * Get the PresenceInfo for the given WonderlandIdentity.
     */
    public PresenceInfo getPresenceInfo(WonderlandIdentity userID) {
	UserInfo info = userIDMap.get(userID);

	if (info == null) {
	    logger.warning("userIDMap does not have an entry for " + userID);
	    return null;
	}

	return new PresenceInfo(info.sessionID, userID, info.cellID);
    }

    /**
     * Listener for changes
     * @param PresenceManagerListener the listener to be notified of a change
     */
    public void addPresenceManagerListener(PresenceManagerListener listener) {
	if (listeners.contains(listener)) {
	    logger.warning("Listener is already added:  " + listener);
	    return;
	}

	listeners.add(listener);
    }

    /**
     * Remove Listener for changes
     * @param PresenceManagerListener the listener to be removed
     */
    public void removePresenceManagerListener(PresenceManagerListener listener) {
	listeners.remove(listener);
    }
    
}

