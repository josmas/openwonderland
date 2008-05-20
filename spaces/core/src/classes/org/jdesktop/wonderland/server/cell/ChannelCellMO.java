/**
 * Project Wonderland
 *
 * $Id$
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
 */

package org.jdesktop.wonderland.server.cell;

import com.sun.sgs.app.ClientSession;
import org.jdesktop.wonderland.common.cell.messages.CellMessage;
import org.jdesktop.wonderland.server.comms.WonderlandClientSender;

/**
 * A server side interface that tags a cell has having it's own channel
 * 
 * @author paulby
 */
public interface ChannelCellMO {
    /**
     * Cells that have a channel should overload this method to actually open the
     * channel. The convenience method defaultOpenChannel can be used to open the channel
     * with a default channel name. Called when the cell is made live.
     */
    public void openChannel();
    
    /**
     * Close the cells channel. Called when the cell is no longer live.
     */
    public void closeChannel();
    
    
    public void messageReceived(WonderlandClientSender sender,
                                   ClientSession session, 
                                   CellMessage message);
}
