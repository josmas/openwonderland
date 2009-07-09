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
package org.jdesktop.wonderland.modules.presencemanager.server;

import com.sun.sgs.app.AppContext;

import com.sun.mpk20.voicelib.app.ManagedCallBeginEndListener;

import com.sun.mpk20.voicelib.app.VoiceManager;

import com.sun.voip.client.connector.CallStatus;

import org.jdesktop.wonderland.common.messages.Message;

import org.jdesktop.wonderland.modules.presencemanager.common.PresenceInfo;

import org.jdesktop.wonderland.modules.presencemanager.common.PresenceManagerConnectionType;

import org.jdesktop.wonderland.modules.presencemanager.common.messages.PresenceInfoAddedMessage;
import org.jdesktop.wonderland.modules.presencemanager.common.messages.PresenceInfoChangeMessage;
import org.jdesktop.wonderland.modules.presencemanager.common.messages.PresenceInfoRemovedMessage;
import org.jdesktop.wonderland.modules.presencemanager.common.messages.ClientConnectMessage;
import org.jdesktop.wonderland.modules.presencemanager.common.messages.ClientConnectResponseMessage;

import org.jdesktop.wonderland.common.comms.ConnectionType;
import org.jdesktop.wonderland.server.comms.ClientConnectionHandler;
import org.jdesktop.wonderland.server.comms.CommsManager;
import org.jdesktop.wonderland.server.comms.CommsManagerFactory;
import org.jdesktop.wonderland.server.comms.WonderlandClientID;
import org.jdesktop.wonderland.server.comms.WonderlandClientSender;

import org.jdesktop.wonderland.common.auth.WonderlandIdentity;

import java.math.BigInteger;

import java.util.logging.Logger;

import java.util.concurrent.ConcurrentHashMap;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Properties;

import java.io.IOException;
import java.io.Serializable;

import com.sun.sgs.app.ManagedObject;

/**
 * Presence Manager
 * 
 * @author jprovino
 */
public class PresenceManagerConnectionHandler implements 
	ClientConnectionHandler, Serializable, ManagedObject, ManagedCallBeginEndListener {

    private static final Logger logger =
            Logger.getLogger(PresenceManagerConnectionHandler.class.getName());
    
    private ConcurrentHashMap<BigInteger, ArrayList<PresenceInfo>> sessions;

    public PresenceManagerConnectionHandler() {
        super();

	sessions = new ConcurrentHashMap();

	AppContext.getManager(VoiceManager.class).addCallBeginEndListener(this);
    }

    public ConnectionType getConnectionType() {
        return PresenceManagerConnectionType.CONNECTION_TYPE;
    }

    public void registered(WonderlandClientSender sender) {
	logger.fine("Presence Server manager connection registered");
    }

    public void clientConnected(WonderlandClientSender sender, 
	    WonderlandClientID clientID, Properties properties) {

	logger.fine("client connected...");
    }

    public void messageReceived(WonderlandClientSender sender, 
	    WonderlandClientID clientID, Message message) {

	if (sessions == null) {
	    sessions = new ConcurrentHashMap();
	}

	if (message instanceof ClientConnectMessage) {
	    ClientConnectMessage msg = (ClientConnectMessage) message;
	    /*
             * Send back all of the PresenceInfo data to the new client
             */
            Iterator<BigInteger> it = sessions.keySet().iterator();

            while (it.hasNext()) {
                BigInteger id = it.next();

                if (clientID.getID().equals(id)) {
                    continue;
                }

                ArrayList<PresenceInfo> presenceInfoList = sessions.get(id);

		//for (PresenceInfo info : presenceInfoList) {
		//    System.out.println("SENDING:  " + info);
		//}

                sender.send(clientID, new ClientConnectResponseMessage(presenceInfoList));
	    }
	    return;
	}

	if (message instanceof PresenceInfoAddedMessage) {
	    PresenceInfo presenceInfo = ((PresenceInfoAddedMessage) message).getPresenceInfo();

	    ArrayList<PresenceInfo> presenceInfoList = sessions.get(clientID.getID());

	    if (presenceInfoList == null) {
		presenceInfoList = new ArrayList();
		//System.out.println("PresenceInfoAddedMessage:  new PI list for clientID " 
		//    + clientID.getID());
	        sessions.put(clientID.getID(), presenceInfoList);
	    }

	    if (presenceInfoList.contains(presenceInfo) == false) {
	        presenceInfoList.add(presenceInfo);
		//System.out.println("PresenceInfoAddedMessage:  added " + presenceInfo);
	        logger.fine("PRESENCEINFOADDEDMESSAGE:  " + presenceInfo);
	    }

	    /*
	     * Send presenceInfo to all clients
	     */
	    sender.send(message);
	    return;
	} 

	if (message instanceof PresenceInfoRemovedMessage) {
	    PresenceInfo presenceInfo = ((PresenceInfoRemovedMessage) message).getPresenceInfo();

	    ArrayList<PresenceInfo> presenceInfoList = sessions.get(clientID.getID());

	    //System.out.println("PresenceInfoRemovedMessage:  removed " + presenceInfo);

	    presenceInfoList.remove(presenceInfo);

	    if (presenceInfoList.size() == 0) {
	        //System.out.println("PresenceInfoRemovedMessage:  removed list for clientID "
		//    + clientID.getID());
		sessions.remove(clientID.getID());
	    }

	    sender.send(message);
	    return;
	}

	if (message instanceof PresenceInfoChangeMessage) {
	    PresenceInfo presenceInfo = ((PresenceInfoChangeMessage) message).getPresenceInfo();

	    ArrayList<PresenceInfo> presenceInfoList = sessions.get(clientID.getID());

	    presenceInfoList.remove(presenceInfo);
	    presenceInfoList.add(presenceInfo);
	    sender.send(message);
	    return;
	}

        throw new UnsupportedOperationException("Unknown message: " + message);
    }

    public void clientDisconnected(WonderlandClientSender sender, WonderlandClientID clientID) {
	System.out.println("PRESENCE:  clientDisconnected");

	ArrayList<PresenceInfo> presenceInfoArrayList = sessions.get(clientID.getID());

	if (presenceInfoArrayList == null) {
	    //System.out.println("clientDisconnected:  No presence info for session " 
	    //	+ clientID.getID());
	    return;
	}

	PresenceInfo[] presenceInfoArray = presenceInfoArrayList.toArray(new PresenceInfo[0]);

	PresenceInfo info = null;

	for (int i = 0; i < presenceInfoArray.length; i++) {
	    info = presenceInfoArray[i];

	    if (info.clientID != null && info.clientID.equals(clientID.getID())) {
		//System.out.println("clientDisconnected:  removed PI " + info);
	        presenceInfoArrayList.remove(info);
		sender.send(new PresenceInfoRemovedMessage(info));
		break;
	    }
	}

	sessions.remove(clientID.getID());

	if (info == null) {
	    return;
	}

	/*
	 * Remove client presence info from other sessions
	 */
        Iterator<BigInteger> it = sessions.keySet().iterator();

        while (it.hasNext()) {
            BigInteger id = it.next();

            ArrayList<PresenceInfo> presenceInfoList = sessions.get(id);

	    presenceInfoList.remove(info);
	}

	System.out.println("PRESENCE:  clientDisconnected complete");
    }

    private void dump(String msg) {
	System.out.println("\n========  " + msg);

            Iterator<BigInteger> it = sessions.keySet().iterator();

            while (it.hasNext()) {
                BigInteger id = it.next();

                ArrayList<PresenceInfo> presenceInfoList = sessions.get(id);

                for (PresenceInfo info : presenceInfoList) {
		    System.out.println(id + " " + info);
                }
            }

	System.out.println("========  " + msg + "\n");
    }

    public void callBeginEndNotification(CallStatus status) {
	if (status.getCode() != CallStatus.ENDED) {
	    return;
	}

	if (status.getOption("Reason").equalsIgnoreCase("Warm Start") == false) {
	    return;
	}

	sessions = new ConcurrentHashMap();
    }

}
