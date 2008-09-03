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

import org.jdesktop.wonderland.server.cell.view.AvatarCellMO;
import com.sun.sgs.app.ClientSession;
import java.io.Serializable;
import java.util.Properties;
import java.util.logging.Logger;
import org.jdesktop.wonderland.common.InternalAPI;
import org.jdesktop.wonderland.common.cell.CellCacheConnectionType;
import org.jdesktop.wonderland.common.cell.MultipleParentException;
import org.jdesktop.wonderland.common.cell.messages.ViewCreateResponseMessage;
import org.jdesktop.wonderland.common.cell.messages.CellHierarchyMessage;
import org.jdesktop.wonderland.common.comms.ConnectionType;
import org.jdesktop.wonderland.common.messages.ErrorMessage;
import org.jdesktop.wonderland.common.messages.Message;
import org.jdesktop.wonderland.server.UserMO;
import org.jdesktop.wonderland.server.WonderlandContext;
import org.jdesktop.wonderland.server.comms.ClientConnectionHandler;
import org.jdesktop.wonderland.server.comms.WonderlandClientSender;

/**
 * Handler for the cell cache connections
 * @author paulby
 */
@InternalAPI
class CellCacheConnectionHandler implements ClientConnectionHandler, Serializable {
    
    private  String viewID=null;
    private static final Logger logger = Logger.getLogger(CellCacheConnectionHandler.class.getName());
    
    protected static final ConnectionType CLIENT_TYPE =
            CellCacheConnectionType.CLIENT_TYPE;
    
    public ConnectionType getConnectionType() {
        return CLIENT_TYPE;
    }

    public void registered(WonderlandClientSender sender) {
        // ignore
    }
    
    public void clientConnected(WonderlandClientSender sender,
                                ClientSession session,
                                Properties properties)   
    {
        // Nothing to do, setup is done when we get the SET_VIEW
        // message
    }

    public void clientDisconnected(WonderlandClientSender sender,
                               ClientSession session) 
    {
        UserMO user = WonderlandContext.getUserManager().getUser(session);
        AvatarCellMO avatar = user.getAvatar(viewID);
        if (avatar == null) {
            logger.severe("clientDetached has null avatar for session");
            return;
        }

        avatar.detach();    // Detach avatar from world
        avatar.getCellCache().logout(session);
    }
    
    public void messageReceived(WonderlandClientSender sender,
                                ClientSession session, 
                                Message message)
    {
        if (message instanceof CellHierarchyMessage) {
            messageReceived(sender, session, (CellHierarchyMessage) message);
        } else {
            sender.send(session, new ErrorMessage(message.getMessageID(),
                        "Unexpected message type: " + message.getClass()));
        }
    }
  
    /**
     * When a cell message is received, dispatch it to the appropriate cell.
     * If the cell does not exist, send back an error message.
     * @param message the cell message
     * @param sender the message sender to send responses to
     */
    public void messageReceived(WonderlandClientSender sender,
                                ClientSession session,
                                CellHierarchyMessage message)
    {        
        switch(message.getActionType()) {
            case SET_VIEW :
                // TODO - should not assume this is an avatar, could be a camera
                ViewCreateResponseMessage response = createAvatar(sender, 
                                                                    session, 
                                                                    message);
                sender.send(session, response);
                
                break;
            default :
                logger.severe("Unexpected message in CellCacheClientHandler "+message.getActionType());
                sender.send(session, new ErrorMessage(message.getMessageID(),
                        "Unexpected message in CellCacheClientHandler: " +
                        message.getActionType()));
                break;
        }
    }
    
    private ViewCreateResponseMessage createAvatar(WonderlandClientSender sender,
                                                     ClientSession session, 
                                                     CellHierarchyMessage msg) {
        UserMO user = WonderlandContext.getUserManager().getUser(session);
        AvatarCellMO avatar = user.getAvatar(msg.getViewID());
        if (avatar == null) {
            user.getReference().getForUpdate(); // Mark for update
            avatar = new AvatarCellMO(user);
            viewID = msg.getViewID();
            user.putAvatar(viewID, avatar);
        }

        try {
            // HACK !
            if (!avatar.isLive())
                WonderlandContext.getCellManager().insertCellInWorld(avatar);

        } catch (MultipleParentException ex) {
            // the isLive check means we should never get here
            logger.severe("Failed to attach avatar, it's already attached");
        }
        
        avatar.getCellCache().login(sender, session);
        
        return new ViewCreateResponseMessage(msg.getMessageID(), avatar.getCellID());
    }
    
    /**
     * Get the channel used for sending to all clients of this type
     * @return the channel to send to all clients
     */
    public static WonderlandClientSender getSender() {
        return WonderlandContext.getCommsManager().getSender(CLIENT_TYPE);
    }
}
