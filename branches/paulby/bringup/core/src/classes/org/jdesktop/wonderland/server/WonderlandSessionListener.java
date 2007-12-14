/**
 * Project Looking Glass
 *
 * $RCSfile: WonderlandSessionListener.java,v $
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
 * $Revision: 1.59 $
 * $Date: 2007/12/03 16:03:41 $
 * $State: Exp $
 */
package org.jdesktop.wonderland.server;

import com.sun.sgs.app.AppContext;
import com.sun.sgs.app.DataManager;
import com.sun.sgs.app.Channel;
import com.sun.sgs.app.ChannelManager;
import com.sun.sgs.app.ClientSession;
import com.sun.sgs.app.ManagedReference;
import java.io.Serializable;
import java.util.ArrayList;
import javax.vecmath.Vector3f;
import org.jdesktop.lg3d.wonderland.darkstar.common.ChannelInfo;
import org.jdesktop.lg3d.wonderland.darkstar.common.messages.AvatarP2PMessage;
import org.jdesktop.lg3d.wonderland.darkstar.common.messages.AvatarSetupMessage;
import org.jdesktop.lg3d.wonderland.darkstar.common.messages.UserChangedMessage;
import org.jdesktop.lg3d.wonderland.darkstar.common.CellID;
import org.jdesktop.lg3d.wonderland.darkstar.common.messages.Message;
import org.jdesktop.lg3d.wonderland.darkstar.common.messages.NativeApplicationMessage;

import com.sun.mpk20.voicelib.app.ManagedCallStatusListener;
import com.sun.mpk20.voicelib.app.ManagedCallBeginEndListener;
import com.sun.mpk20.voicelib.app.VoiceHandler;
import com.sun.mpk20.voicelib.app.VoiceManagerParameters;

import com.sun.mpk20.voicelib.impl.app.VoiceHandlerImpl;
import com.sun.sgs.app.Delivery;

import com.sun.voip.client.connector.CallStatus;
import java.util.logging.Level;
import javax.media.j3d.BoundingBox;
import javax.vecmath.Matrix4d;
import javax.vecmath.Matrix4f;
import javax.vecmath.Point3d;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3d;
import org.jdesktop.lg3d.wonderland.darkstar.common.messages.AvatarCellMessage;
import org.jdesktop.lg3d.wonderland.darkstar.common.messages.CellMessage;
import org.jdesktop.lg3d.wonderland.darkstar.common.messages.ErrorMessage;
import org.jdesktop.lg3d.wonderland.darkstar.common.messages.FileTransferMessage;
import org.jdesktop.lg3d.wonderland.darkstar.common.messages.PingMessage;
import org.jdesktop.lg3d.wonderland.darkstar.common.messages.ServerMasterClientReadyMessage;
import org.jdesktop.lg3d.wonderland.darkstar.common.messages.SoftphoneMessage;
import org.jdesktop.lg3d.wonderland.darkstar.server.cell.AvatarCellGLO;
import org.jdesktop.lg3d.wonderland.darkstar.server.cell.CellGLO;
import org.jdesktop.lg3d.wonderland.darkstar.server.cell.MoveableCellGLO;
import org.jdesktop.lg3d.wonderland.darkstar.server.cell.MasterCellCacheGLO;
import org.jdesktop.lg3d.wonderland.darkstar.server.cell.SharedApp2DX11CellGLO;
import com.sun.sgs.app.ObjectNotFoundException;
import org.jdesktop.lg3d.wonderland.darkstar.common.messages.AlsSupportedMessage;
import org.jdesktop.lg3d.wonderland.darkstar.common.messages.ServerLaunchXAppMessage;

/**
 * 
 * Process the messages from the users session, there will be an instance of this class
 * per user.
 * 
 * TODO Implement message handlers that are automatically selected instead of the
 * if instanceof checks we currently use in receiveMessage
 *
 * @author paulby
 */
class WonderlandSessionListener extends WonderlandBaseSessionListener implements
	ManagedCallStatusListener, ManagedCallBeginEndListener, Serializable {
    
    private ManagedReference userGLORef;
    private ArrayList<ManagedReference> nativeAppCells = new ArrayList<ManagedReference>();

    public WonderlandSessionListener(ClientSession session) {
        super(session);
    }
    
    /**
     * After the client connects, it sets up its avatar by sending
     * an AvatarSetupMessage.
     *
     * OpenHouse HACK, the avatar information should all come from the server
     */
    protected boolean processAvatarSetupMessage(AvatarSetupMessage setup) {
	AvatarSetupMessage.ActionType action = setup.getActionType();

	ChannelManager chanMgr = AppContext.getChannelManager();
	Channel userChangeChannel = chanMgr.getChannel(ChannelInfo.USER_CHANGE);

	UserChangedMessage msg;

	switch (action) {
	case LOGGED_IN:
	    userGLORef = UserGLO.getOrCreateUserGLO(name, session.getSessionId(),
						    setup.getAvatarInfo());
	    userChangeChannel.join(session, null);
	    UserGLO userGLO = userGLORef.getForUpdate(UserGLO.class);

            userGLO.setUserColor(setup.getUserColor());
	    
	    // Tell everyone (including ourselves) that we have joined
	    msg = new UserChangedMessage(
		    UserChangedMessage.ActionType.USER_ADDED, 
		    session.getSessionId().getBytes(), 
		    session.getName(),
                    userGLO.getUserColor(),
                    userGLO.getAvatarCellRef().get(AvatarCellGLO.class).getSpeakingStatus());
                    
	    userChangeChannel.send(msg.getBytes());
	    	    
            userGLO.login(session);
            
            if (setup.getInitialPosition()!=null) {
                AvatarCellGLO avatar = userGLO.getAvatarCellRef().get(AvatarCellGLO.class);
                avatar.setLocation(new Point3f(setup.getInitialPosition()), new Vector3f(0f,-1f,0f));
            } else {
                // Default location
                AvatarCellGLO avatar = userGLO.getAvatarCellRef().get(AvatarCellGLO.class);
                avatar.setLocation(new Point3f(50f, 1.7f, 50f), new Vector3f(0f,-1f,0f));                
            }

	    UserManager userManager = UserManager.getUserManager();
	    
	    // Notify new user of all other users
	    for(ManagedReference uRef : userManager.getAllUsers()) {
		UserGLO u = uRef.get(UserGLO.class);
		if (!u.getUserName().equals(name)) {
                    try {
                        if(logger.isLoggable(Level.FINER)) logger.log(Level.FINER, "creating UserChangedMessage for {0} including SpeakingStatus {1}", new Object[]{u.getUserName(), u.getAvatarCellRef().get(AvatarCellGLO.class).getSpeakingStatus()});
                        msg = new UserChangedMessage(
                                UserChangedMessage.ActionType.USER_ADDED, 
                                u.getUserID().getBytes(), 
                                u.getUserName(),
                                u.getUserColor(),
                                u.getAvatarCellRef().get(AvatarCellGLO.class).getSpeakingStatus());
                        userChangeChannel.send(session, msg.getBytes());
                    } catch(Exception e) {
                        // getUserID can return null if the user was not
                        // removed from the UserManager. Seems to be caused by
                        // transaction timeouts
                        logger.log(Level.SEVERE, "Error sending UserChangeMessage", e);
                    }
 		}            
	    }

	    break;

	case MODEL_CHANGED:
	    userGLORef.getForUpdate(UserGLO.class).setAvatarInfo(setup.getAvatarInfo());
	    // Tell everyone (including ourselves) that we have changed our model
	    msg = new UserChangedMessage(
		    session.getSessionId().getBytes(), 
		    session.getName(),
		    setup.getAvatarInfo());
	    userChangeChannel.send(msg.getBytes());
	    break;
	}
        
        return true;
    }

    public void callStatusChanged(CallStatus status) {
	if (logger.isLoggable(Level.FINE)) logger.log(Level.FINE, "CallStatusNotification:  {0} for {1}", new Object[]{status, this});

	if (status.getCode() == CallStatus.BRIDGE_OFFLINE) {
	    logger.info("Bridge Offline status:  " + status);

	    bridgeInfo = status.getOption("CallInfo");

	    if (bridgeInfo == null || bridgeInfo.length() == 0) {
		logger.info("no voice bridge in status! " + status);
		return;
	    }

	    SoftphoneMessage sm = new SoftphoneMessage(bridgeInfo);
	    logger.info("Sending bridge info " + bridgeInfo 
		+ " to session " + session.getName() + " Id " 
		+ session.getSessionId());
	    session.send(sm.getBytes());
	} else if (status.getCode() == CallStatus.STARTEDSPEAKING) {
	    userGLORef.get(UserGLO.class).getAvatarCellRef().getForUpdate(AvatarCellGLO.class).setSpeakingStatus(AvatarP2PMessage.SpeakingStatus.TALKING);
	} else if (status.getCode() == CallStatus.STOPPEDSPEAKING) {
	    userGLORef.get(UserGLO.class).getAvatarCellRef().getForUpdate(AvatarCellGLO.class).setSpeakingStatus(AvatarP2PMessage.SpeakingStatus.SILENT);
	} else if (status.getCode() == CallStatus.ENDING) {
        userGLORef.get(UserGLO.class).getAvatarCellRef().getForUpdate(AvatarCellGLO.class).setSpeakingStatus(AvatarP2PMessage.SpeakingStatus.CALL_ENDED);
    } else if (status.getCode() == CallStatus.ANSWERED) {
        userGLORef.get(UserGLO.class).getAvatarCellRef().getForUpdate(AvatarCellGLO.class).setSpeakingStatus(AvatarP2PMessage.SpeakingStatus.CALL_STARTING);
    }
    }

    public void callBeginEndNotification(CallStatus status) {
	if (logger.isLoggable(Level.FINEST)) logger.log(Level.FINEST, "CallBeginEndNotification:  {0} for {1}", new Object[]{status, this});
    }

    @Override
    public void disconnected(boolean graceful) {
        super.disconnected(graceful);
        logger.severe("User Disconnected "+session.getName());

	VoiceHandler voiceHandler = VoiceHandlerImpl.getInstance();

	voiceHandler.endCall(name);

	deleteApp2DCells();

        if (userGLORef == null) {
            // this will happen if we never sent an AvatarSetupMessage
            return;
        }

        UserGLO userGLO = userGLORef.getForUpdate(UserGLO.class);
        ChannelManager chanMgr = AppContext.getChannelManager();
        Channel userChangeChannel = chanMgr.getChannel(ChannelInfo.USER_CHANGE);
        UserChangedMessage msg = new UserChangedMessage(
                UserChangedMessage.ActionType.USER_LEFT, 
                userGLO.getUserID().getBytes(), 
                userGLO.getUserName(),
                userGLO.getUserColor());
        userChangeChannel.send(msg.getBytes());
        userGLO.logout();
    }

    private CellGLO createApp2DCell (String appName, float xDim, float yDim, float zDim,
            Matrix4d cellOrigin, float pixelScaleX, float pixelScaleY, 
            Matrix4f viewRectMat, String connectionInfo ) {

        Point3d center = new Point3d();
        cellOrigin.transform(center);
        BoundingBox bounds = CellGLO.createBoundingBox(new Vector3d(center), xDim, yDim, zDim);
        
        DataManager dataMgr = AppContext.getDataManager();
	CellGLO appGLO = new SharedApp2DX11CellGLO(bounds, appName, cellOrigin, 
						pixelScaleX, pixelScaleY, 
						viewRectMat, connectionInfo);
        
        ManagedReference appRef = dataMgr.createReference(appGLO);
        MasterCellCacheGLO.getMasterCellCache().addCell(appRef);
	
        return appGLO;
    }
    
    // Clean up channels we have created

    protected void deleteApp2DCells() {
        MasterCellCacheGLO masterCache = MasterCellCacheGLO.getMasterCellCache();
        AppContext.getDataManager().markForUpdate(masterCache);
        DataManager dataMgr = AppContext.getDataManager();
	synchronized (nativeAppCells) {
	    for(ManagedReference cellRef : nativeAppCells) {
		try {
		    masterCache.deleteCell(cellRef);
		} catch (ObjectNotFoundException ex) {
		    continue;
		}
	    }
	}
    }

    @Override
    public void receivedMessage(byte[] data) {
        Message message = Message.extractMessage(data);
        
        if (message instanceof NativeApplicationMessage) 
            processNativeApplicationMessage((NativeApplicationMessage) message);

        // SMC has started
        if (message instanceof ServerMasterClientReadyMessage) {
	    System.err.println("Found ServerMasterClientReadyMessage");
            processServerMasterClientReadyMessage((ServerMasterClientReadyMessage)message);
	    return;
	}

        if (message instanceof SoftphoneMessage) 
	    processSoftphoneMessage((SoftphoneMessage) message);
        
        if (message instanceof AvatarSetupMessage)
            processAvatarSetupMessage((AvatarSetupMessage) message);
        
        if (message instanceof AvatarCellMessage)
            processAvatarCellMessage((AvatarCellMessage)message);
        
        if (message instanceof CellMessage)
            processCellMessage((CellMessage) message);
        
        if (message instanceof PingMessage)
            processPingMessage((PingMessage) message);
        
        if (message instanceof FileTransferMessage)
            processFileTransferMessage((FileTransferMessage)message);
        
        if (message instanceof ServerLaunchXAppMessage)
            processServerLaunchXAppMessage((ServerLaunchXAppMessage) message);
    }
    
    protected void processServerMasterClientReadyMessage(
		      ServerMasterClientReadyMessage message) {
        // this client is a server master client
        ServerMasterClientGLO.getServerMasterClientGLO().
                addServerMasterClient(session, message.getApps());
        setServerMasterClient(true);
    }
    
    private void processFileTransferMessage(FileTransferMessage message) {
        switch(message.getActionType()) {
        case TRANSFER_COMPLETE :
            break;
        case TRANSFER_DATA_PACKET :
            break;
        case TRANSFER_ERROR :
            break;
        case TRANSFER_REQUEST :
            UserGLO destUserGLO = UserGLO.getUserGLO(message.getDestinationUser());
            // TODO check user will accept file
            
            if (destUserGLO==null) {
                System.err.println("ERROR Unknown user for file transfer "+destUserGLO);
                session.send(new ErrorMessage("Unknown user for file transfer "+message.getDestinationUser()).getBytes());
                return;
            }
            
            Channel chan = AppContext.getChannelManager().createChannel(System.nanoTime()+ChannelInfo.FILE_TRANSFER, null,  Delivery.RELIABLE);
            
            chan.join(destUserGLO.getClientSession(), null);
            chan.join(session, null);
            
            chan.send(destUserGLO.getClientSession(), 
                      FileTransferMessage.newFileTransferSetupReceiverMessage(message.getFilename()).getBytes());
            chan.send(session, 
                      FileTransferMessage.newFileTransferSetupSenderMessage(message.getFilename()).getBytes());
            
            
            break;
        case TRANSFER_SMC_REQUEST :
            // Currently we only support a single ServerMasterClient
            Channel chanSmc = AppContext.getChannelManager().createChannel(System.nanoTime()+ChannelInfo.FILE_TRANSFER, null,  Delivery.RELIABLE);
            
            ClientSession clientSmc = ServerMasterClientGLO.getServerMasterClientGLO().getClientSession();
            if (clientSmc==null) {
                // TODO Handle failure
                logger.warning("Unhandled error, no SMC client for File transfer");
            } else {
                chanSmc.join(clientSmc, null);
                chanSmc.join(session, null);

                chanSmc.send(clientSmc, 
                          FileTransferMessage.newFileTransferSetupReceiverMessage(message.getFilename()).getBytes());
                chanSmc.send(session, 
                          FileTransferMessage.newFileTransferSetupSenderMessage(message.getFilename()).getBytes());
            }
            break;
        }
    }
    
    protected boolean processNativeApplicationMessage(NativeApplicationMessage message) {
        
	if (super.processNativeApplicationMessage(message))
	    return true;

	NativeApplicationMessage.ActionType action = message.getActionType();

	//logger.info("process native message:  " + message + " action " + action);

        switch (action) {

	case CREATE_APP2DCELL :
	     String connInfo = message.getConnectionInfo();
	     logger.info("Arg: " + action + " connInfo = " + connInfo);

	     DataManager dataMgr = AppContext.getDataManager();
	     Vector3f dim = message.getDimension();
	     CellGLO cellGLO = createApp2DCell(
	            message.getAppName(),
	            dim.x, dim.y, dim.z, 
		    message.getCellOrigin(), 
		    message.getPixelScaleX(),
		    message.getPixelScaleY(),
		    message.getViewRectMat(), 
		    message.getConnectionInfo());
	     CellID cellID = cellGLO.getCellID();
	     logger.info("Created App2D Cell cellID = " + cellID);
	     synchronized (nativeAppCells) {
		 nativeAppCells.add(dataMgr.createReference(cellGLO));
	     }

	     // Command reply: send the id of the cell created
             
	     session.send(NativeApplicationMessage.newNotifyAppCellIDMessage(cellID).getBytes());
	     break;

	case DELETE_APP2DCELL : {
	    MasterCellCacheGLO masterCache = MasterCellCacheGLO.getMasterCellCache();
	    CellID cellIdToDelete = message.getAppCellID();
	    synchronized (nativeAppCells) {
		// TODO: make nativeAppCells a HashMap rather than searching it sequentially
		for (ManagedReference cellRef : nativeAppCells) {
		    CellGLO cell = null;
		    try {
			cell = cellRef.get(CellGLO.class);
		    } catch (ObjectNotFoundException ex) {
			continue;
		    }
		    CellID cellId = cell.getCellID();
		    if (cellId.equals(cellIdToDelete)) {
			logger.info("Deleted native app cell " + cellId);
			masterCache.deleteCell(cellRef);
		    }
		}
	    }
	    break;
	}

	case SET_BOUNDS_APP2DCELL : {
	    MasterCellCacheGLO masterCache = MasterCellCacheGLO.getMasterCellCache();
	    CellID cellIdToSet = message.getAppCellID();
	    synchronized (nativeAppCells) {
		// TODO: make nativeAppCells a HashMap rather than searching it sequentially
		for (ManagedReference cellRef : nativeAppCells) {
		    CellGLO cell = null;
		    try {
			cell = cellRef.get(CellGLO.class);
		    } catch (ObjectNotFoundException ex) {
			continue;
		    }
		    CellID cellId = cell.getCellID();
		    if (cellId.equals(cellIdToSet)) {
			logger.info("SetBounds for native app cell " + cellId);
			if (cell instanceof MoveableCellGLO) {
			    logger.info("Bounds = " + message.getBounds());
			    MoveableCellGLO mCell = (MoveableCellGLO) cell;
			    mCell.setBounds(message.getBounds());
			} else {
			    logger.severe("Error: Cell is not moveable");
			}
		    }
		}
	    }
	    break;
	}

	default:
	     return false;
        }
        
        return true;
    }

    protected boolean processServerLaunchXAppMessage(ServerLaunchXAppMessage serverLaunchXAppMessage) {
        return ServerMasterClientGLO.getServerMasterClientGLO().launch(session, serverLaunchXAppMessage);
    }
    
    /*
     * This is the voice bridge that was given to the client to use.
     */
    private String bridgeInfo;

    protected boolean processSoftphoneMessage(SoftphoneMessage message) {
        SoftphoneMessage.ActionType action = message.getActionType();
 
	VoiceHandler voiceHandler = VoiceHandlerImpl.getInstance();

        String callId = name;

        switch (action) {
	case GET_BRIDGE:
        if(logger.isLoggable(Level.FINEST)) logger.log(Level.FINEST, "GET_BRIDGE on {0} with {1}", new Object[]{this, message});
	    bridgeInfo = voiceHandler.getVoiceBridge();
	    SoftphoneMessage sm = new SoftphoneMessage(bridgeInfo);
	    session.send(sm.getBytes());
	    break;
	
        case SETUP:
	    if (bridgeInfo == null) {
		logger.info("Unable to setup call, bridgeInfo is null");
		return false;
	    }

	    logger.info("Setup call:  callId=" + callId + ", sip url="
		+ message.getSipUrl() + ", bridgeInfo=" + bridgeInfo
		+ " position " + message.getPosition() + " direction "
		+ message.getDirection());

	    voiceHandler.addCallStatusListener(this, callId);

	    voiceHandler.setupCall(callId, message.getSipUrl(), bridgeInfo);

	    voiceHandler.setPositionAndOrientation(callId, 
                (double) message.getPosition().getX(), (double) message.getPosition().getY(), 
                (double) message.getPosition().getZ(),
                AvatarCellGLO.getAngle(message.getPosition(), message.getDirection()));

	    bridgeInfo = null;
	    break;

	case DISCONNECT_CALL:
	    voiceHandler.disconnectCall(callId);
	    break;
        
	default:
	     logger.info("Unknown softphone message type: " + action);
	     return false;
	}

	return true;
    }
    
    protected boolean processCellMessage(CellMessage message) {
        CellID cellID = message.getCellID();
        CellGLO cell = MasterCellCacheGLO.getMasterCellCache().findCell(cellID);
        if (cell != null) {
            try {
                ((CellMessageListener) cell).receivedMessage(session, message);
                return true;
            } catch (ClassCastException cce) {
                logger.log(Level.SEVERE, "Cell " + cellID + 
                           " does not implement CellMessageListener", cce);
            }
        }
        
        return false;
    }

    protected boolean processPingMessage(PingMessage message) {
        // echo back to client
        this.session.send(message.getBytes());
        return true;
    }
    
    private void processAvatarCellMessage(AvatarCellMessage message) {
        
        if (message.getActionType()==AvatarCellMessage.ActionType.AVATAR_MOVE) {
                if (logger.isLoggable(Level.FINEST)) {
                    logger.finest("AVATAR_MOVE: " + message.getSequence());
                }

		VoiceHandler voiceHandler = VoiceHandlerImpl.getInstance();

                if (voiceHandler!=null) {
                    Point3f position = message.getPosition(null);
                    Vector3f direction = message.getDirection(null);
                    UserGLO userGLO = UserGLO.getUserGLO(name);
                    
                    voiceHandler.setPositionAndOrientation(name, 
                        (double) position.getX(), (double) position.getY(), 
                        (double) position.getZ(),
                         AvatarCellGLO.getAngle(position, direction));
                }
        } else if (message.getActionType()==AvatarCellMessage.ActionType.AVATAR_MUTE) {
		VoiceHandler voiceHandler = VoiceHandlerImpl.getInstance();

                if (voiceHandler!=null) {
                    UserGLO userGLO = UserGLO.getUserGLO(name);

		    voiceHandler.muteCall(name, message.getIsMuted());	
            if(logger.isLoggable(Level.FINEST)) logger.log(Level.FINEST, "{0} setting MUTE with {1}", new Object[]{this, message});
            AvatarP2PMessage.SpeakingStatus speakingStatus;
            if (message.getIsMuted()) {
                speakingStatus = AvatarP2PMessage.SpeakingStatus.MUTE;
            } else {
                speakingStatus = AvatarP2PMessage.SpeakingStatus.SILENT;
            }
            userGLO.getAvatarCellRef().get(AvatarCellGLO.class).setSpeakingStatus(speakingStatus);
		}
        } else if (message.getActionType() == AvatarCellMessage.ActionType.AVATAR_WHISPER) {

            VoiceHandler voiceHandler = VoiceHandlerImpl.getInstance();

            if (voiceHandler!=null) {
                UserGLO userGLO = UserGLO.getUserGLO(name);

                //userGLO.getAvatarCellRef().get(AvatarCellGLO.class).setSpeakingStatus(speakingStatus);
                userGLO.getAvatarCellRef().get(AvatarCellGLO.class).setWhisperingStatus(message.getIsWhispering());
            }

        } else {       
            CellID cellID = message.getCellID();
            AvatarCellGLO avatarCellGLO = (AvatarCellGLO) MasterCellCacheGLO.getMasterCellCache().findCell(cellID);
            avatarCellGLO.processMessage(message);
        }
    }
}
