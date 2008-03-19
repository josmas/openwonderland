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
import com.sun.sgs.app.ClientSession;
import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.wonderland.common.cell.AvatarClientType;
import org.jdesktop.wonderland.common.cell.CellTransform;
import org.jdesktop.wonderland.common.cell.MultipleParentException;
import org.jdesktop.wonderland.common.cell.messages.AvatarCreateResponseMessage;
import org.jdesktop.wonderland.common.cell.messages.AvatarMessage;
import org.jdesktop.wonderland.common.comms.ClientType;
import org.jdesktop.wonderland.common.messages.ErrorMessage;
import org.jdesktop.wonderland.common.messages.Message;
import org.jdesktop.wonderland.server.UserMO;
import org.jdesktop.wonderland.server.WonderlandContext;
import org.jdesktop.wonderland.server.comms.ClientHandler;
import org.jdesktop.wonderland.server.comms.WonderlandClientSender;

/**
 * Handler for the avatar
 * @author paulby
 */
class AvatarClientHandler implements ClientHandler, Serializable {
    
    private static final Logger logger = Logger.getLogger(AvatarClientHandler.class.getName());
    
    protected static final ClientType CLIENT_TYPE =
            AvatarClientType.CLIENT_TYPE;
    
    public ClientType getClientType() {
        return CLIENT_TYPE;
    }

    public void registered(WonderlandClientSender sender) {
        // ignore
    }
    
    public void clientAttached(WonderlandClientSender sender,
                               ClientSession session)
    {
        logger.fine("AvatarClientHandler.clientAttached");
    }

    public void clientDetached(WonderlandClientSender sender,
                               ClientSession session)
    {
        logger.fine("AvatarClientHandler.clientDettached");
    }
    
    public void messageReceived(WonderlandClientSender sender,
                                ClientSession session, 
                                Message message) 
    {
        if (message instanceof AvatarMessage) {
            messageReceived(sender, session, (AvatarMessage) message);
        } else {
            sender.send(session, new ErrorMessage(message.getMessageID(),
                        "Unexpected message type: " + message.getClass()));
        }
    }
  
    /**
     * Handle reception of AvatarMessage
     * @param channel
     * @param sessionId
     * @param message
     */
    private void messageReceived(WonderlandClientSender sender,
                                ClientSession session, 
                                AvatarMessage message) 
    {
        switch(message.getActionType()) {
            case MOVE_REQUEST :
                AvatarMO avatar = (AvatarMO) CellManager.getCell(message.getCellID());
                AppContext.getDataManager().markForUpdate(avatar);
                avatar.setTransform(new CellTransform(message.getOrientation(), message.getLocation()));
                break;
            case MOVED :
                logger.warning("AvatarClientHandler.messageReceived MOVED - not implemnted");
                break;
            case CREATE :
                AvatarCreateResponseMessage response = createAvatar(session, message);
                sender.send(session, response);
                break;
        }
    }
    
    private AvatarCreateResponseMessage createAvatar(ClientSession session, AvatarMessage msg) {
        UserMO user = WonderlandContext.getUserManager().getUser(session);
        AvatarMO avatar = user.getAvatar(msg.getAvatarID());
        if (avatar == null) {
            user.getReference().getForUpdate(); // Mark for update
            avatar = new AvatarMO(user);
            user.putAvatar(msg.getAvatarID(), avatar);
        }

        try {
            // HACK !
            if (!avatar.isLive())
                WonderlandContext.getCellManager().addCell(avatar);

        } catch (MultipleParentException ex) {
            Logger.getLogger(AvatarClientHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return new AvatarCreateResponseMessage(msg.getMessageID(), avatar.getCellID());
    }
    
    /**
     * Get the channel used for sending to all clients of this type
     * @return the channel to send to all clients
     */
    public static WonderlandClientSender getSender() {
        return WonderlandContext.getCommsManager().getSender(CLIENT_TYPE);
    }
}
