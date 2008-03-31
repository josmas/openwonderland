/**
 * Project Wonderland
 *
 * $RCSfile: LogControl.java,v $
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
 * $Revision: 1.3 $
 * $Date: 2007/10/23 18:27:41 $
 * $State: Exp $
 */
package org.jdesktop.wonderland.client.cell;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.wonderland.ExperimentalAPI;
import org.jdesktop.wonderland.client.ClientContext;
import org.jdesktop.wonderland.client.comms.BaseHandler;
import org.jdesktop.wonderland.client.comms.ResponseListener;
import org.jdesktop.wonderland.common.cell.CellChannelHandlerType;
import org.jdesktop.wonderland.common.cell.messages.CellMessage;
import org.jdesktop.wonderland.common.comms.HandlerType;
import org.jdesktop.wonderland.common.messages.Message;
import org.jdesktop.wonderland.common.messages.ResponseMessage;

/**
 * Client that received cell information
 * @author jkaplan
 */
@ExperimentalAPI
public class CellChannelHandler extends BaseHandler {
    private static CellChannelHandler cellClient = null;
    private static Logger logger = Logger.getLogger(CellChannelHandler.class.getName());
    
    public CellChannelHandler() {
        super();
        cellClient = this;
    }
    
    public static CellChannelHandler getCellClient() {
        return cellClient;
    }
    
    /**
     * Get the type of client
     * @return CellChannelHandlerType.CELL_CLIENT_TYPE
     */
    public HandlerType getClientType() {
        return CellChannelHandlerType.CLIENT_TYPE;
    }

    /**
     * Send a cell message to a specific cell on the server
     * @see org.jdesktop.wonderland.client.comms.WonderlandSession#send(WonderlandClient, Message)
     * 
     * @param message the cell message to send
     */
    public void send(CellMessage message) {
        super.send(message);
    }
    
    /**
     * Send a cell messag to a specific cell on the server with the given
     * listener.
     * @see org.jdesktop.wonderland.client.comms.WonderlandSession#send(WonderlandClient, Message, ResponseListener)
     * 
     * @param message the message to send
     * @param listener the response listener to notify when a response
     * is received.
     */
    public void send(CellMessage message, ResponseListener listener) {
        super.send(message, listener);
    }
    
    /**
     * Send a cell messag to a specific cell on the server and wait for a 
     * response.
     * @see org.jdesktop.wonderland.client.comms.WonderlandSession#sendAndWait(WonderlandClient, Message)
     * 
     * @param message the message to send
     * @throws InterruptedException if there is a problem sending a message
     * to the given cell
     */
    public ResponseMessage sendAndWait(CellMessage message)
        throws InterruptedException
    {
        return super.sendAndWait(message);
    }
    
    /**
     * Handle a message from the server
     * @param message the message to handle
     */
    public void handleMessage(Message message) {
        if (logger.isLoggable(Level.FINEST)) {
            logger.finest("Handling Message "+message.getClass().getName());
        }
        
        CellMessage cellMessage = (CellMessage)message;
        CellCache cellCache = ClientContext.getCellCache(getSession());
        
        if (cellCache==null) {
            logger.severe("Unable to deliver CellMessage, CellCache is null");
            return;
        }
        
        Cell cell = cellCache.getCell(cellMessage.getCellID());
        if (cell==null) {
            logger.warning("Unable to deliver CellMessage, cell not available on this client");
            return;
        }
        if (!(cell instanceof ChannelCell)) {
            logger.severe("Attempting to deliver message to cell that does not implement ChannelCell");
            throw new RuntimeException("Illegal message target");
        }
        
        ((ChannelCell)cell).handleMessage(cellMessage);
    }
}
