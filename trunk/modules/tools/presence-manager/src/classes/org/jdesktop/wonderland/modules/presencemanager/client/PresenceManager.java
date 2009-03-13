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

import java.math.BigInteger;

import org.jdesktop.wonderland.common.auth.WonderlandIdentity;

import org.jdesktop.wonderland.common.cell.CellID;

import org.jdesktop.wonderland.modules.presencemanager.common.PresenceInfo;

import com.jme.bounding.BoundingBox;
import com.jme.bounding.BoundingSphere;
import com.jme.bounding.BoundingVolume;

/**
* Presence API.  This interfaces defines methods for mapping between
* CellID, WonderlandIdentity and SessionID.
*/
public interface PresenceManager {

    /**
     * Tell the PresenceManager about a new session.
     * @param CellID the cell ID of the ViewCell.
     * @param BigInteger the Wonderland session ID
     * @param WonderlandIdentity the user ID
     */
    public void addSession(CellID cellID, BigInteger sessionID, WonderlandIdentity userID);

    /**
     * Tell the PresenceManager that a session has been removed.
     * @param CellID the cell ID of the ViewCell.
     * @param BigInteger the Wonderland session ID
     * @param WonderlandIdentity the user ID
     */
    public void removeSession(CellID cellID, BigInteger sessionID, WonderlandIdentity userID);

    /**
     * Get a WonderlandIdentity associated with a Wonderland session.
     * @param BigInteger the Wonderland session ID
     * @return WonderlandIdentity the WonderlandIdentity associated with the sessionID.
     */
    public WonderlandIdentity getUserID(BigInteger sessionID);
 
    /**
     * Get a WonderlandIdentity from a cellID.  The cellID must be for a ViewCell.
     * @param CellID the CellID of the ViewCell
     * @return WonderlandIdentity the WonderlandIdentity assoicated with the CellID.
     */
    public WonderlandIdentity getUserID(CellID cellID) throws IllegalArgumentException;
 
    /**
     * Get a Wonderland session ID from a WonderlandIdentity.
     * @param WonderlandIdentity the WonderlandIdentity
     * @return BigInteger the Wonderland session ID associated with the WonderlandIdentity.
     */
    public BigInteger getSessionID(WonderlandIdentity userID);
 
    /**
     * Get a Wonderland session ID from a cellID.  The cellID must be for a ViewCell.
     * @param CellID the CellID of the ViewCell.
     * @ return BigInteger the Wonderland session ID associated with the CellID.
     */
    public BigInteger getSessionID(CellID cellID) throws IllegalArgumentException;
 
    /**
     * Get a CellID from a WonderlandIdentity.
     *@param WonderlandIdentity the WonderlandIdentity
     * @return CellID the CellID associated with the WonderlandIdentity.     */
    public CellID getCellID(WonderlandIdentity userID);
 
    /**
     * Get a CellID from a Wonderland session ID.
     * @param BigInteger the Wonderland session ID
     * @return CellID the cellID associated with the Wonderland session ID.
     */
    public CellID getCellID(BigInteger sessionID);
 
    /**
     * Get the WonderlandIdentity list of cells in range of the specified cellID.
     * @param CellID the CellID of the requestor
     * @param BoundingVolume The BoundingBox or BoundingSphere specifying the range.
     */
    public WonderlandIdentity[] getUsersInRange(CellID cellID, BoundingVolume bounds);
 
    /**
     * Get the WonderlandIdentity list of all users.
     */
    public WonderlandIdentity[] getAllUsers();

    /**
     * Get PresenceInfo for a given username.  If there is more than one user
     * with the username, all of them are returned;
     */
    public PresenceInfo[] getPresenceInfo(String username);

    /**
     * Get the PresenceInfo for the given CellID.
     */
    public PresenceInfo getPresenceInfo(CellID cellID);

    /**
     * Listener for changes
     * @param PresenceManagerListener the listener to be notified of a change
     */
    public void addPresenceManagerListener(PresenceManagerListener listener);
 
    /**
     * Remove Listener for changes
     * @param PresenceManagerListener the listener to be removed
     */
    public void removePresenceManagerListener(PresenceManagerListener listener);
 
 }
 
