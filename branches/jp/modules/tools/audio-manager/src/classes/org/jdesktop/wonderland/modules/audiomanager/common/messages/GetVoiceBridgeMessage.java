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
package org.jdesktop.wonderland.modules.audiomanager.common.messages;

import org.jdesktop.wonderland.common.ExperimentalAPI;
import org.jdesktop.wonderland.common.comms.ProtocolVersion;

import org.jdesktop.wonderland.common.messages.Message;

/**
 * The initial message that a client must send to the Wonderland server
 * in order to specify a communications protocol to use.
 * @author jprovino
 */
@ExperimentalAPI
public class GetVoiceBridgeMessage extends Message {
    private String bridgeInfo;   // voice bridge information
    private String username;

    public GetVoiceBridgeMessage() {
    }

    public void setBridgeInfo(String bridgeInfo) {
	this.bridgeInfo = bridgeInfo;
    }

    public String getBridgeInfo() {
	return bridgeInfo;
    }

    public void setUsername(String username) {
	this.username = username;
    }

    public String getUsername() {
	return username;
    }

}
