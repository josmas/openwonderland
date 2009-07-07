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
package org.jdesktop.wonderland.modules.avatarbase.server.cell;

import com.sun.sgs.app.AppContext;
import com.sun.sgs.app.ManagedReference;
import java.util.logging.Logger;
import org.jdesktop.wonderland.common.cell.ClientCapabilities;
import org.jdesktop.wonderland.common.cell.messages.CellMessage;
import org.jdesktop.wonderland.common.cell.state.CellComponentClientState;
import org.jdesktop.wonderland.common.cell.state.CellComponentServerState;
import org.jdesktop.wonderland.modules.avatarbase.common.cell.messages.AvatarConfigComponentClientState;
import org.jdesktop.wonderland.modules.avatarbase.common.cell.messages.AvatarConfigComponentServerState;
import org.jdesktop.wonderland.modules.avatarbase.common.cell.messages.AvatarConfigMessage;
import org.jdesktop.wonderland.server.cell.CellComponentMO;
import org.jdesktop.wonderland.server.cell.CellMO;
import org.jdesktop.wonderland.server.cell.ChannelComponentMO;
import org.jdesktop.wonderland.server.cell.ChannelComponentMO.ComponentMessageReceiver;
import org.jdesktop.wonderland.server.cell.annotation.UsesCellComponentMO;
import org.jdesktop.wonderland.server.comms.WonderlandClientID;
import org.jdesktop.wonderland.server.comms.WonderlandClientSender;

/**
 *
 * @author paulby
 */
public class AvatarConfigComponentMO extends CellComponentMO {

    @UsesCellComponentMO(ChannelComponentMO.class)
    protected ManagedReference<ChannelComponentMO> channelComponentRef = null;
    private boolean live;
    private String avatarConfig=null;

    public AvatarConfigComponentMO(CellMO cell) {
        super(cell);
        
    }

    @Override
    protected String getClientClass() {
        return "org.jdesktop.wonderland.modules.avatarbase.client.cell.AvatarConfigComponent";
    }

    protected Class getMessageClass() {
        return AvatarConfigMessage.class;
    }

    @Override
    public void setLive(boolean live) {
        if (this.live==live)
            return;

        this.live = live;
        if (live) {
            channelComponentRef.getForUpdate().addMessageReceiver(getMessageClass(), new ComponentMessageReceiverImpl(this));
        } else {
            channelComponentRef.getForUpdate().removeMessageReceiver(getMessageClass());
        }
    }

    public void handleMessage(WonderlandClientID clientID, AvatarConfigMessage msg) {

        avatarConfig = msg.getModelConfigURL();

        if (live) {
            channelComponentRef.getForUpdate().sendAll(clientID, AvatarConfigMessage.newApplyMessage(msg));
        }
    }

    @Override
    public CellComponentClientState getClientState(CellComponentClientState clientState, WonderlandClientID clientID, ClientCapabilities capabilities) {
        if (clientState == null) {
            clientState = new AvatarConfigComponentClientState();
        }
        ((AvatarConfigComponentClientState)clientState).setConfigURL(avatarConfig);
        return super.getClientState(clientState, clientID, capabilities);
    }

    @Override
    public CellComponentServerState getServerState(CellComponentServerState state) {
        if (state == null) {
            state = new AvatarConfigComponentServerState();
        }
        ((AvatarConfigComponentServerState)state).setAvatarConfigURL(avatarConfig);
        return super.getServerState(state);
    }

    @Override
    public void setServerState(CellComponentServerState state) {
        super.setServerState(state);
        avatarConfig = ((AvatarConfigComponentServerState)state).getAvatarConfigURL();
    }

    private static class ComponentMessageReceiverImpl implements ComponentMessageReceiver {

        private ManagedReference<AvatarConfigComponentMO> compRef;

        public ComponentMessageReceiverImpl(AvatarConfigComponentMO comp) {
            compRef = AppContext.getDataManager().createReference(comp);
        }

        public void messageReceived(WonderlandClientSender sender, WonderlandClientID clientID, CellMessage message) {
            AvatarConfigMessage ent = (AvatarConfigMessage) message;

            switch (ent.getActionType()) {
                case REQUEST:
                    // TODO check permisions

                    compRef.getForUpdate().handleMessage(clientID, ent);

                    // Only need to send a response if the change can not be completed as requested
                    //sender.send(session, MovableMessageResponse.newMoveModifiedMessage(ent.getMessageID(), ent.getTranslation(), ent.getRotation()));
                    break;
                case APPLY:
                    Logger.getAnonymousLogger().severe("Server should never receive APPLY messages");
                    break;
            }
        }

         /**
         * Record the message -- part of the event recording mechanism.
         * Nothing more than the message is recorded in this implementation, delegate it to the recorder manager
         */
        public void recordMessage(WonderlandClientSender sender, WonderlandClientID clientID, CellMessage message) {
//            RecorderManager.getDefaultManager().recordMessage(sender, clientID, message);
        }
    }
}
