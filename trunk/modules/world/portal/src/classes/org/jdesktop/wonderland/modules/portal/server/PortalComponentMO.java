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

package org.jdesktop.wonderland.modules.portal.server;

import java.util.logging.Logger;
import org.jdesktop.wonderland.common.cell.ClientCapabilities;
import org.jdesktop.wonderland.common.cell.state.CellComponentClientState;
import org.jdesktop.wonderland.common.cell.state.CellComponentServerState;
import org.jdesktop.wonderland.common.cell.state.PositionComponentServerState.Translation;
import org.jdesktop.wonderland.common.cell.state.PositionComponentServerState.Rotation;
import org.jdesktop.wonderland.modules.portal.common.PortalComponentClientState;
import org.jdesktop.wonderland.modules.portal.common.PortalComponentServerState;
import org.jdesktop.wonderland.server.cell.CellComponentMO;
import org.jdesktop.wonderland.server.cell.CellMO;
import org.jdesktop.wonderland.server.comms.WonderlandClientID;

/**
 * A sample cell component
 *
 * @author Jordan Slott <jslott@dev.java.net>
 */
public class PortalComponentMO extends CellComponentMO {

    private static Logger logger = Logger.getLogger(PortalComponentMO.class.getName());
    
    private String serverURL;
    private Translation location;
    private Rotation look;

    public PortalComponentMO(CellMO cell) {
        super(cell);
    }

    @Override
    protected String getClientClass() {
        return "org.jdesktop.wonderland.modules.portal.client.PortalComponent";
    }

    @Override
    public CellComponentClientState getClientState(CellComponentClientState state, WonderlandClientID clientID, ClientCapabilities capabilities) {
        if (state == null) {
            state = new PortalComponentClientState();
        }

        ((PortalComponentClientState)state).setServerURL(serverURL);
        ((PortalComponentClientState)state).setLocation(location);
        ((PortalComponentClientState)state).setLook(look);

        return super.getClientState(state, clientID, capabilities);
    }

    @Override
    public CellComponentServerState getServerState(CellComponentServerState state) {
        if (state == null) {
            state = new PortalComponentServerState();
        }

        ((PortalComponentServerState)state).setServerURL(serverURL);
        ((PortalComponentServerState)state).setLocation(location);
        ((PortalComponentServerState)state).setLook(look);

        return super.getServerState(state);
    }

    @Override
    public void setServerState(CellComponentServerState state) {
        super.setServerState(state);

        serverURL = ((PortalComponentServerState) state).getServerURL();
        location = ((PortalComponentServerState) state).getLocation();
        look = ((PortalComponentServerState) state).getLook();
    }
}
