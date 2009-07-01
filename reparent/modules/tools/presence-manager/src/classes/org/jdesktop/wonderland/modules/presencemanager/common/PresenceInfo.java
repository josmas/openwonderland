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
package org.jdesktop.wonderland.modules.presencemanager.common;

import java.io.Serializable;

import java.math.BigInteger;

import org.jdesktop.wonderland.common.auth.WonderlandIdentity;

import org.jdesktop.wonderland.common.cell.CellID;

/**
 * Presence Information 
 * @author jprovino
 */
public class PresenceInfo implements Serializable {

    public CellID cellID;
    public BigInteger clientID;
    public WonderlandIdentity userID;
    public String callID;

    public boolean isSpeaking;
    public boolean isMuted;
    public boolean inConeOfSilence;
    public boolean inSecretChat;

    public String usernameAlias;

    public PresenceInfo(CellID cellID, BigInteger clientID, 
	    WonderlandIdentity userID, String callID) {

        this.cellID = cellID;
	this.clientID = clientID;
        this.userID = userID;
	this.callID = callID;

	usernameAlias = userID.getUsername();
    }

    public boolean equals(Object info) {
	PresenceInfo pi = (PresenceInfo) info;

	if (cellID == null) {
	    return callID.equals(pi.callID);
	}

	return cellID.equals(pi.cellID);
    }

    public Object clone() {
	return new PresenceInfo(cellID, clientID, userID, callID);
    }

    public String toString() {
	String s = "null";

	if (userID != null) {
	    s = userID.toString();
	}

        return "cellID=" + cellID + ", userID=" + s
            + ", clientID=" + clientID + ", callID=" + callID 
	    + ", alias=" + usernameAlias 
	    + ", isSpeaking " + isSpeaking
	    + ", isMuted " + isMuted
	    + ", inConeOfSilence " + inConeOfSilence
	    + ", inSecretChat " + inSecretChat;
    }

}
