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
package org.jdesktop.wonderland.modules.avatarbase.client.cell;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.cell.CellComponent;
import org.jdesktop.wonderland.client.cell.ChannelComponent;
import org.jdesktop.wonderland.client.cell.annotation.UsesCellComponent;
import org.jdesktop.wonderland.client.cell.asset.AssetUtils;
import org.jdesktop.wonderland.client.comms.WonderlandSession;
import org.jdesktop.wonderland.client.login.ServerSessionManager;
import org.jdesktop.wonderland.common.cell.CellStatus;
import org.jdesktop.wonderland.common.cell.messages.CellMessage;
import org.jdesktop.wonderland.common.cell.state.CellComponentClientState;
import org.jdesktop.wonderland.modules.avatarbase.common.cell.messages.AvatarConfigComponentClientState;
import org.jdesktop.wonderland.modules.avatarbase.common.cell.messages.AvatarConfigMessage;

/**
 *
 * @author paulby
 */
public class AvatarConfigComponent extends CellComponent {

    private String avatarConfigName;
    private URL avatarConfigURL;

    @UsesCellComponent
    protected ChannelComponent channelComp;
    protected ChannelComponent.ComponentMessageReceiver msgReceiver=null;

    private LinkedList<AvatarConfigChangeListener> avatarChangeListeners = new LinkedList();

    private URL pendingChange = null;

    public AvatarConfigComponent(Cell cell) {
        super(cell);
    }

    public String getAvatarConfigName() {
        return avatarConfigName;
    }


    @Override
    public void setClientState(CellComponentClientState clientState) {
        super.setClientState(clientState);
        try {
            String str = ((AvatarConfigComponentClientState) clientState).getConfigURL();
            if (str!=null) {
                if (str.startsWith("assets")) {
                    // FOR NPC
                    WonderlandSession session = cell.getCellCache().getSession();
                    ServerSessionManager manager = session.getSessionManager();
                    String serverHostAndPort = manager.getServerNameAndPort();
                    avatarConfigURL = AssetUtils.getAssetURL("wla://avatarbaseart@" + serverHostAndPort + "/"+str, cell);
//                    System.err.println("------> NPC URL "+avatarConfigURL);
                } else
                    avatarConfigURL = new URL(str);
            } else
                avatarConfigURL = null;
        } catch (MalformedURLException ex) {
            Logger.getLogger(AvatarConfigComponent.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void setStatus(CellStatus status) {
        super.setStatus(status);
        switch (status) {
            case DISK:
                if (msgReceiver != null && channelComp != null) {
                    channelComp.removeMessageReceiver(getMessageClass());
                    msgReceiver = null;
                }
                break;
            case INACTIVE: {
                if (msgReceiver == null) {
                    msgReceiver = new ChannelComponent.ComponentMessageReceiver() {

                        public void messageReceived(CellMessage message) {
                                notifyConfigUpdate((AvatarConfigMessage) message);
                        }
                    };
                    channelComp.addMessageReceiver(getMessageClass(), msgReceiver);
                    synchronized(this) {
                        if (pendingChange!=null) {
                            channelComp.send(AvatarConfigMessage.newRequestMessage(pendingChange));
                            pendingChange = null;
                        }
                    }
                }
            }
        }
    }

    private void notifyConfigUpdate(AvatarConfigMessage msg) {
        if ((avatarConfigURL!=null && avatarConfigURL.toExternalForm().equals(msg.getModelConfigURL())
                || msg.getModelConfigURL()==null))
            return;

        try {
            avatarConfigURL = new URL(msg.getModelConfigURL());
            synchronized(avatarChangeListeners) {
                for(AvatarConfigChangeListener l : avatarChangeListeners) {
                    l.AvatarConfigChanged(msg);
                }
            }
        } catch (MalformedURLException ex) {
            Logger.getLogger(AvatarConfigComponent.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public URL getAvatarConfigURL() {
        return avatarConfigURL;
    }

    public void addAvatarConfigChageListener(AvatarConfigChangeListener listener) {
        synchronized(avatarChangeListeners) {
            avatarChangeListeners.add(listener);
        }
    }

    public void requestConfigChange(URL configURL) {
        if (avatarConfigURL!=null && avatarConfigURL.equals(configURL))
            return;

        // This can be called before channelComp is initialzed

        synchronized(this) {
            if (channelComp==null) {
                pendingChange = configURL;
            } else {
                channelComp.send(AvatarConfigMessage.newRequestMessage(configURL));
            }
        }
    }

    /**
     * @return the class of the message this component handles.
     */
    protected Class getMessageClass() {
        return AvatarConfigMessage.class;
    }

    public interface AvatarConfigChangeListener {
        public void AvatarConfigChanged(AvatarConfigMessage msg);
    }
}
