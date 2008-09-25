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
package org.jdesktop.wonderland.server.simplewhiteboard;

import com.sun.sgs.app.ClientSession;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;
import java.util.logging.Logger;
import com.jme.bounding.BoundingVolume;
import javax.vecmath.Matrix4d;
import org.jdesktop.wonderland.server.app.base.App2DCellGLO;
import org.jdesktop.wonderland.server.app.base.AppTypeCellGLO;
import org.jdesktop.wonderland.server.app.base.AppTypeGLO;
import org.jdesktop.wonderland.common.app.simplewhiteboard.CompoundWhiteboardCellMessage;
import org.jdesktop.wonderland.common.app.simplewhiteboard.WhiteboardAction.Action;
import org.jdesktop.wonderland.common.app.simplewhiteboard.WhiteboardCommand.Command;
import org.jdesktop.wonderland.common.app.simplewhiteboard.WhiteboardTypeName;

/**
 * A server cell associated with a whiteboard
 *
 * @author nsimpson,deronj
 */

@ExperimentalAPI
public class WhiteboardCellGLO 
    extends App2DCellMO
    implements CellMessageListener 
{
    private static final Logger logger = Logger.getLogger(WhiteboardCellMO.class.getName());
    
    // The messages list contains the current state of the whiteboard.
    // It's updated every time a client makes a change to the whiteboard
    // so that when new clients join, they receive the current state
    private static LinkedList<CompoundWhiteboardCellMessage> messages;
    private static CompoundWhiteboardCellMessage lastMessage;
    
    /** The communications component used to broadcast to all clients */
    private WhiteboardComponentMO commComponent;

    /** Default constructor, used when the cell is created via WFS */
    public WhiteboardCellMO() {
        this(null, null);
        addComponent(new ChannelComponentMO(this));
	commComponent = new WhiteboardComponentMO(this);
        addComponent(commComponent);
        messages = new LinkedList<CompoundWhiteboardCellMessage>();
    }

    /** Returns the class name for the corresponding client cells */
    public String getClientCellClassName() {
        return "org.jdesktop.wonderland.client.app.whiteboard.WhiteboardCell";
    }

    /** 
     * {@inheritDoc}
     */
    public AppTypeMO getAppType () {
	return AppTypeCellMO.findAppType(WhiteboardTypeName.WHITEBOARD_APP_TYPE_NAME);
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
        CompoundWhiteboardCellMessage cmsg = (CompoundWhiteboardCellMessage)message;
        logger.fine("received whiteboard message: " + cmsg);

        if (cmsg.getAction() == Action.REQUEST_SYNC) {
            logger.fine("sending " + messages.size() + " whiteboard sync messages");
            Iterator<CompoundWhiteboardCellMessage> iter = messages.iterator();
            
            while (iter.hasNext()) {
                CompoundWhiteboardCellMessage msg = iter.next();
		clientSender.send(session, msg);
            }
        } else {

	    // Create the copy of the message to be broadcast to clients
            CompoundWhiteboardCellMessage msg = new CompoundWhiteboardCellMessage(cmsg.getAction());
	    msg.setClientID(cmsg.getClientID());
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
		    accumulatedMessage = true;
                }
            }
            lastMessage = cmsg;

	    // Broadcast message to all clients (including the original sender of the message).
            commComponent.sendAllClients(msg);
        }
    }
}
