/**
 * Copyright (c) 2014, WonderBuilders, Inc., All Rights Reserved
 */
package org.jdesktop.wonderland.modules.avatarbase.common.cell.messages;

import org.jdesktop.wonderland.common.cell.messages.CellMessage;

/**
 *
 * @author Abhishek Upadhyay
 */
public class NameTagMessage extends CellMessage {
    
    private boolean isMute;
    private String username;

    public boolean isIsMute() {
        return isMute;
    }

    public void setIsMute(boolean isMute) {
        this.isMute = isMute;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
    
}
