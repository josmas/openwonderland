/**
 * Project Looking Glass
 *
 * $RCSfile: WonderlandBaseSessionListener.java,v $
 *
 * Copyright (c) 2004-2007, Sun Microsystems, Inc., All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * $Revision: 1.15 $
 * $Date: 2007/11/28 23:53:34 $
 * $State: Exp $
 */
package org.jdesktop.wonderland.server;

import com.sun.sgs.app.AppContext;
import com.sun.sgs.app.Channel;
import com.sun.sgs.app.ChannelManager;
import com.sun.sgs.app.ClientSession;
import com.sun.sgs.app.ClientSessionListener;
import com.sun.sgs.app.Delivery;
import com.sun.sgs.app.NameExistsException;
import com.sun.sgs.app.NameNotBoundException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.logging.Logger;
import org.jdesktop.lg3d.wonderland.darkstar.common.messages.Message;
import org.jdesktop.lg3d.wonderland.darkstar.common.messages.NativeApplicationMessage;

/**
 *
 * @author krishna_gadepalli
 */
class WonderlandBaseSessionListener implements ClientSessionListener, Serializable {

    protected final static Logger logger = Logger.getLogger("org.jdesktop.lg3d.wonderland.session");
    
    protected ClientSession session;
    protected String name;

    protected ArrayList<String> channelsCreated = new ArrayList<String>();
    private boolean serverMasterClient = false;
    
    public WonderlandBaseSessionListener(ClientSession session) {
        logger.info("New client session " + session.getName() + " id " 
	   + session.getSessionId());
        this.session = session;
        name = session.getName();
        
        // TODO - don't use a hard coded user name
        if (name.equalsIgnoreCase("ServerManager")) {
            AppContext.getDataManager().getBinding(ServerManagerGLO.BINDING_NAME, ServerManagerGLO.class).addUserToChannel(session);
        }
    }

    public void disconnected(boolean graceful) {
        logger.info("Session disconnected "+name);
	closeChannels();
        
        // TODO - don't use a hard coded user name
        if (name.equalsIgnoreCase("ServerManager")) {
            AppContext.getDataManager().getBinding(ServerManagerGLO.BINDING_NAME, ServerManagerGLO.class).removeUserFromChannel(session);
        }
        
        // unregister this app as a master client if it was one
        if (isServerMasterClient()) {
            ServerMasterClientGLO.getServerMasterClientGLO().removeServerMasterClient(session);
        }
    }

    // Clean up channels we have created

    protected Channel getChannel(String channelName) {
	try {
	    return AppContext.getChannelManager().getChannel(channelName);
	} catch (NameNotBoundException e) {
	    logger.warning("Channel[" + channelName + "] does not exist!");
	    return null;
	}
    }

    protected void closeChannels () {
	Channel nativeChan;

	synchronized(channelsCreated) {
	    for (String channelName : channelsCreated) {
		if ((nativeChan = getChannel(channelName)) != null)
		    nativeChan.close();
	    }
	    channelsCreated.clear();
	}
    }
    
    public void receivedMessage(byte[] data) {
        Message message = Message.extractMessage(data);
        
        if (message instanceof NativeApplicationMessage) 
            processNativeApplicationMessage((NativeApplicationMessage) message);
    }
    
    protected boolean processNativeApplicationMessage(NativeApplicationMessage message) {
        
        String channelName=null;
        Channel nativeChan=null;
        
	NativeApplicationMessage.ActionType action = message.getActionType();

        switch (action) {
	case CREATE:
	     channelName = message.getChannelName();
	     logger.info("Arg: " + action + " channelName = " + channelName);
	     ChannelManager chanMgr = AppContext.getChannelManager();
	     try {
		nativeChan = chanMgr.createChannel(channelName, null, Delivery.RELIABLE);
	     } catch (NameExistsException e) {
		logger.warning("Arg: " + action + " channelName = " + channelName +
				    " already exists! Joining it...");
	     } catch (Exception e) {
		logger.warning("Arg: " + action + " channelName = " + channelName +
				    " Error creating...");
		break;
	     }

	     if ((nativeChan = getChannel(channelName)) != null) {
		nativeChan.join(session, null);
		synchronized(channelsCreated) {
                    channelsCreated.add(channelName);
                }
             }
	     break;

	case JOIN:
	     channelName = message.getChannelName();
	     logger.info("Arg: " + action + " channelName = " + channelName);
	     if ((nativeChan = getChannel(channelName)) != null) {
		 nativeChan.join(session, null);
	     } 
	     break;

	case LEAVE:
	     channelName = message.getChannelName();
	     logger.info("Arg: " + action + " channelName = " + channelName);
	     if ((nativeChan = getChannel(channelName)) != null) {
		 nativeChan.leave(session);
		 if (!nativeChan.hasSessions()) {
		     synchronized(channelsCreated) {
			 logger.warning("CLOSING channelName = + channelName");
			 channelsCreated.remove(channelName);
		     }
		     nativeChan.close();
		 }
	     }
	     break;

	case CLOSE:
	     channelName = message.getChannelName();
	     logger.info("Arg: " + action + " channelName = " + channelName);
	     if ((nativeChan = getChannel(channelName)) != null) {
		 nativeChan.leaveAll();
		 synchronized(channelsCreated) {
		     channelsCreated.remove(channelName);
		 }
		 nativeChan.close();
	     }
	     break;

	default:
	     return false;
        }

	return true;
    }
    
    protected boolean isServerMasterClient() {
        return serverMasterClient;
    }
    
    protected void setServerMasterClient(boolean serverMasterClient) {
        this.serverMasterClient = serverMasterClient;
    }
}
