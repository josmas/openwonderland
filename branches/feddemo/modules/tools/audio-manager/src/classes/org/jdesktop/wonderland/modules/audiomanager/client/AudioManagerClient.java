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

import org.jdesktop.wonderland.client.cell.Cell;

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

import org.jdesktop.wonderland.common.cell.CallID;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.CellStatus;

import org.jdesktop.wonderland.client.input.InputManager;
import org.jdesktop.wonderland.client.input.Event;
import org.jdesktop.wonderland.client.input.EventClassFocusListener;
import org.jdesktop.wonderland.client.input.EventListener;

import org.jdesktop.wonderland.common.messages.Message;

import org.jdesktop.wonderland.modules.audiomanager.common.AudioManagerConnectionType;

import org.jdesktop.wonderland.modules.audiomanager.common.messages.ConeOfSilenceEnterExitMessage;
import org.jdesktop.wonderland.modules.audiomanager.common.messages.GetVoiceBridgeMessage;
import org.jdesktop.wonderland.modules.audiomanager.common.messages.GetVoiceBridgeResponseMessage;
import org.jdesktop.wonderland.modules.audiomanager.common.messages.MuteCallMessage;
import org.jdesktop.wonderland.modules.audiomanager.common.messages.PlaceCallMessage;
import org.jdesktop.wonderland.modules.audiomanager.common.messages.PlayerInRangeMessage;
import org.jdesktop.wonderland.modules.audiomanager.common.messages.SpeakingMessage;
import org.jdesktop.wonderland.modules.audiomanager.common.messages.TransferCallMessage;
import org.jdesktop.wonderland.modules.audiomanager.common.messages.VoiceChatJoinRequestMessage;
import org.jdesktop.wonderland.modules.audiomanager.common.messages.VoiceChatBusyMessage;
import org.jdesktop.wonderland.modules.audiomanager.common.messages.VoiceChatInfoResponseMessage;

import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.cell.CellManager;

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

import org.jdesktop.wonderland.client.jme.input.KeyEvent3D;
import org.jdesktop.wonderland.client.jme.input.MouseEvent3D;

import java.awt.Color;

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

import org.jdesktop.wonderland.modules.avatarbase.client.jme.cellrenderer.AvatarNameEvent;
import org.jdesktop.wonderland.modules.avatarbase.client.jme.cellrenderer.NameTag;
import org.jdesktop.wonderland.modules.avatarbase.client.jme.cellrenderer.NameTag.EventType;

/**
 *
 * @author jprovino
 */
public class AudioManagerClient extends BaseConnection implements 
	AudioMenuListener, SoftphoneListener, ViewCellConfiguredListener {

    private static final Logger logger =
        Logger.getLogger(AudioManagerClient.class.getName());

    private WonderlandSession session;

    private boolean connected = true;

    private PresenceManager pm;

    private PresenceInfo presenceInfo;

    private Cell cell;

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
            userListJFrame = new UserListJFrame(pm, cell);
	}

	userListJFrame.setUserList();
        userListJFrame.setVisible(true);
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
	cell = localAvatar.getViewCell();

	CellID cellID = cell.getCellID();

	presenceInfo = pm.getPresenceInfo(cellID);

	connectSoftphone();
    }

    public void connectSoftphone() {
 	logger.fine("Sending message to server to get voice bridge...");
	session.send(this, new GetVoiceBridgeMessage());
    }

    public void showSoftphone(boolean isVisible) {
        SoftphoneControlImpl.getInstance().setVisible(isVisible);
    }

    public void setAudioQuality(AudioQuality audioQuality) {
        SoftphoneControlImpl.getInstance().setAudioQuality(audioQuality);
	reconnectSoftphone();
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
	if (cell == null) {
	    return;
	}

	try {
	    new VoiceChatDialog(this, cell.getCellID(), session, presenceInfo);
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
                    voiceChatDialog = new VoiceChatDialog(this, cell.getCellID(), session, msg.getCaller());
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

	    PresenceInfo info = pm.getPresenceInfo(msg.getCallID());

	    if (info == null) {
		logger.warning("No presence info for " + msg.getCallID());
		return;
	    }

	    pm.setSpeaking(info, msg.isSpeaking());

	    if (userListJFrame != null) {
		userListJFrame.setUserList();
	    }

	    AvatarNameEvent avatarNameEvent;

	    if (msg.isSpeaking()) {
		avatarNameEvent = new AvatarNameEvent(EventType.STARTED_SPEAKING,
		    info.userID.getUsername(), info.usernameAlias);
	    } else {
		avatarNameEvent = new AvatarNameEvent(EventType.STOPPED_SPEAKING,
		    info.userID.getUsername(), info.usernameAlias);
	    }

	    InputManager.inputManager().postEvent(avatarNameEvent);
	} else if (message instanceof MuteCallMessage) {
	    MuteCallMessage msg = (MuteCallMessage) message;

	    PresenceInfo info = pm.getPresenceInfo(msg.getCallID());

	    if (info == null) {
		logger.warning("No presence info for " + msg.getCallID());
		return;
	    }

	    pm.setMute(info, msg.isMuted());

	    if (userListJFrame != null) {
	        userListJFrame.setUserList();
	    }

	    AvatarNameEvent avatarNameEvent;

	    if (msg.isMuted()) {
		avatarNameEvent = new AvatarNameEvent(EventType.MUTE,
		    info.userID.getUsername(), info.usernameAlias);
	    } else {
		avatarNameEvent = new AvatarNameEvent(EventType.UNMUTE,
		    info.userID.getUsername(), info.usernameAlias);
	    }

	    InputManager.inputManager().postEvent(avatarNameEvent);
	} else if (message instanceof ConeOfSilenceEnterExitMessage) {
	    ConeOfSilenceEnterExitMessage msg = (ConeOfSilenceEnterExitMessage) message;

	    pm.setEnteredConeOfSilence(presenceInfo, msg.entered()); 	

	    PresenceInfo info = pm.getPresenceInfo(msg.getCallID());

	    if (info == null) {
		logger.warning("No presence info for " + msg.getCallID());
		return;
	    }

	    AvatarNameEvent avatarNameEvent;

	    if (msg.entered()) {
		avatarNameEvent = new AvatarNameEvent(EventType.ENTERED_CONE_OF_SILENCE,
		    info.userID.getUsername(), info.usernameAlias);
	    } else {
		avatarNameEvent = new AvatarNameEvent(EventType.EXITED_CONE_OF_SILENCE,
		    info.userID.getUsername(), info.usernameAlias);
	    }

	    InputManager.inputManager().postEvent(avatarNameEvent);
	} else if (message instanceof PlayerInRangeMessage) {
	    PlayerInRangeMessage msg = (PlayerInRangeMessage) message;

            System.out.println("Player in range " + msg.isInRange() + " " 
		+ msg.getPlayerID() + " player in range " + msg.getPlayerInRangeID());
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
