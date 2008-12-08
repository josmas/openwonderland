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

package org.jdesktop.wonderland.server.cell;

import com.sun.sgs.app.AppContext;
import com.sun.sgs.app.ClientSession;
import com.sun.sgs.app.ManagedReference;
import org.jdesktop.wonderland.common.cell.messages.CellMessage;
import org.jdesktop.wonderland.server.cell.ChannelComponentMO.ComponentMessageReceiver;
import org.jdesktop.wonderland.server.comms.WonderlandClientSender;

/**
 * An abstract base class that implements the channel component message
 * receiver interface and hides Darkstar implementation details from the
 * developer.
 * 
 * @author Jordan Slott <jslott@dev.java.net>
 */
public abstract class AbstractComponentMessageReceiver implements ComponentMessageReceiver {

    private ManagedReference<CellMO> cellRef = null;
    private ManagedReference<ChannelComponentMO> channelRef = null;
    /**
     * Constructor, takes the cell associated with the channel component
     */
    public AbstractComponentMessageReceiver(CellMO cellMO) {
        cellRef = AppContext.getDataManager().createReference(cellMO);
        ChannelComponentMO channelComponent = (ChannelComponentMO) cellMO.getComponent(ChannelComponentMO.class);
        if (channelComponent == null) {
            throw new IllegalStateException("Cell does not have a ChannelComponent");
        }
        channelRef = AppContext.getDataManager().createReference(channelComponent); 
    }
    
    /**
     * Returns the cell associated with this message receiver.
     * 
     * @return The CellMO
     */
    public CellMO getCell() {
        return cellRef.getForUpdate();
    }
    
    /**
     * Returns the channel component associated with this message receiver.
     * 
     * @return The ChannelComponentMO
     */
    public ChannelComponentMO getChannelComponent() {
        return channelRef.getForUpdate();
    }
    
    public abstract void messageReceived(WonderlandClientSender sender,
            ClientSession session, CellMessage message);
}
