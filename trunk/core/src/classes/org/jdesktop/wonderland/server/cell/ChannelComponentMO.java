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
package org.jdesktop.wonderland.server.cell;

import com.sun.sgs.app.Channel;
import com.sun.sgs.app.ManagedObject;
import com.sun.sgs.app.ManagedReference;
import java.io.Serializable;
import java.util.HashMap;
import org.jdesktop.wonderland.common.cell.messages.CellMessage;
import org.jdesktop.wonderland.server.comms.WonderlandClientID;
import org.jdesktop.wonderland.server.comms.WonderlandClientSender;

/**
 *
 * @author paulby
 */
public abstract class ChannelComponentMO extends CellComponentMO {

    private WonderlandClientSender cellSender;
    private ManagedReference<Channel> cellChannelRef;
    private HashMap<Class, ManagedReference<ComponentMessageReceiver>> messageReceivers = new HashMap();
    
    public ChannelComponentMO(CellMO cell) {
        super(cell);
    }
        
    /**
     * Send message to all clients on this channel
     * @param senderID the id of the sender session, or null if this
     * message being sent by the server
     * @param message
     *
     */
    public abstract void sendAll(WonderlandClientID senderID, CellMessage message);
    
    /**
     * Add user to the cells channel, if there is no channel simply return
     * @param userID
     */
    public abstract void addUserToCellChannel(WonderlandClientID clientID);
    
    /**
     * Remove user from the cells channel
     * @param userID
     */
    public abstract void removeUserFromCellChannel(WonderlandClientID clientID);
     
    /**
     * Register a receiver for a specific message class. Only a single receiver
     * is allowed for each message class, calling this method to add a duplicate
     * receiver will cause an IllegalStateException to be thrown.
     * 
     * @param msgClass
     * @param receiver
     */
    public abstract void addMessageReceiver(Class<? extends CellMessage> msgClass, ComponentMessageReceiver receiver);

    public abstract void removeMessageReceiver(Class<? extends CellMessage> msgClass);
    
    @Override
    protected Class getLookupClass() {
        return ChannelComponentMO.class;
    }

    abstract void messageReceived(WonderlandClientSender sender,
                                WonderlandClientID clientID,
                                CellMessage message );


    static public interface ComponentMessageReceiver extends ManagedObject, Serializable {
        public void messageReceived(WonderlandClientSender sender, 
                                    WonderlandClientID clientID,
                                    CellMessage message );        
    }
}
