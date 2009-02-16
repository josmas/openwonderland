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
package org.jdesktop.wonderland.modules.audiomanager.client;

import org.jdesktop.wonderland.client.cell.view.LocalAvatar;
import org.jdesktop.wonderland.client.cell.view.LocalAvatar.ViewCellConfiguredListener;

import org.jdesktop.wonderland.client.cell.view.ViewCell;

import org.jdesktop.wonderland.client.comms.BaseConnection;
import org.jdesktop.wonderland.client.comms.CellClientSession;
import org.jdesktop.wonderland.client.comms.ConnectionFailureException;
import org.jdesktop.wonderland.client.comms.SessionStatusListener;
import org.jdesktop.wonderland.client.comms.WonderlandSession;

import org.jdesktop.wonderland.client.jme.JmeClientMain;

import org.jdesktop.wonderland.common.NetworkAddress;

import org.jdesktop.wonderland.common.comms.ConnectionType;

import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.CellStatus;

import org.jdesktop.wonderland.client.input.InputManager;
import org.jdesktop.wonderland.client.input.Event;
import org.jdesktop.wonderland.client.input.EventClassListener;
import org.jdesktop.wonderland.client.input.EventListener;

import org.jdesktop.wonderland.common.messages.Message;

import org.jdesktop.wonderland.modules.audiomanager.common.AudioManagerConnectionType;
import org.jdesktop.wonderland.modules.audiomanager.common.messages.AvatarCellIDMessage;
import org.jdesktop.wonderland.modules.audiomanager.common.messages.CellStatusChangeMessage;
import org.jdesktop.wonderland.modules.audiomanager.common.messages.GetVoiceBridgeMessage;
import org.jdesktop.wonderland.modules.audiomanager.common.messages.GetUserListMessage;
import org.jdesktop.wonderland.modules.audiomanager.common.messages.PlaceCallMessage;
import org.jdesktop.wonderland.modules.audiomanager.common.messages.SpeakingMessage;
import org.jdesktop.wonderland.modules.audiomanager.common.messages.TransferCallMessage;
import org.jdesktop.wonderland.modules.audiomanager.common.messages.VoiceChatJoinRequestMessage;
import org.jdesktop.wonderland.modules.audiomanager.common.messages.VoiceChatBusyMessage;
import org.jdesktop.wonderland.modules.audiomanager.common.messages.VoiceChatInfoResponseMessage;

import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.cell.CellManager;
import org.jdesktop.wonderland.client.cell.CellStatusChangeListener;

import org.jdesktop.wonderland.client.softphone.AudioQuality;
import org.jdesktop.wonderland.client.softphone.SoftphoneControl;
import org.jdesktop.wonderland.client.softphone.SoftphoneControlImpl;
import org.jdesktop.wonderland.client.softphone.SoftphoneListener;

import java.io.IOException;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import java.util.ArrayList;

import java.util.logging.Logger;

import java.awt.event.MouseEvent;
import org.jdesktop.wonderland.client.jme.input.MouseEvent3D;

/**
 *
 * @author jprovino
 */
public class AudioManagerClient extends BaseConnection implements 
	AudioMenuListener, SoftphoneListener, ViewCellConfiguredListener, CellStatusChangeListener {

    private static final Logger logger =
        Logger.getLogger(AudioManagerClient.class.getName());

    private WonderlandSession session;

    private CellID cellID;
    private boolean connected = true;

    private UserListJFrame userListJFrame;

    /** 
     * Create a new AudioManagerClient
     * @param session the session to connect to, guaranteed to be in
     * the CONNECTED state
     * @throws org.jdesktop.wonderland.client.comms.ConnectionFailureException
     */
    public AudioManagerClient(WonderlandSession session)  
	    throws ConnectionFailureException {

	this.session = session;
	session.connect(this);

        LocalAvatar avatar = ((CellClientSession)session).getLocalAvatar();
        avatar.addViewCellConfiguredListener(this);
        if (avatar.getViewCell() != null) {
            // if the view is already configured, fake an event
            viewConfigured(avatar);
        }

        SoftphoneControlImpl.getInstance().addSoftphoneListener(this);

        JmeClientMain.getFrame().addToToolMenu(AudioMenu.getAudioMenu(this));
        
	CellManager.getCellManager().addCellStatusChangeListener(this);

	InputManager.inputManager().addGlobalEventListener(new MouseEventListener());

	logger.fine("Starting AudioManagerCLient");
    }

    public void cellStatusChanged(Cell cell, CellStatus status) {
	session.send(this, new CellStatusChangeMessage(cell.getCellID(), 
	    status.equals(CellStatus.ACTIVE)));
    }

    public synchronized void execute(final Runnable r) {
    }

    @Override
    public void disconnect() {
        // remove listeners

        // TODO: add methods to remove listeners!

        // LocalAvatar avatar = ((CellClientSession)session).getLocalAvatar();
        // avatar.removeViewCellConfiguredListener(this);
        SoftphoneControlImpl.getInstance().removeSoftphoneListener(this);
        //JmeClientMain.getFrame().removeAudioMenuListener(this);

        super.disconnect();
    }

    public void viewConfigured(LocalAvatar localAvatar) {
	cellID = localAvatar.getViewCell().getCellID();
	connectSoftphone();
    }

    public void connectSoftphone() {
	session.send(this, new AvatarCellIDMessage(cellID));

 	logger.fine("Sending message to server to get voice bridge...");

	session.send(this, new GetVoiceBridgeMessage());
    }

    public void showSoftphone(boolean isVisible) {
        SoftphoneControlImpl.getInstance().setVisible(isVisible);
    }

    public void testAudio() {
        SoftphoneControlImpl.getInstance().runLineTest();
    }

    public void reconnectSoftphone() {
        connectSoftphone();
    }

    public void transferCall() {
	CallMigrationForm callMigrationForm = CallMigrationForm.getInstance();

	callMigrationForm.setClient(this);
	callMigrationForm.setVisible(true);
    }

    public void logAudioProblem() {
        SoftphoneControlImpl.getInstance().logAudioProblem();
    }

    public void mute(boolean isMuted) {
	SoftphoneControlImpl.getInstance().mute(isMuted);
    }

    public void voiceChat() {
	new VoiceChatDialog(this, session, cellID);
    }

    public void softphoneVisible(boolean isVisible) {
        AudioMenu.updateSoftphoneCheckBoxMenuItem(isVisible);
    }

    public void softphoneMuted(boolean muted) {
    }

    public void softphoneConnected(boolean connected) {
    }

    public void softphoneExited() {
        logger.fine("Softphone exited, reconnect");

        AudioMenu.updateSoftphoneCheckBoxMenuItem(false);

        connectSoftphone();
    }

    public void microphoneGainTooHigh() {
    }

    public void transferCall(String phoneNumber) {
	SoftphoneControlImpl sc = SoftphoneControlImpl.getInstance();

	session.send(this, new TransferCallMessage(sc.getCallID(), phoneNumber));
    }

    public void cancelCallTransfer() {
    }

    @Override
    public void handleMessage(Message message) {
	logger.fine("got a message...");

	if (message instanceof GetVoiceBridgeMessage) {
	    GetVoiceBridgeMessage msg = (GetVoiceBridgeMessage) message;

	    logger.fine("Got voice bridge " + msg.getBridgeInfo());

	    SoftphoneControlImpl sc = SoftphoneControlImpl.getInstance();

	    /*
	     * The voice bridge info is a String of values separated by ":".
	     * The numbers indicate the index in tokens[].
	     *
	     *     0      1      2                   3                 4
	     * <bridgeId>::<privateHostName>:<privateControlPort>:<privateSipPort>
	     *			 5                   6                 7
	     *   	  :<publicHostName>:<publicControlPort>:<publicSipPort>
	     */
            String tokens[] = msg.getBridgeInfo().split(":");

            String registrarAddress = tokens[5] + ";sip-stun:";

            registrarAddress += tokens[7];

	    String localAddress = null;

	    try {
	        InetAddress ia = NetworkAddress.getPrivateLocalAddress(
		    "server:" + tokens[5] + ":" + tokens[7] + ":10000");

	        localAddress = ia.getHostAddress();
	    } catch (UnknownHostException e) {
	        logger.warning(e.getMessage());
	    }

	    if (localAddress != null) {
	        try {
	            String sipURL = sc.startSoftphone(
		        msg.getUsername(), registrarAddress, 10, localAddress, AudioQuality.VPN);

	            CellID cellID = ((CellClientSession)session).getLocalAvatar().getViewCell().getCellID();

		    logger.fine("Softphone call id is " + cellID.toString());

		    sc.setCallID(cellID.toString());

	            // XXX need location and direction
	            session.send(this, new PlaceCallMessage(
		        cellID.toString(), sipURL, 0., 0., 0., 90., false));
	        } catch (IOException e) {
                    logger.warning(e.getMessage());
	        }
	    } else {
		// XXX Put up a dialog box here
		logger.warning("UNABLE TO START SOFTPHONE.  AUDIO WILL NOT WORK!!!!!!!!!!!!");
		/*
		 * Try again.
		 */
		connectSoftphone();
	    }
	} else if (message instanceof VoiceChatJoinRequestMessage) {
	   VoiceChatJoinRequestMessage msg = (VoiceChatJoinRequestMessage) message;

	   VoiceChatDialog voiceChatDialog =
                VoiceChatDialog.getVoiceChatDialog(msg.getGroup());

            if (voiceChatDialog == null) {
	        CellID cellID = ((CellClientSession)session).getLocalAvatar().getViewCell().getCellID();
                voiceChatDialog = new VoiceChatDialog(this, session, cellID);
            }

            voiceChatDialog.requestToJoin(msg.getGroup(), msg.getCaller(), 
		msg.getCalleeList(), msg.getChatType());
	} else if (message instanceof VoiceChatBusyMessage) {
	    VoiceChatBusyMessage msg = (VoiceChatBusyMessage) message;

	    new VoiceChatBusyDialog(msg.getGroup(), msg.getCaller());
	} else if (message instanceof VoiceChatInfoResponseMessage) {
	    VoiceChatInfoResponseMessage msg = (VoiceChatInfoResponseMessage) message;

            VoiceChatDialog voiceChatDialog =
                VoiceChatDialog.getVoiceChatDialog(msg.getGroup());

            logger.fine("response " + msg.getChatInfo());

            if (voiceChatDialog == null) {
                logger.warning(
                    "No voiceChatDialog for " + msg.getGroup());
            } else {
                voiceChatDialog.setChatters(msg.getChatInfo());
            }
	} else if (message instanceof SpeakingMessage) {
	    SpeakingMessage msg = (SpeakingMessage) message;

	    logger.info("CallId " + msg.getCallID() 
		+ (msg.isSpeaking() ? " Started Speaking" : " Stopped Speaking"));
	} else if (message instanceof GetUserListMessage) {
	    if (userListJFrame == null) {
	    	userListJFrame = 
		    new UserListJFrame(((GetUserListMessage) message).getLocation());
	    }

	    ArrayList<String> userList = ((GetUserListMessage) message).getUserList();

	    String s = "";

	    for (String user : userList) {
		s += user + " ";
	    }

	    userListJFrame.setListData(userList.toArray(new String[0]));
	    userListJFrame.setVisible(true);
	} else {
            throw new UnsupportedOperationException("Not supported yet.");
	}
    }

    public ConnectionType getConnectionType() {
        return AudioManagerConnectionType.CONNECTION_TYPE;
    }

    private void inputEvent(Event event) {
	//System.out.println("Got event " + event);
    }

    /**
     * Global mouse listener for selection events. Reports back to the Selection
     * Manager on any updates.
     */
    class MouseEventListener extends EventClassListener {
        @Override
        public Class[] eventClassesToConsume() {
            return new Class[] { MouseEvent3D.class };
        }

        // Note: we don't override computeEvent because we don't do any computation in this listener.

        @Override
        public void commitEvent(Event event) {
            inputEvent(event);
        }
    }

}
