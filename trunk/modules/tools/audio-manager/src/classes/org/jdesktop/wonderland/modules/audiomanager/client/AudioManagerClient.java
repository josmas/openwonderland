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
package org.jdesktop.wonderland.modules.audiomanager.client;

import org.jdesktop.wonderland.client.cell.MovableComponent;
import org.jdesktop.wonderland.client.cell.MovableComponent.CellMoveListener;

import org.jdesktop.wonderland.client.cell.view.LocalAvatar;
import org.jdesktop.wonderland.client.cell.view.LocalAvatar.ViewCellConfiguredListener;

import org.jdesktop.wonderland.client.cell.view.ViewCell;

import org.jdesktop.wonderland.client.comms.BaseConnection;
import org.jdesktop.wonderland.client.comms.CellClientSession;
import org.jdesktop.wonderland.client.comms.ConnectionFailureException;
import org.jdesktop.wonderland.client.comms.SessionStatusListener;
import org.jdesktop.wonderland.client.comms.SessionLifecycleListener;
import org.jdesktop.wonderland.client.comms.WonderlandSession;
import org.jdesktop.wonderland.client.comms.WonderlandSessionManager;

import org.jdesktop.wonderland.client.jme.JmeClientMain;
import org.jdesktop.wonderland.modules.audiomanager.client.AudioMenuListener;

import org.jdesktop.wonderland.common.comms.ConnectionType;

import org.jdesktop.wonderland.common.cell.CellID;

import org.jdesktop.wonderland.common.messages.Message;

import org.jdesktop.wonderland.modules.audiomanager.common.AudioManagerConnectionType;
import org.jdesktop.wonderland.modules.audiomanager.common.messages.AvatarCellIDMessage;
import org.jdesktop.wonderland.modules.audiomanager.common.messages.GetVoiceBridgeMessage;
import org.jdesktop.wonderland.modules.audiomanager.common.messages.PlaceCallMessage;
import org.jdesktop.wonderland.modules.audiomanager.common.messages.TransferCallMessage;

import org.jdesktop.wonderland.client.softphone.AudioQuality;
import org.jdesktop.wonderland.client.softphone.SoftphoneControl;
import org.jdesktop.wonderland.client.softphone.SoftphoneControlImpl;
import org.jdesktop.wonderland.client.softphone.SoftphoneListener;

import java.io.IOException;

import java.util.logging.Logger;

import org.jdesktop.wonderland.common.NetworkAddress;

/**
 *
 * @author jprovino
 */
public class AudioManagerClient extends BaseConnection implements 
	AudioMenuListener, SoftphoneListener, ViewCellConfiguredListener
{
    private static final Logger logger =
        Logger.getLogger(AudioManagerClient.class.getName());

    private WonderlandSession session;
    private CellID cellID;
    private boolean connected = true;
    
    /** 
     * Create a new AudioManagerClient
     * @param session the session to connect to, guaranteed to be in
     * the CONNECTED state
     * @throws org.jdesktop.wonderland.client.comms.ConnectionFailureException
     */
    public AudioManagerClient(WonderlandSession session)  
	    throws ConnectionFailureException 
    {

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
        
	logger.warning("Starting AudioManagerCLient");
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
	logger.warning("View CONFIGURED!!!!!!!!!!!!!!!!!!!!!!!!!!!");

	cellID = localAvatar.getViewCell().getCellID();
	connectSoftphone();
    }

    public void connectSoftphone() {
	session.send(this, new AvatarCellIDMessage(cellID));

 	logger.warning("Sending message to server to get voice bridge...");

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
        SoftphoneControlImpl sc = SoftphoneControlImpl.getInstance();
    }

    public void softphoneVisible(boolean isVisible) {
        AudioMenu.updateSoftphoneCheckBoxMenuItem(isVisible);
    }

    public void softphoneMuted(boolean muted) {
    }

    public void softphoneConnected(boolean connected) {
    }

    public void softphoneExited() {
        logger.warning("Softphone exited, reconnect");

        AudioMenu.updateSoftphoneCheckBoxMenuItem(false);

        connectSoftphone();
    }

    public void microphoneGainTooHigh() {
    }

    public void transferCall(String phoneNumber) {
        CellID cellID = ((CellClientSession)session).getLocalAvatar().getViewCell().getCellID();
	session.send(this, new TransferCallMessage(cellID, phoneNumber));
    }

    public void cancelCallTransfer() {
    }

    @Override
    public void handleMessage(Message message) {
	logger.warning("got a message...");

	if (message instanceof GetVoiceBridgeMessage) {
	    GetVoiceBridgeMessage msg = (GetVoiceBridgeMessage) message;

	    logger.warning("Got voice bridge " + msg.getBridgeInfo());

	    SoftphoneControlImpl sc = SoftphoneControlImpl.getInstance();

            String tokens[] = msg.getBridgeInfo().split(":");

            String registrarAddress = tokens[2] + ";sip-stun:";

            registrarAddress += tokens[4];

	    String localHost = NetworkAddress.getDefaultHostAddress();

	    try {
	        String sipURL = sc.startSoftphone(
		    msg.getUsername(), registrarAddress, 10, localHost, AudioQuality.VPN);

	        CellID cellID = ((CellClientSession)session).getLocalAvatar().getViewCell().getCellID();

	        // XXX need location and direction
	        session.send(this, new PlaceCallMessage(
		    cellID, sipURL, 0., 0., 0., 0., false));
	    } catch (IOException e) {
		logger.warning(e.getMessage());
	    }
	} else {
            throw new UnsupportedOperationException("Not supported yet.");
	}
    }

    public ConnectionType getConnectionType() {
        return AudioManagerConnectionType.CONNECTION_TYPE;
    }

}
