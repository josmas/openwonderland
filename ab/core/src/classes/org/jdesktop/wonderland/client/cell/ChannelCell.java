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

import org.jdesktop.wonderland.common.ExperimentalAPI;
import org.jdesktop.wonderland.common.cell.messages.CellMessage;

/**
 * Any cell which has it's own channel must implement this interface
 * 
 * @author paulby
 */
@ExperimentalAPI
public interface ChannelCell {

    /**
     * Notification of the CellChannelConnection to use when sending
     * data to the server for this cell.  This method will be called 
     * automatically at cell creation time.
     */
    public void setCellChannelConnection(CellChannelConnection connection);
    
    /**
     * Handle a cell message sent to this cell
     * @param message
     */
    public void handleMessage(CellMessage message);
    
    // TODO various send methods required, cell to server, cell to cell, cell to channel
    // Not sure these need to be defined in this interface, implementors should have
    // the choice of which send messages to implement and expose (if any) in a cell.
//    public void send(CellMessage message);
}
