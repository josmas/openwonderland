/**
 * Copyright (c) 2014, WonderBuilders, Inc., All Rights Reserved
 */
package org.jdesktop.wonderland.modules.avatarbase.common.cell;

import javax.xml.bind.annotation.XmlRootElement;
import org.jdesktop.wonderland.common.cell.state.CellComponentServerState;
import org.jdesktop.wonderland.common.cell.state.annotation.ServerState;

/**
 *
 * @author Abhishek Upadhyay
 */

@ServerState
@XmlRootElement(name="name-tag-component")
public class NameTagComponentServerState extends CellComponentServerState {
    
    private boolean isMuted;

    public boolean isIsMuted() {
        return isMuted;
    }

    public void setIsMuted(boolean isMuted) {
        this.isMuted = isMuted;
    }
    
    /**
     * Get the class name of the server CellComponentMO to instantiate
     */
    @Override
    public String getServerComponentClassName() {
        return "org.jdesktop.wonderland.modules.avatarbase.server.cell.NameTagComponentMO";
    }
    
}
