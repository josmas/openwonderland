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
package org.jdesktop.wonderland.client.cell;

import com.sun.sgs.client.SessionId;
import org.jdesktop.wonderland.client.comms.WonderlandSession;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.CellTransform;
import com.sun.sgs.client.ClientChannel;
import com.sun.sgs.client.ClientChannelListener;
import java.util.logging.Logger;

/**
 * A cell that can move
 * 
 * @author paulby
 */
public class EntityCell extends Cell {
    
    private MoveableCellChannelListener channelListener=null;
    private static Logger logger = Logger.getLogger(EntityCell.class.getName());

    public EntityCell(CellID cellID, String channelName, WonderlandSession session) {
        super(cellID, channelName, session);
    }
    
    /**
     * @{inheritDoc}
     * @param channel
     * @return
     */
    @Override
    protected ClientChannelListener createClientChannelListener(ClientChannel channel) {
        if (channelListener==null)
            channelListener = new MoveableCellChannelListener();
        return channelListener;
    }
    /**
     * Listen for move events from the server
     * @param listener
     */
    public void addRemoteCellMoveListener(CellMoveListener listener) {
        throw new RuntimeException("Not Implemented");
    }
    
    public interface CellMoveListener {
        public void cellMoved(CellTransform transform);
    }
    
    class MoveableCellChannelListener implements ClientChannelListener {

        public void receivedMessage(ClientChannel arg0, SessionId arg1, byte[] arg2) {
            logger.warning("MoveableCell recieveMessage");
        }

        public void leftChannel(ClientChannel arg0) {
            logger.info("Left channel");
        }
        
    }
}
