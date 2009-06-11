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
     * Tell the PresenceManager about new PresenceInfo.
     * @param CellID the cell ID of the ViewCell.
     * @param BigInteger the Wonderland session ID
     * @param WonderlandIdentity the user ID
     */
    public void addPresenceInfo(PresenceInfo presenceInfo);

    /**
     * Tell the PresenceManager that PresenceInfo has been removed.
     * @param CellID the cell ID of the ViewCell.
     * @param BigInteger the Wonderland session ID
     * @param WonderlandIdentity the user ID
     */
    public void removePresenceInfo(PresenceInfo presenceInfo);

    /**
     * Get PresenceInfo from a cellID.  The cellID must be for a ViewCell.
     * @param CellID the CellID of the ViewCell
     * @return PresenceInfo the PresenceInfo assoicated with the CellID.
     */
    public PresenceInfo getPresenceInfo(CellID cellID);

    /**
     * Get PresenceInfo from a Wonderland sessionID.
     * @param BigInteger the Wonderland sessionID
     * @return PresenceInfo PresenceInfo associated with the sessionID.
     */
    public PresenceInfo getPresenceInfo(BigInteger sessionID);

    /**
     * Get PresenceInfo from a WonderlandIdentity.
     * @param WonderlandIdentity userID
     * @return PresenceInfo PresenceInfo associated with the WonderlandIdentity.
    public PresenceInfo getPresenceInfo(WonderlandIdentity userID);

    /**
     * Get PresenceInfo from a callID.
     * @param String callID
     * @return PresenceInfo the PresenceInfo associated with the callID.
     */
    public PresenceInfo getPresenceInfo(String callID);

 
    /**
     * Get the WonderlandIdentity list of cells in range of the specified cellID.
     * @param CellID the CellID of the requestor
     * @param BoundingVolume The BoundingBox or BoundingSphere specifying the range.
     */
    public PresenceInfo[] getUsersInRange(CellID cellID, BoundingVolume bounds);
 
    /**
     * Get the WonderlandIdentity list of all users.
     */
    public PresenceInfo[] getAllUsers();

    /**
     * Get PresenceInfo for a given username.  If there is more than one user
     * with the username, all of them are returned;
     * @param String user name
     * @return PresenceInfo[] presence information for user.  Should be only one entry.
     */
    public PresenceInfo[] getUserPresenceInfo(String username);

    /**
     * Get PresenceInfo for a given username alias.  If there is more 
     * than one user with the username alias, all of them are returned;
     * @param String user name alias
     * @return PresenceInfo[] presence information for user.  
     */
    public PresenceInfo[] getAliasPresenceInfo(String usernameAlias);

    /**
     * Change usernameAlias in PresenceInfo.
     * @param PresenceInfo
     * @param String user name
     */
    public void changeUsernameAlias(PresenceInfo info);

    /** 
     * Set speaking flag
     * @param PresenceInfo
     * @param boolean
     */
    public void setSpeaking(PresenceInfo info, boolean isSpeaking);

    /** 
     * Set mute flag
     * @param PresenceInfo
     * @param boolean
     */
    public void setMute(PresenceInfo info, boolean isMuted);

    /** 
     * Set enteredConeOfSilence flag
     * @param PresenceInfo
     * @param boolean
     */
    public void setEnteredConeOfSilence(PresenceInfo info, boolean enteredConeOfSilence);

    /** 
     * Set inSecretChat flag
     * @param PresenceInfo
     * @param boolean
     */
    public void setInSecretChat(PresenceInfo info, boolean inSecretChat);

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
 
    /**
     * Display all presenceInfo
     */
    public void dump();

 }
 
