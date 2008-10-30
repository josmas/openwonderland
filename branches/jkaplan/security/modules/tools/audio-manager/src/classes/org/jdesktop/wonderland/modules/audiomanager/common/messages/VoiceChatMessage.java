/**
 * Project Looking Glass
 *
 * $RCSfile: SoftphoneMessage.java,v $
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
 * $Revision: 1.9 $
 * $Date: 2008/06/10 20:27:18 $
 * $State: Exp $
 */

package org.jdesktop.wonderland.modules.audiomanager.common.messages;

import org.jdesktop.wonderland.common.messages.Message;

import org.jdesktop.wonderland.common.cell.CellID;

/**
 *
 * @author joe provino
 */
public class VoiceChatMessage extends Message {
    
    public enum ActionType { 
	JOIN, 
	LEAVE,
	END,
	REQUEST_TO_JOIN,
	BUSY,
	GET_CHAT_INFO_REQUEST,
	GET_CHAT_INFO_RESPONSE
    }
    
    public enum ChatType {
	SECRET,
	PRIVATE,
	PUBLIC
    }

    private ActionType actionType;
    
    private CellID cellID;
    private String group;
    private String calleeList;
    private String caller;
    private ChatType chatType;
    private String chatInfo;

    /**
     * Default constructor for 'deserialization'
     */
    public VoiceChatMessage() {
    }

    public VoiceChatMessage(ActionType actionType, CellID cellID) {
	if (actionType != ActionType.GET_CHAT_INFO_REQUEST) {
	    System.out.println("Invalid action type:  " + actionType);
	}

	this.cellID = cellID;
	this.actionType = actionType;
    }

    public VoiceChatMessage(ActionType actionType, CellID cellID, String group) {
	if (actionType != ActionType.GET_CHAT_INFO_REQUEST && 
		actionType != ActionType.GET_CHAT_INFO_RESPONSE) {

	    System.out.println("Invalid action type:  " + actionType);
	}

	this.cellID = cellID;
	this.actionType = actionType;
	this.group = group;
    }

    /*
     * End group
     */
    public VoiceChatMessage(CellID cellID, String group) {
	actionType = ActionType.END;
	this.cellID = cellID;
	this.group = group;
    }

    /*
     * Leave group
     */
    public VoiceChatMessage(ActionType actionType, CellID cellID, String group, 
	    String caller) {

        this.actionType = actionType;
	this.cellID = cellID;
	this.group = group;
	this.caller = caller;
    }
	 
    public VoiceChatMessage(ActionType actionType, CellID cellID, String group, 
	    String caller, String calleeList, ChatType chatType) {

        this.actionType = actionType;
	this.cellID = cellID;
	this.group = group;
	this.caller = caller;
	this.calleeList = calleeList;
	this.chatType = chatType;
    }
    
    public ActionType getActionType() {
        return actionType;
    }
    
    public void setCellID(CellID cellID) {
        this.cellID = cellID;
    }

    public CellID getCellID() {
        return cellID;
    }

    public String getGroup() {
	return group;
    }

    public String getCaller() {
	return caller;
    }

    public String getCalleeList() {
	return calleeList;
    }
    
    public void setChatInfo(String chatInfo) {
	this.chatInfo = chatInfo;
    }

    public ChatType getChatType() {
	return chatType;
    }

    public String getChatInfo() {
	return chatInfo;
    }

}
