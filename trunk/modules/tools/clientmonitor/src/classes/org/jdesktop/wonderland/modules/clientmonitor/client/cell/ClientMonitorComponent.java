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
package org.jdesktop.wonderland.modules.clientmonitor.client.cell;

import org.jdesktop.wonderland.client.cell.*;
import java.math.BigInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.wonderland.client.cell.annotation.UsesCellComponent;
import org.jdesktop.wonderland.common.ExperimentalAPI;
import org.jdesktop.wonderland.common.cell.CellStatus;
import org.jdesktop.wonderland.common.cell.messages.CellMessage;
import org.jdesktop.wonderland.modules.clientmonitor.common.cell.messages.ClientMonitorMessage;

/**
 * A component that provides cell movement
 * 
 * @author paulby
 */
@ExperimentalAPI
public class ClientMonitorComponent extends CellComponent {
    protected static Logger logger = Logger.getLogger(ClientMonitorComponent.class.getName());

    @UsesCellComponent
    protected ChannelComponent channelComp;
    
    protected ChannelComponent.ComponentMessageReceiver msgReceiver=null;

    private MonitorThread monitorThread;
    
    public ClientMonitorComponent(Cell cell) {
        super(cell);
    }
    
    
    @Override
    protected void setStatus(CellStatus status, boolean increasing) {
        super.setStatus(status, increasing);
        switch (status) {
            case INACTIVE:
                if (!increasing && msgReceiver != null && channelComp != null) {
                    channelComp.removeMessageReceiver(getMessageClass());
                    msgReceiver = null;
                }
                break;
            case ACTIVE: {
                if (increasing && msgReceiver == null) {
                    msgReceiver = new ChannelComponent.ComponentMessageReceiver() {

                        public void messageReceived(CellMessage message) {
                            // Ignore messages from this client, TODO move this up into addMessageReciever with an option to turn off the test
                            BigInteger senderID = message.getSenderID();
                            if (senderID == null) {
                                senderID = BigInteger.ZERO;
                            }
//                            if (!senderID.equals(cell.getCellCache().getSession().getID())) {
//                                serverMoveRequest((MovableMessage) message);
//                            }
                        }
                    };
                    channelComp.addMessageReceiver(getMessageClass(), msgReceiver);
                    monitorThread = new MonitorThread();
                }
            }
        }
    }

    /**
     * @return the class of the message this component handles.
     */
    protected Class getMessageClass() {
        return ClientMonitorMessage.class;
    }
    


    /**
     * Temporary throttle. TODO we should not have a thread for every MovableAvatarComponnet
     */
    class MonitorThread extends Thread {

        public MonitorThread() {
            // TODO REENABLE MONITOR
//            this.start();
        }

        @Override
        public void run() {
            while(true) {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException ex) {
                    Logger.getLogger(MovableAvatarComponent.class.getName()).log(Level.SEVERE, null, ex);
                }
                
                synchronized(this) {
                    ClientMonitorMessage msg = new ClientMonitorMessage();
                    channelComp.send(msg);
                }

            }
        }
    }
}
