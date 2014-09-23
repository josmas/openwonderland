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
package org.jdesktop.wonderland.modules.avatarbase.client.jme.cellrenderer;

import com.jme.scene.Node;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.cell.CellComponent;
import org.jdesktop.wonderland.client.cell.ChannelComponent;
import org.jdesktop.wonderland.client.cell.annotation.UsesCellComponent;
import org.jdesktop.wonderland.client.cell.view.ViewCell;
import org.jdesktop.wonderland.common.cell.CellStatus;
import org.jdesktop.wonderland.common.cell.messages.CellMessage;
import org.jdesktop.wonderland.common.cell.state.CellComponentClientState;
import org.jdesktop.wonderland.modules.avatarbase.client.jme.cellrenderer.AvatarNameEvent.EventType;
import org.jdesktop.wonderland.modules.avatarbase.common.cell.NameTagComponentClientState;
import org.jdesktop.wonderland.modules.avatarbase.common.cell.messages.NameTagMessage;

/**
 * A NameTag for an avatar cell. Currently only supports 3D name tags, but
 * will be extended to support other forms of tag
 *
 * @author paulby
 * @author Abhishek Upadhyay
 */
public class NameTagComponent extends CellComponent {

    private NameTagNode nameTagNode=null;

    private String username = null;
    private String usernameAlias = null;
    private boolean inConeOfSilence = false;
    private boolean isSpeaking = false;
    private float heightAbove;
    private boolean isMuted;
    
    @UsesCellComponent
    protected ChannelComponent channelComp;
    protected ChannelComponent.ComponentMessageReceiver msgReceiver = null;
    
    public NameTagComponent(Cell cell) {
        this (cell, 2f);
    }

    public NameTagComponent(Cell cell, float heightAbove) {
        super (cell);

        this.heightAbove = heightAbove;
    }

    /**
     * Return the renderer for the name tag node. This should return a CellRenderer.
     * Fix in next release
     * @return
     */
    public Node getRenderer(Cell.RendererType rendererType) {
        assert(rendererType==Cell.RendererType.RENDERER_JME);
        return nameTagNode;
    }
    
    NameTagNode getNameTagNode() {
        return nameTagNode;
    }
    
    /**
     * Configure the component based on the client state that was
     * passed in.
     */
    @Override
    public void setClientState(CellComponentClientState clientState) {
        
        // allow the superclass to do any configuration necessary
        super.setClientState(clientState);
        isMuted = ((NameTagComponentClientState)clientState).isIsMuted();
    }

    @Override
    protected void setStatus(CellStatus status, boolean increasing) {
        switch(status) {
            case INACTIVE :
                if (increasing) {
                    synchronized(this) {
                        if (nameTagNode==null) {
                            nameTagNode = new NameTagNode(((ViewCell)cell).getIdentity().getUsername(), 
                                                            heightAbove,
                                                            inConeOfSilence,
                                                            isSpeaking,
                                                            isMuted);
                        }
                    }
                }
                break;
            case DISK :
                if (!increasing) {
                    synchronized(this) {
                        if (nameTagNode!=null)
                             nameTagNode.done();
                        nameTagNode = null;
                        if (msgReceiver != null && channelComp != null) {
                            channelComp.removeMessageReceiver(NameTagMessage.class);
                            msgReceiver = null;
                        }
                    }
                }
                break;
            case ACTIVE:
                if(increasing) {
                     if (msgReceiver == null) {
                        msgReceiver = new ChannelComponent.ComponentMessageReceiver() {
                            public void messageReceived(CellMessage message) {
                                //handleConfigMessage((AvatarConfigMessage) message);
                                NameTagMessage msg = (NameTagMessage) message;
                                updateLabel(msg.getUsername(), false, false, msg.isIsMute());
                                //updateLabel()
                            }
                        };
                        channelComp.addMessageReceiver(NameTagMessage.class, msgReceiver);
                     }
                }
                break;
        }
    }

    public void updateLabel(String usernameAlias, boolean inConeOfSilence, boolean isSpeaking,
	    boolean isMuted) {
        this.usernameAlias = usernameAlias;
        this.inConeOfSilence = inConeOfSilence;
        this.isSpeaking = isSpeaking;
        this.isMuted = isMuted;
        synchronized(this) {
            if (nameTagNode!=null)
                nameTagNode.updateLabel(usernameAlias, inConeOfSilence, isSpeaking, isMuted);
        }
    }

    public void setNameTag(EventType eventType, String username, String alias) {
        synchronized(this) {
            this.username = username;
            this.usernameAlias = alias;
            if (nameTagNode!=null) {
                nameTagNode.setNameTag(eventType, username, alias);
            }
        }
    }
}
