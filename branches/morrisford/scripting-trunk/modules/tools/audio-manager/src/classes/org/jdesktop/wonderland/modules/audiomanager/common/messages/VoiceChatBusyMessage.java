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

import org.jdesktop.wonderland.common.messages.Message;

/**
 *
 * @author jprovino
 */
public class VoiceChatBusyMessage extends VoiceChatMessage {
    
    private String caller;
    private String calleeList;
    private ChatType chatType;

    public VoiceChatBusyMessage(String group, String caller, String calleeList, 
	    ChatType chatType) {

	super(group);

	this.caller = caller;
	this.calleeList = calleeList;
	this.chatType = chatType;
    }
    
    public String getCaller() {
	return caller;
    }

    public String getCalleeList() {
	return calleeList;
    }
    
    public ChatType getChatType() {
	return chatType;
    }

}
