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

import com.sun.sgs.app.AppContext;
import com.sun.sgs.app.ClientSessionId;
import com.sun.sgs.app.ManagedReference;
import java.io.Serializable;
import java.util.logging.Logger;
import org.jdesktop.wonderland.common.cell.CellCacheClientType;
import org.jdesktop.wonderland.common.cell.messages.CellHierarchyMessage;
import org.jdesktop.wonderland.common.comms.ClientType;
import org.jdesktop.wonderland.common.messages.ErrorMessage;
import org.jdesktop.wonderland.common.messages.Message;
import org.jdesktop.wonderland.server.UserMO;
import org.jdesktop.wonderland.server.WonderlandContext;
import org.jdesktop.wonderland.server.comms.ClientHandler;
import org.jdesktop.wonderland.server.comms.WonderlandClientChannel;

/**
 * Handler for the cell cache
 * @author paulby
 */
class CellCacheClientHandler implements ClientHandler, Serializable {
    
    private static final String DEFAULT_AVATAR="DEFAULT";
    private static final Logger logger = Logger.getLogger(CellCacheClientHandler.class.getName());
    
    protected static final ClientType CLIENT_TYPE =
            CellCacheClientType.CLIENT_TYPE;
    
    public ClientType getClientType() {
        return CLIENT_TYPE;
    }

    public void registered(WonderlandClientChannel channel) {
        // ignore
    }
    
    public void clientAttached(WonderlandClientChannel channel,
                               ClientSessionId sessionId)
    {
        // Get the avatar and AvatarCellCache. Notify the cache
        // that the avatar is active (logged in)
        System.out.println("********* CellCacheClientHandler.clientAttached");
        UserMO user = WonderlandContext.getUserManager().getUser(sessionId).get(UserMO.class);
        ManagedReference avatarRef = user.getAvatar(DEFAULT_AVATAR);
        AvatarMO avatar;
        if (avatarRef==null) {
            user.getReference().getForUpdate(UserMO.class); // Mark for update
            avatar = new AvatarMO(user);
            user.putAvatar(DEFAULT_AVATAR, AppContext.getDataManager().createReference(avatar));
        } else {
            avatar = avatarRef.get(AvatarMO.class);
        }
        
        avatar.getCellCache().login(channel, sessionId);
    }

    public void clientDetached(WonderlandClientChannel channel,
                               ClientSessionId sessionId) 
    {
        UserMO user = WonderlandContext.getUserManager().getUser(sessionId).get(UserMO.class);
        ManagedReference avatarRef = user.getAvatar(DEFAULT_AVATAR);
        if (avatarRef==null) {
            logger.severe("clientDetached has null avatar for session");
            return;
        }
        avatarRef.getForUpdate(AvatarMO.class).getCellCache().logout(sessionId);
    }
    
    public void messageReceived(WonderlandClientChannel channel,
                                ClientSessionId sessionId, 
                                Message message)
    {
        if (message instanceof CellHierarchyMessage) {
            messageReceived(channel, sessionId, (CellHierarchyMessage) message);
        } else {
            channel.send(sessionId, new ErrorMessage(message.getMessageID(),
                         "Unexpected message type: " + message.getClass()));
        }
    }
  
    /**
     * When a cell message is received, dispatch it to the appropriate cell.
     * If the cell does not exist, send back an error message.
     * @param message the cell message
     * @param sender the message sender to send responses to
     */
    public void messageReceived(WonderlandClientChannel channel,
                                ClientSessionId sessionId,
                                CellHierarchyMessage message)
    {
        // get the MasterCellCache
        MasterCellCache mcc = WonderlandContext.getMasterCellCache();
        
        System.out.println("CellCacheHandler.messageReceived");
    }
    
    /**
     * Get the channel used for sending to all clients of this type
     * @return the channel to send to all clients
     */
    public static WonderlandClientChannel getChannel() {
        return WonderlandContext.getCommsManager().getChannel(CLIENT_TYPE);
    }
}
