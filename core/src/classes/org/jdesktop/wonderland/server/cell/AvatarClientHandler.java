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

import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import com.sun.sgs.app.AppContext;
import com.sun.sgs.app.ClientSessionId;
import com.sun.sgs.app.ManagedObject;
import com.sun.sgs.app.ManagedReference;
import java.io.Serializable;
import java.util.ArrayList;
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
import org.jdesktop.wonderland.server.comms.WonderlandClientChannel;

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

    public void registered(WonderlandClientChannel channel) {
        // ignore
    }
    
    public void clientAttached(WonderlandClientChannel channel,
                               ClientSessionId sessionId) {
        logger.fine("AvatarClientHandler.clientAttached");
    }

    public void clientDetached(WonderlandClientChannel channel,
                               ClientSessionId sessionId) {
        logger.fine("AvatarClientHandler.clientDettached");
    }
    
    public void messageReceived(WonderlandClientChannel channel,
                                ClientSessionId sessionId, 
                                Message message) {
        if (message instanceof AvatarMessage) {
            messageReceived(channel, sessionId, (AvatarMessage) message);
        } else {
            channel.send(new ErrorMessage(message.getMessageID(),
                         "Unexpected message type: " + message.getClass()));
        }
    }
  
    /**
     * Handle reception of AvatarMessage
     * @param channel
     * @param sessionId
     * @param message
     */
    private void messageReceived(WonderlandClientChannel channel,
                                ClientSessionId sessionId, 
                                AvatarMessage message) {
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
                AvatarCreateResponseMessage response = createAvatar(sessionId, message);
                channel.send(sessionId, response);
                break;
        }
    }
    
    private AvatarCreateResponseMessage createAvatar(ClientSessionId sessionId, AvatarMessage msg) {
        UserMO user = WonderlandContext.getUserManager().getUser(sessionId).get(UserMO.class);
        ManagedReference avatarRef = user.getAvatar(msg.getAvatarID());
        AvatarMO avatar;
        if (avatarRef == null) {
            user.getReference().getForUpdate(UserMO.class); // Mark for update
            avatar = new AvatarMO(user);
            user.putAvatar(msg.getAvatarID(), AppContext.getDataManager().createReference(avatar));
        } else {
            avatar = avatarRef.get(AvatarMO.class);
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
    public static WonderlandClientChannel getChannel() {
        return WonderlandContext.getCommsManager().getChannel(CLIENT_TYPE);
    }
    
}
