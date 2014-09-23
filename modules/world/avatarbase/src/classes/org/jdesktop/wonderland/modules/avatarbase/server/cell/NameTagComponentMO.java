/**
 * Copyright (c) 2014, WonderBuilders, Inc., All Rights Reserved
 */

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
import org.jdesktop.wonderland.common.cell.ClientCapabilities;
import org.jdesktop.wonderland.common.cell.messages.CellMessage;
import org.jdesktop.wonderland.common.cell.state.CellComponentClientState;
import org.jdesktop.wonderland.common.cell.state.CellComponentServerState;
import org.jdesktop.wonderland.modules.avatarbase.common.cell.NameTagComponentClientState;
import org.jdesktop.wonderland.modules.avatarbase.common.cell.NameTagComponentServerState;
import org.jdesktop.wonderland.modules.avatarbase.common.cell.messages.NameTagMessage;
import org.jdesktop.wonderland.server.cell.CellComponentMO;
import org.jdesktop.wonderland.server.cell.CellMO;
import org.jdesktop.wonderland.server.cell.ChannelComponentMO;
import org.jdesktop.wonderland.server.cell.annotation.UsesCellComponentMO;
import org.jdesktop.wonderland.server.comms.WonderlandClientID;
import org.jdesktop.wonderland.server.comms.WonderlandClientSender;

/**
 * Server side component for name tags. The only purpose of class at the moment
 * is to allow the avatarbase to register it's client NameTagComponent. This server
 * side class does not track or allow changes to be made to the status of the name tag.
 * It may be enhanced in the future....
 *
 * @author paulby
 * @author Abhishek Upadhyay
 */
public class NameTagComponentMO extends CellComponentMO {

    private boolean isMuted;
    @UsesCellComponentMO(ChannelComponentMO.class)
    protected ManagedReference<ChannelComponentMO> channelComponentRef = null;
    private ManagedReference<CellMO> cellRef = null;
    
    public NameTagComponentMO(CellMO cell) {
        super(cell);
        this.cellRef = AppContext.getDataManager().createReference(cell);
    }

    @Override
    protected String getClientClass() {
        return "org.jdesktop.wonderland.modules.avatarbase.client.jme.cellrenderer.NameTagComponent";
    }
    
    /**
     * Get the client state for this component
     */
    @Override
    public CellComponentClientState getClientState(CellComponentClientState state,
                                                   WonderlandClientID clientID,
                                                   ClientCapabilities capabilities)
    {
        // if an existing state is not passed in from a subclass, create one
        // ourselves
        if (state == null) {
            state = new NameTagComponentClientState();
        }
        ((NameTagComponentClientState)state).setIsMuted(isMuted);
        
        // do any configuration necessary
        //((BestViewClientState) state).setXXX();

        // pass the state we created up to the superclass to add any other
        // necessary properties
        return super.getClientState(state, clientID, capabilities);
    }

    /**
     * Get the server state for this component
     */
    @Override
    public CellComponentServerState getServerState(CellComponentServerState state) {
        // if an existing state is not passed in from a subclass, create one
        // ourselves
        if (state == null) {
            state = new NameTagComponentServerState();
        }
        ((NameTagComponentServerState)state).setIsMuted(isMuted);
        
        
        // do any configuration necessary
        // ((BestViewServerState) state).setXXX();

        // pass the state we created up to the superclass to add any other
        // necessary properties
        return super.getServerState(state);
    }
    
    /**
     * Handle when the system sets our server state, for example when
     * restoring from WFS
     */
    @Override
    public void setServerState(CellComponentServerState state) {
        // pass the state object to the superclass for further processing
        super.setServerState(state);
        isMuted = ((NameTagComponentServerState)state).isIsMuted();
        
    }
    
    @Override
    public void setLive(boolean live) {
        super.setLive(live);

        // Otherwise, either add or remove the message receiver to listen for
        // avatar configuration events
        ChannelComponentMO channel = channelComponentRef.getForUpdate();
        if (live) {
            AvatarNameTagMessageReceiver recv = new AvatarNameTagMessageReceiver(cellRef,channel);
            channel.addMessageReceiver(NameTagMessage.class, recv);
        } else {
            channel.removeMessageReceiver(NameTagMessage.class);
        }
    }
    
    public void sentToAll() {
        
    }
    
    /**
     * Handles messages for the avatar name tag.
     */
    private static class AvatarNameTagMessageReceiver implements ChannelComponentMO.ComponentMessageReceiver {

        private ManagedReference<CellMO> cellRef = null;
        private ChannelComponentMO channel = null;
        
        public AvatarNameTagMessageReceiver(ManagedReference<CellMO> cellRef, ChannelComponentMO channel) {
            this.cellRef = cellRef;
            this.channel = channel;
        }
        
        public void messageReceived(WonderlandClientSender sender, WonderlandClientID clientID
                , CellMessage message) {
            //set cellComp server state.
            NameTagComponentMO cellCompMO = cellRef.getForUpdate().getComponent(NameTagComponentMO.class);
            NameTagComponentServerState ntss = new NameTagComponentServerState();
            ntss.setIsMuted(((NameTagMessage)message).isIsMute());
            cellCompMO.setServerState(ntss);
            channel.sendAll(clientID, message);
        }

        public void recordMessage(WonderlandClientSender sender, WonderlandClientID clientID
                , CellMessage message) {
            //empty
        }
        
    }

}
