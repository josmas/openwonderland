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
package org.jdesktop.wonderland.modules.clientmonitor.server.cell;

import org.jdesktop.wonderland.server.cell.*;
import com.sun.sgs.app.AppContext;
import com.sun.sgs.app.ManagedObject;
import com.sun.sgs.app.ManagedReference;
import java.util.logging.Logger;
import org.jdesktop.wonderland.common.cell.CellTransform;
import org.jdesktop.wonderland.common.cell.messages.CellMessage;
import org.jdesktop.wonderland.modules.clientmonitor.common.cell.messages.ClientMonitorMessage;
import org.jdesktop.wonderland.common.cell.messages.MovableMessage;
import org.jdesktop.wonderland.server.cell.ChannelComponentMO.ComponentMessageReceiver;
import org.jdesktop.wonderland.server.cell.annotation.UsesCellComponentMO;
import org.jdesktop.wonderland.server.comms.WonderlandClientID;
import org.jdesktop.wonderland.server.comms.WonderlandClientSender;
import org.jdesktop.wonderland.server.eventrecorder.RecorderManager;

/**
 *
 * @author paulby
 */
public class ClientMonitorComponentMO extends CellComponentMO {

    @UsesCellComponentMO(ChannelComponentMO.class)
    protected ManagedReference<ChannelComponentMO> channelComponentRef = null;
    
    /**
     * @param cell
     */
    public ClientMonitorComponentMO(CellMO cell) {
        super(cell);        
    }
    
    @Override
    public void setLive(boolean live) {
        if (live) {
            channelComponentRef.getForUpdate().addMessageReceiver(getMessageClass(), new ComponentMessageReceiverImpl(this));
        } else {
            channelComponentRef.getForUpdate().removeMessageReceiver(getMessageClass());
        }
    }

    protected Class getMessageClass() {
        return ClientMonitorMessage.class;
    }
    
    @Override
    protected String getClientClass() {
        // Return null for the client class as we only want to install the 
        // client component for the avatars which are selected for input
        return null;
//        return "org.jdesktop.wonderland.client.cell.ClientMonitorComponent";
    }


    private static class ComponentMessageReceiverImpl implements ComponentMessageReceiver {

        private ManagedReference<ClientMonitorComponentMO> compRef;
        
        public ComponentMessageReceiverImpl(ClientMonitorComponentMO comp) {
            compRef = AppContext.getDataManager().createReference(comp);
        }

        public void messageReceived(WonderlandClientSender sender, WonderlandClientID clientID, CellMessage message) {
            ClientMonitorMessage monitorMessage = (ClientMonitorMessage) message;
            System.err.println("MONITOR MSG "+monitorMessage);
        }

         /**
         * Record the message -- part of the event recording mechanism.
         * Nothing more than the message is recorded in this implementation, delegate it to the recorder manager
         */
        public void recordMessage(WonderlandClientSender sender, WonderlandClientID clientID, CellMessage message) {
            // No need to record these messages
        }
    }
}
