/**
 * Copyright (c) 2014, WonderBuilders, Inc., All Rights Reserved
 */
package org.jdesktop.wonderland.modules.avatarbase.common.cell;

import org.jdesktop.wonderland.common.cell.state.CellComponentClientState;

/**
 *
 * @author Abhishek Upadhyay
 */
public class NameTagComponentClientState extends CellComponentClientState {
    
    private boolean isMuted;

    public boolean isIsMuted() {
        return isMuted;
    }

    public void setIsMuted(boolean isMuted) {
        this.isMuted = isMuted;
    }
    
}
