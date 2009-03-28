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

import org.jdesktop.wonderland.common.auth.WonderlandIdentity;

import org.jdesktop.wonderland.common.comms.ConnectionType;

import org.jdesktop.wonderland.common.cell.CallID;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.CellStatus;

import org.jdesktop.wonderland.client.input.InputManager;
import org.jdesktop.wonderland.client.input.Event;
import org.jdesktop.wonderland.client.input.EventClassFocusListener;
import org.jdesktop.wonderland.client.input.EventListener;

import org.jdesktop.wonderland.common.messages.Message;

import org.jdesktop.wonderland.modules.audiomanager.common.AudioManagerConnectionType;

import org.jdesktop.wonderland.modules.audiomanager.common.messages.CellStatusChangeMessage;
import org.jdesktop.wonderland.modules.audiomanager.common.messages.GetVoiceBridgeMessage;
import org.jdesktop.wonderland.modules.audiomanager.common.messages.GetVoiceBridgeResponseMessage;
import org.jdesktop.wonderland.modules.audiomanager.common.messages.MuteCallMessage;
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

import org.jdesktop.wonderland.modules.presencemanager.client.PresenceManager;
import org.jdesktop.wonderland.modules.presencemanager.client.PresenceManagerFactory;

import org.jdesktop.wonderland.modules.presencemanager.common.PresenceInfo;

import java.io.IOException;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import java.util.ArrayList;

import java.util.logging.Logger;

import java.awt.event.KeyEvent;
import org.jdesktop.wonderland.client.jme.input.KeyEvent3D;
import java.awt.event.MouseEvent;
import org.jdesktop.wonderland.client.jme.input.MouseEvent3D;

import org.jdesktop.wonderland.modules.avatarbase.client.jme.cellrenderer.AvatarMuteEvent;
import org.jdesktop.wonderland.modules.avatarbase.client.jme.cellrenderer.AvatarSpeakingEvent;

/**
 *
 * @author jprovino
 */
public class AudioManagerClient extends BaseConnection implements 
	AudioMenuListener, SoftphoneListener, ViewCellConfiguredListener, CellStatusChangeListener {

    private static final Logger logger =
        Logger.getLogger(AudioManagerClient.class.getName());

    private WonderlandSession session;

    private boolean connected = true;

    private PresenceInfo presenceInfo;

    private CellID cellID;

    private PresenceManager pm;

    /** 
     * Create a new AudioManagerClient
     * @param session the session to connect to, guaranteed to be in
     * the CONNECTED state
     * @throws org.jdesktop.wonderland.client.comms.ConnectionFailureException
     */
    public AudioManagerClient(WonderlandSession session)  
	    throws ConnectionFailureException {

	this.session = session;

	pm = PresenceManagerFactory.getPresenceManager(session);

	session.connect(this);

        LocalAvatar avatar = ((CellClientSession)session).getLocalAvatar();
        avatar.addViewCellConfiguredListener(this);
        if (avatar.getViewCell() != null) {
            // if the view is already configured, fake an event
            viewConfigured(avatar);
        }

        JmeClientMain.getFrame().addToToolMenu(AudioMenu.getAudioMenu(this));

	javax.swing.JMenuItem userListJMenuItem = new javax.swing.JMenuItem();
        userListJMenuItem.setText("Show Users");
        userListJMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showUsers(evt);
            }
        });
        
        JmeClientMain.getFrame().addToToolMenu(userListJMenuItem);

	CellManager.getCellManager().addCellStatusChangeListener(this);

        SoftphoneControlImpl.getInstance().addSoftphoneListener(this);

	InputManager.inputManager().addGlobalEventListener(new MyEventListener());

	logger.fine("Starting AudioManagerCLient");
    }

    private UserListJFrame userListJFrame;

    public void showUsers(java.awt.event.ActionEvent evt) {
	if (presenceInfo == null) {
	    return;
	}

        if (userListJFrame == null) {
            userListJFrame = new UserListJFrame(session, this);
	}

	userListJFrame.setUserList();
        userListJFrame.setVisible(true);
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

	presenceInfo = pm.getPresenceInfo(cellID);

	if (presenceInfo == null) {
	    /*
	     * This happened when our viewConfigured() is called
	     * before viewConfigured() in the PresenceManager.
	     * 
	     * We create the PresenceInfo here and tell the PresenceManager
	     * about it.  When viewConfigured() is called in the PresenceManager,
	     * the PresenceManager will send a message to the server about
	     * a new session being created.
	     */
	    System.out.println("AudioManagerClient viewConfigured:  "
		+ "No Presence info for " + cellID + " THIS SHOULDN'T HAPPEN!");

	    String callID = CallID.getCallID(cellID);

            SoftphoneControlImpl.getInstance().setCallID(callID);

            presenceInfo = new PresenceInfo(cellID, session.getID(),
                session.getUserID(), callID);

	    pm.addSession(presenceInfo);
	}

	connectSoftphone();
    }

    public void connectSoftphone() {
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
	SoftphoneControlImpl sc = SoftphoneControlImpl.getInstance();
	sc.mute(isMuted);

	session.send(this, new MuteCallMessage(sc.getCallID(), isMuted));
    }

    private void toggleMute() {
	SoftphoneControlImpl sc = SoftphoneControlImpl.getInstance();
	boolean isMuted = sc.isMuted();

	isMuted = !isMuted;

	sc.mute(isMuted);
	session.send(this, new MuteCallMessage(sc.getCallID(), isMuted));
    }

    public void voiceChat() {
	try {
	    new VoiceChatDialog(this, cellID, session, presenceInfo);
	} catch (IOException e) {
	    logger.warning("Unable to get voice chat dialog:  " + e.getMessage());
	}
    }

    public void softphoneVisible(boolean isVisible) {
        AudioMenu.updateSoftphoneCheckBoxMenuItem(isVisible);
    }

    public void softphoneMuted(boolean isMuted) {
	SoftphoneControlImpl sc = SoftphoneControlImpl.getInstance();
	session.send(this, new MuteCallMessage(sc.getCallID(), sc.isMuted()));
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

	session.send(this, new TransferCallMessage(presenceInfo, phoneNumber));
    }

    public void cancelCallTransfer() {
    }

    @Override
    public void handleMessage(Message message) {
	logger.fine("got a message...");

	if (message instanceof GetVoiceBridgeResponseMessage) {
	    GetVoiceBridgeResponseMessage msg = (GetVoiceBridgeResponseMessage) message;

	    logger.warning("Got voice bridge " + msg.getBridgeInfo());

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

		logger.warning("The client is unable to connect to the bridge public address. "
		    + " Trying the bridge private Address.");

		try {
	            InetAddress ia = NetworkAddress.getPrivateLocalAddress(
		        "server:" + tokens[2] + ":" + tokens[4] + ":10000");

	            localAddress = ia.getHostAddress();
	        } catch (UnknownHostException ee) {
	            logger.warning(ee.getMessage());
		}
	    }

	    if (localAddress != null) {
	        try {
	            String sipURL = sc.startSoftphone(
		        presenceInfo.userID.getUsername(), registrarAddress, 10, localAddress, 
			AudioQuality.VPN);

		    logger.fine("Starting softphone:  " + presenceInfo);

	            // XXX need location and direction
	            session.send(this, new PlaceCallMessage(presenceInfo, sipURL, 0., 0., 0., 90., false));
	        } catch (IOException e) {
                    logger.warning(e.getMessage());
	        }
	    } else {
		// XXX Put up a dialog box here
		logger.warning("LOCAL ADDRESS IS NULL.  AUDIO WILL NOT WORK!!!!!!!!!!!!");
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
		try {
                    voiceChatDialog = new VoiceChatDialog(this, cellID, session, msg.getCaller());
		} catch (IOException e) {
	    	    logger.warning("Unable to get voice chat dialog:  " + e.getMessage());
		    return;
		}
            }

            voiceChatDialog.requestToJoin(msg.getGroup(), msg.getCaller(), 
		msg.getCalleeList(), msg.getChatType());
	} else if (message instanceof VoiceChatBusyMessage) {
	    VoiceChatBusyMessage msg = (VoiceChatBusyMessage) message;

	    new VoiceChatBusyDialog(msg.getGroup(), msg.getCallee());
	} else if (message instanceof VoiceChatInfoResponseMessage) {
	    VoiceChatInfoResponseMessage msg = (VoiceChatInfoResponseMessage) message;

            VoiceChatDialog voiceChatDialog =
                VoiceChatDialog.getVoiceChatDialog(msg.getGroup());

            if (voiceChatDialog == null) {
                logger.warning("No voiceChatDialog for " + msg.getGroup());
            } else {
                voiceChatDialog.setChatters(msg.getChatters());
            }
	} else if (message instanceof SpeakingMessage) {
	    SpeakingMessage msg = (SpeakingMessage) message;

	    if (userListJFrame != null) {
		userListJFrame.setSpeaking(msg.getCallID(), msg.isSpeaking());
	    }

	    PresenceInfo info = pm.getPresenceInfo(msg.getCallID());

	    if (info == null) {
		logger.warning("No presence info for " + msg.getCallID());
		return;
	    }

	    InputManager.inputManager().postEvent(
	        new AvatarSpeakingEvent(info.userID.getUsername(), msg.isSpeaking()));
	} else if (message instanceof MuteCallMessage) {
	    MuteCallMessage msg = (MuteCallMessage) message;

	    if (userListJFrame != null) {
	        userListJFrame.muteCall(msg.getCallID(), msg.isMuted());
	    }

	    PresenceInfo info = pm.getPresenceInfo(msg.getCallID());

	    if (info == null) {
		logger.warning("No presence info for " + msg.getCallID());
		return;
	    }

	    InputManager.inputManager().postEvent(
	        new AvatarMuteEvent(info.userID.getUsername(), msg.isMuted()));
	} else {
            throw new UnsupportedOperationException("Not supported yet.");
	}
    }

    public ConnectionType getConnectionType() {
        return AudioManagerConnectionType.CONNECTION_TYPE;
    }

    private void inputEvent(Event event) {
	if (event instanceof KeyEvent3D) {
	    KeyEvent3D e = (KeyEvent3D) event;

	    if (e.isPressed() && e.getKeyChar() == '[') {
		toggleMute();		
	    }
	}
    }

    /**
     * Global listener for keyboard and mouse events. Reports back to the Selection
     * Manager on any updates.
     */
    class MyEventListener extends EventClassFocusListener {
        @Override
        public Class[] eventClassesToConsume() {
            return new Class[] { KeyEvent3D.class, MouseEvent3D.class };
        }

        // Note: we don't override computeEvent because we don't do any computation in this listener.

        @Override
        public void commitEvent(Event event) {
            inputEvent(event);
        }

    }

}
