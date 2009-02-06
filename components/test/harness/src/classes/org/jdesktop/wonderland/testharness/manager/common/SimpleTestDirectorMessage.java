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
package org.jdesktop.wonderland.testharness.manager.common;

import java.util.Properties;

/**
 *
 * @author paulby
 */
public class SimpleTestDirectorMessage extends ManagerMessage {

    public enum MessageType { USER_COUNT, UI_UPDATE, REQUEST_STATUS, CHANGE_ALLOCATOR };
    
    private MessageType messageType;
    private int userCount;
    private String allocatorName;
    private Properties props;

    private SimpleTestDirectorMessage(MessageType messageType) {
        this(messageType, -1);
    }

    private SimpleTestDirectorMessage(MessageType messageType, int userCount) {
        this.messageType = messageType;
        this.userCount = userCount;
    }
    
    public static SimpleTestDirectorMessage newUIUpdate(int userCount) {
        return new SimpleTestDirectorMessage(MessageType.UI_UPDATE, userCount);
    }
    
    public static SimpleTestDirectorMessage newUserCountMessage(int userCount) {
        return new SimpleTestDirectorMessage(MessageType.USER_COUNT, userCount);
    }
    
    public static SimpleTestDirectorMessage newRequestStatusMessage() {
        return new SimpleTestDirectorMessage(MessageType.REQUEST_STATUS);
    }

    public static SimpleTestDirectorMessage newChangeAllocatorMessage(
            String allocatorName, Properties props)
    {
        SimpleTestDirectorMessage out =
                new SimpleTestDirectorMessage(MessageType.CHANGE_ALLOCATOR);
        out.allocatorName = allocatorName;
        out.props = props;

        return out;
    }


    public MessageType getMessageType() {
        return messageType;
    }
    
    public int getUserCount() {
        assert(messageType!=MessageType.REQUEST_STATUS);
        
        return userCount;
    }

    public String getAllocatorName() {
        return allocatorName;
    }

    public Properties getProperties() {
        return props;
    }

    public String toString() {
        return "SimpleTestDirectorMessage:"+messageType;
    }
}
