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
package org.jdesktop.wonderland.testharness.manager.common;

/**
 *
 * @author paulby
 */
public class SimpleTestDirectorMessage extends ManagerMessage {

    public enum MessageType { ADD_USER, UI_UPDATE, REQUEST_STATUS };
    
    private MessageType messageType;
    private int userCount;
    
    private SimpleTestDirectorMessage(MessageType messageType) {
        this.messageType = messageType;
    }
    
    public static SimpleTestDirectorMessage newUIUpdate(int userCount) {
        SimpleTestDirectorMessage ret = new SimpleTestDirectorMessage(MessageType.UI_UPDATE);
        ret.userCount = userCount;
        
        return ret;
    }
    
    public static SimpleTestDirectorMessage newAddUserMessage() {
        return new SimpleTestDirectorMessage(MessageType.ADD_USER);
    }
    
    public static SimpleTestDirectorMessage newRequestStatusMessage() {
        return new SimpleTestDirectorMessage(MessageType.REQUEST_STATUS);
    }
    

    public MessageType getMessageType() {
        return messageType;
    }
    
    public int getUserCount() {
        assert(messageType==MessageType.UI_UPDATE);
        
        return userCount;
    }
    
    public String toString() {
        return "SimpleTestDirectorMessage:"+messageType;
    }
}
