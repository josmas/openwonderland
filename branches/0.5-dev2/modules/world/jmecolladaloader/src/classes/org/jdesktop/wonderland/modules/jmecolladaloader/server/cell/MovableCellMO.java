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
package org.jdesktop.wonderland.modules.jmecolladaloader.server.cell;

import org.jdesktop.wonderland.server.cell.*;
import com.jme.bounding.BoundingVolume;
import com.sun.sgs.app.ClientSession;
import org.jdesktop.wonderland.common.cell.CellTransform;
import org.jdesktop.wonderland.common.cell.ClientCapabilities;
import org.jdesktop.wonderland.common.cell.config.CellConfig;
import org.jdesktop.wonderland.server.comms.WonderlandClientID;

/**
 * For cells that are expected to move frequently
 * 
 * TODO - Do we need a listener that allows veto of a move request, don't
 * think so instead I suggest we would subclass to add veto capability
 * 
 * @deprecated
 * @author paulby
 */
public class MovableCellMO extends JmeColladaCellMO {

    public MovableCellMO(BoundingVolume bounds, CellTransform transform) {
        super(bounds, transform);
        addComponent(new ChannelComponentMO(this));
        addComponent(new MovableComponentMO(this));
    }
    

    @Override protected String getClientCellClassName(WonderlandClientID clientID,ClientCapabilities capabilities) {
        return "org.jdesktop.wonderland.modules.jmecolladaloader.client.cell.MovableCell";
    }
    
    @Override
    public CellConfig getCellConfig(WonderlandClientID clientID, ClientCapabilities capabilities) {
        CellConfig ret = super.getCellConfig(clientID, capabilities);
        ret.addClientComponentClasses(new String[] {
            "org.jdesktop.wonderland.client.cell.ChannelComponent",
            "org.jdesktop.wonderland.client.cell.MovableComponent"
        });
        
        return ret;
    }


}
