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

import org.jdesktop.wonderland.client.softphone.SoftphoneControlImpl;

import org.jdesktop.wonderland.common.auth.WonderlandIdentity;

import org.jdesktop.wonderland.common.cell.CellID;

import org.jdesktop.wonderland.modules.presencemanager.common.PresenceInfo;

import com.jme.bounding.BoundingBox;
import com.jme.bounding.BoundingSphere;
import com.jme.bounding.BoundingVolume;

public class PresenceManagerImpl implements PresenceManager {

    private static final Logger logger = Logger.getLogger(PresenceManagerImpl.class.getName());

    private HashMap<CellID, PresenceInfo> cellIDMap = new HashMap();

    private HashMap<BigInteger, PresenceInfo> sessionIDMap = new HashMap();

    private HashMap<WonderlandIdentity, PresenceInfo> userIDMap = new HashMap();

    private HashMap<String, PresenceInfo> callIDMap = new HashMap();

    private ArrayList<PresenceManagerListener> listeners = new ArrayList();

    private WonderlandSession session;

    public PresenceManagerImpl(WonderlandSession session) {
	this.session = session;
    }

    public void addSession(PresenceInfo presenceInfo) {
	synchronized (cellIDMap) {
	    synchronized (sessionIDMap) {
		synchronized (userIDMap) {
		    synchronized (callIDMap) {
			if (alreadyInMaps(presenceInfo) == false) {
			    addPresenceInfo(presenceInfo);
			}
		    }
	        }
	    }
	}

	notifyListeners(presenceInfo, true);
    }

    private void addPresenceInfo(PresenceInfo presenceInfo) {
	PresenceInfo info = cellIDMap.get(presenceInfo.cellID);

	if (info != null && info.equals(presenceInfo) == false) {
	    logger.warning("cellIDMap already has entry for " + info);
	}

	cellIDMap.put(presenceInfo.cellID, presenceInfo);

	info = sessionIDMap.get(presenceInfo.clientID);

	if (info != null && info.equals(presenceInfo) == false) {
	    logger.warning("sessionIDMap already has entry for " + info);
	}

	sessionIDMap.put(presenceInfo.clientID, presenceInfo);

	info = userIDMap.get(presenceInfo.userID);

	if (info != null && info.equals(presenceInfo) == false) {
	    logger.warning("userIDMap already has entry for " + info);
	}

	userIDMap.put(presenceInfo.userID, presenceInfo);

	info = callIDMap.get(presenceInfo.callID);

	if (info != null && info.equals(presenceInfo) == false) {
	    logger.warning("callIDMap already has entry for " + info);
	}

	callIDMap.put(presenceInfo.callID, presenceInfo);
    }

    private boolean alreadyInMaps(PresenceInfo presenceInfo) {
	PresenceInfo info = cellIDMap.get(presenceInfo.cellID);

	if (info == null || info.equals(presenceInfo) == false) {
	    return false;
	}

	info = sessionIDMap.get(presenceInfo.clientID);

	if (info == null || info.equals(presenceInfo) == false) {
	    return false;
	}

	info = userIDMap.get(presenceInfo.userID);

	if (info == null || info.equals(presenceInfo) == false) {
	    return false;
	}

	info = callIDMap.get(presenceInfo.callID);

	if (info == null || info.equals(presenceInfo) == false) {
	    return false;
	}

	return true;
    }

    private void notifyListeners(PresenceInfo presenceInfo, boolean added) {
	/*
	 * Notify listeners
	 */
	PresenceManagerListener[] listenerArray;

	synchronized (listeners) {
	    listenerArray = this.listeners.toArray(new PresenceManagerListener[0]);
	}

	for (int i = 0; i < listenerArray.length; i++) {
	    if (added) {
	        listenerArray[i].userAdded(presenceInfo);
	    } else {
	        listenerArray[i].userRemoved(presenceInfo);
	    }
	}
    }

    public void removeSession(PresenceInfo presenceInfo) {
	synchronized (cellIDMap) {
	    synchronized (sessionIDMap) {
		synchronized (userIDMap) {
		    synchronized (callIDMap) {
	                cellIDMap.remove(presenceInfo.cellID);
	    	        sessionIDMap.remove(presenceInfo.clientID);
	    	        userIDMap.remove(presenceInfo.userID);
			callIDMap.remove(presenceInfo.callID);
		    }
		}
	    }
	}

	notifyListeners(presenceInfo, false);
    }

    /**
     * Get PresenceInfo from a cellID.  The cellID must be for a ViewCell.
     * @param CellID the CellID of the ViewCell
     * @return PresenceInfo the PresenceInfo assoicated with the CellID.
     */
    public PresenceInfo getPresenceInfo(CellID cellID) {
	synchronized (cellIDMap) {
	    PresenceInfo info = cellIDMap.get(cellID);

	    if (info == null) {
		return null;
	    }

	    return info;
	}
    }

    /**
     * Get PresenceInfo from a Wonderland sessionID.
     * @param BigInteger the Wonderland sessionID
     * @return PresenceInfo PresenceInfo associated with the sessionID.
     */
    public PresenceInfo getPresenceInfo(BigInteger sessionID) {
	synchronized (sessionIDMap) {
	    PresenceInfo info = sessionIDMap.get(sessionID);

	    if (info == null) {
		return null;
	    }

	    return info;
	}
    }

    /**
     * Get PresenceInfo from a WonderlandIdentity.
     * @param WonderlandIdentity userID
     * @return PresenceInfo PresenceInfo associated with the WonderlandIdentity.
    public PresenceInfo getPresenceInfo(WonderlandIdentity userID) {
	synchronized (userIDMap) {
	    PresenceInfo info = userIDMap.get(userID);

	    if (info == null) {
		return null;
	    }

	    return info;
	}
    }

    /**
     * Get PresenceInfo from a callID.
     * @param String callID
     * @return PresenceInfo the PresenceInfo associated with the callID.
     */
    public PresenceInfo getPresenceInfo(String callID) {
	synchronized (callIDMap) {
	    PresenceInfo info = callIDMap.get(callID);

	    if (info == null) {
	  	return null;
	    }

	    return info;
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
    public PresenceInfo[] getUserPresenceInfo(String username) {
	WonderlandIdentity[] users;

	synchronized (userIDMap) {
	    users = userIDMap.keySet().toArray(new WonderlandIdentity[0]);
	}

	ArrayList<PresenceInfo> userList = new ArrayList();

	for (int i = 0; i < users.length; i++) {
	    if (users[i].getUsername().equals(username) == false) {
		continue;
	    }

	    PresenceInfo info = userIDMap.get(users[i]);

	    if (info == null) {
		logger.warning("userIDMap does not have an entry for " + users[i]);
		return null;
	    }

	    userList.add(info);
	}

	if (userList.size() > 0) {
	    return userList.toArray(new PresenceInfo[0]);
	}

	return null;
    }

    /**
     * Listener for changes
     * @param PresenceManagerListener the listener to be notified of a change
     */
    public void addPresenceManagerListener(PresenceManagerListener listener) {
	PresenceInfo[] info;

	synchronized (listeners) {
	    if (listeners.contains(listener)) {
	        logger.warning("Listener is already added:  " + listener);
	        return;
	    }

	    listeners.add(listener);
	    info = cellIDMap.values().toArray(new PresenceInfo[0]);
	}

	for (int i = 0; i < info.length; i++) {
	    listener.userAdded(info[i]);
	}
    }

    /**
     * Remove Listener for changes
     * @param PresenceManagerListener the listener to be removed
     */
    public void removePresenceManagerListener(PresenceManagerListener listener) {
	synchronized (listeners) {
	    listeners.remove(listener);
	}
    }
    
}
