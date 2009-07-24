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

package org.jdesktop.wonderland.modules.sample.server;

import java.util.logging.Logger;
import org.jdesktop.wonderland.common.cell.ClientCapabilities;
import org.jdesktop.wonderland.common.cell.state.CellComponentClientState;
import org.jdesktop.wonderland.common.cell.state.CellComponentServerState;
import org.jdesktop.wonderland.modules.sample.common.SampleCellSubComponentClientState;
import org.jdesktop.wonderland.modules.sample.common.SampleCellSubComponentServerState;
import org.jdesktop.wonderland.server.cell.CellComponentMO;
import org.jdesktop.wonderland.server.cell.CellMO;
import org.jdesktop.wonderland.server.comms.WonderlandClientID;

/**
 * A sample cell sub-component
 *
 * @author Jordan Slott <jslott@dev.java.net>
 */
public class SampleCellSubComponentMO extends CellComponentMO {

    private static Logger logger = Logger.getLogger(SampleCellSubComponentMO.class.getName());
    private String info = null;

    public SampleCellSubComponentMO(CellMO cell) {
        super(cell);
    }

    @Override
    protected String getClientClass() {
        return "org.jdesktop.wonderland.modules.sample.client.SampleCellSubComponent";
    }

    @Override
    protected void setLive(boolean live) {
        super.setLive(live);
        logger.warning("Setting SampleCellSubComponentMO to live = " + live);
    }


    @Override
    public CellComponentClientState getClientState(CellComponentClientState state, WonderlandClientID clientID, ClientCapabilities capabilities) {
        if (state == null) {
            state = new SampleCellSubComponentClientState();
        }
        ((SampleCellSubComponentClientState)state).setInfo(info);
        return super.getClientState(state, clientID, capabilities);
    }

    @Override
    public CellComponentServerState getServerState(CellComponentServerState state) {
        if (state == null) {
            state = new SampleCellSubComponentServerState();
        }
        ((SampleCellSubComponentServerState)state).setInfo(info);
        return super.getServerState(state);
    }

    @Override
    public void setServerState(CellComponentServerState state) {
        super.setServerState(state);
        info = ((SampleCellSubComponentServerState)state).getInfo();
    }
}
