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
package org.jdesktop.wonderland.server.app.simplewhiteboard;

import com.jme.bounding.BoundingVolume;
import com.sun.sgs.app.ClientSession;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.logging.Logger;
import org.jdesktop.wonderland.common.ExperimentalAPI;
import org.jdesktop.wonderland.common.app.simplewhiteboard.WhiteboardCompoundCellMessage;
import org.jdesktop.wonderland.common.app.simplewhiteboard.WhiteboardAction.Action;
import org.jdesktop.wonderland.common.app.simplewhiteboard.WhiteboardCellConfig;
import org.jdesktop.wonderland.common.app.simplewhiteboard.WhiteboardCommand.Command;
import org.jdesktop.wonderland.common.app.simplewhiteboard.WhiteboardTypeName;
import org.jdesktop.wonderland.common.cell.CellTransform;
import org.jdesktop.wonderland.common.cell.ClientCapabilities;
import org.jdesktop.wonderland.common.cell.config.CellConfig;
import org.jdesktop.wonderland.common.cell.messages.CellMessage;
import org.jdesktop.wonderland.server.app.base.App2DCellMO;
import org.jdesktop.wonderland.server.app.base.AppTypeCellMO;
import org.jdesktop.wonderland.server.app.base.AppTypeMO;
import org.jdesktop.wonderland.server.cell.ChannelComponentMO;
import org.jdesktop.wonderland.common.cell.setup.BasicCellSetup;
import org.jdesktop.wonderland.server.comms.WonderlandClientSender;
import org.jdesktop.wonderland.server.setup.BasicCellSetupHelper;

/**
 * A server cell associated with a whiteboard
 *
 * @author nsimpson,deronj
 */

@ExperimentalAPI
public class WhiteboardCellMO extends App2DCellMO
{
    private static final Logger logger = Logger.getLogger(WhiteboardCellMO.class.getName());
    
    // The messages list contains the current state of the whiteboard.
    // It's updated every time a client makes a change to the whiteboard
    // so that when new clients join, they receive the current state
    private static LinkedList<WhiteboardCompoundCellMessage> messages;
    private static WhiteboardCompoundCellMessage lastMessage;
    
    /** The communications component used to broadcast to all clients */
    private WhiteboardComponentMO commComponent;

    /** The preferred width (from the WFS file) */
    private int preferredWidth;

    /** The preferred height (from the WFS file) */
    private int preferredHeight;

    /** Default constructor, used when the cell is created via WFS */
    public WhiteboardCellMO() {
        this(null, null);
        addComponent(new ChannelComponentMO(this));
	commComponent = new WhiteboardComponentMO(this);
        addComponent(commComponent);
        messages = new LinkedList<WhiteboardCompoundCellMessage>();
    }

    /**
     * Creates a new instance of <code>WhiteboardCellMO</code> with the specified localBounds and transform.
     * If either parameter is null an IllegalArgumentException will be thrown.
     *
     * @param localBounds the bounds of the new cell, must not be null.
     * @param transform the transform for this cell, must not be null.
     */
    public WhiteboardCellMO (BoundingVolume localBounds, CellTransform transform) {
        super(localBounds, transform);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getClientCellClassName(ClientSession clientSession, ClientCapabilities capabilities) {
        return "org.jdesktop.wonderland.client.app.simplewhitebaord.WhiteboardCell";
    }

    /** 
     * {@inheritDoc}
     */
    public AppTypeMO getAppType () {
	return AppTypeCellMO.findAppType(WhiteboardTypeName.WHITEBOARD_APP_TYPE_NAME);
    }

    /** 
     * {@inheritDoc}
     */
    @Override
    protected CellConfig getCellConfig (ClientSession clientSession, ClientCapabilities capabilities) {
	WhiteboardCellConfig config = new WhiteboardCellConfig(pixelScale);
	config.setPreferredWidth(preferredWidth);
	config.setPreferredHeight(preferredHeight);
        return config;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setupCell(BasicCellSetup setupData) {
	super.setupCell(setupData);

	WhiteboardCellSetup setup = (WhiteboardCellSetup) setupData;
	preferredWidth = setup.getPreferredWidth();
	preferredHeight = setup.getPreferredHeight();
	pixelScale = setup.getPixelScale();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void reconfigureCell(BasicCellSetup setup) {
        super.reconfigureCell(setup);
        setupCell(setup);
    }

    /**
     * Return a new BasicCellSetup Java bean class that represents the current
     * state of the cell.
     * 
     * @return a JavaBean representing the current state
     */
    public BasicCellSetup getCellMOSetup() {

        /* Create a new BasicCellState and populate its members */
        WhiteboardCellSetup setup = new WhiteboardCellSetup();
	setup.setPixelScale(this.pixelScale);
        
        /* Set the bounds of the cell */
        BoundingVolume bounds = this.getLocalBounds();
        if (bounds != null) {
            setup.setBounds(BasicCellSetupHelper.getSetupBounds(bounds));
        }

        /* Set the origin, scale, and rotation of the cell */
        CellTransform transform = this.getLocalTransform(null);
        if (transform != null) {
            setup.setOrigin(BasicCellSetupHelper.getSetupOrigin(transform));
            setup.setRotation(BasicCellSetupHelper.getSetupRotation(transform));
            setup.setScaling(BasicCellSetupHelper.getSetupScaling(transform));
        }
        return setup;
    }

    /**
     * Process a message from a client.
     *
     * Sync message: send all accumulated messages back to the client (the sender).
     * All other messages: broadcast to <bold>all</bold> cells (including the sender!)
     *
     * @param clientSender The sender object for the client who sent the message.
     * @param clientSession The session for the client who sent the message.
     * @param message The message which was received.
     * @param commComponent The communications component that received the message.
     */
    public void receivedMessage(WonderlandClientSender clientSender, ClientSession clientSession, CellMessage message) {
        WhiteboardCompoundCellMessage cmsg = (WhiteboardCompoundCellMessage)message;
        logger.fine("received whiteboard message: " + cmsg);

        if (cmsg.getAction() == Action.REQUEST_SYNC) {
            logger.fine("sending " + messages.size() + " whiteboard sync messages");
            Iterator<WhiteboardCompoundCellMessage> iter = messages.iterator();
            
            while (iter.hasNext()) {
                WhiteboardCompoundCellMessage msg = iter.next();
		clientSender.send(clientSession, msg);
            }
        } else {

	    // Create the copy of the message to be broadcast to clients
            WhiteboardCompoundCellMessage msg = new WhiteboardCompoundCellMessage(cmsg.getClientID(), 
										  cmsg.getCellID(),
										  cmsg.getAction());
            switch (cmsg.getAction()) {
                case SET_TOOL:
                    // tool
                    msg.setTool(cmsg.getTool());
                    break;
                case SET_COLOR:
                    // color
                    msg.setColor(cmsg.getColor());
                    break;
                case MOVE_TO:
                case DRAG_TO:
                    // position
                    msg.setPositions(cmsg.getPositions());
                    break;
                case REQUEST_SYNC:
                    break;
                case EXECUTE_COMMAND:
                    // command
                    msg.setCommand(cmsg.getCommand());
                    break;
            }
            
            // record the message in setup data (move events are not recorded)
            if (cmsg.getAction() == Action.EXECUTE_COMMAND) {
                if (cmsg.getCommand() == Command.ERASE) {
                    // clear the action history
                    logger.fine("clearing message history");
                    messages.clear();
                }
            } else {
                if (cmsg.getAction() != Action.MOVE_TO) {
                    if ((lastMessage != null) &&
			lastMessage.getAction() == Action.MOVE_TO) {
                        messages.add(lastMessage);
                    }

		    // Must guarantee that the original sender doesn't ignore this when it is played back during a sync
		    cmsg.setClientID(null);

                    messages.add(cmsg);
                }
            }
            lastMessage = cmsg;

	    // Broadcast message to all clients (including the original sender of the message).
            commComponent.sendAllClients(msg);
        }
    }
}
