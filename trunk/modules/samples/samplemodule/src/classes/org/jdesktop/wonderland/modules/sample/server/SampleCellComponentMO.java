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

import com.sun.sgs.app.ManagedReference;
import java.util.logging.Logger;
import org.jdesktop.wonderland.common.cell.ClientCapabilities;
import org.jdesktop.wonderland.common.cell.state.CellComponentClientState;
import org.jdesktop.wonderland.common.cell.state.CellComponentServerState;
import org.jdesktop.wonderland.modules.sample.common.SampleCellComponentClientState;
import org.jdesktop.wonderland.modules.sample.common.SampleCellComponentServerState;
import org.jdesktop.wonderland.server.cell.CellComponentMO;
import org.jdesktop.wonderland.server.cell.CellMO;
import org.jdesktop.wonderland.server.cell.annotation.UsesCellComponentMO;
import org.jdesktop.wonderland.server.comms.WonderlandClientID;

/**
 * A sample cell component
 *
 * @author Jordan Slott <jslott@dev.java.net>
 */
public class SampleCellComponentMO extends CellComponentMO {

    private static Logger logger = Logger.getLogger(SampleCellComponentMO.class.getName());
    private String info = null;

    @UsesCellComponentMO(SampleCellSubComponentMO.class)
    private ManagedReference<SampleCellSubComponentMO> subComponentRef;
    
    public SampleCellComponentMO(CellMO cell) {
        super(cell);
    }

    @Override
    protected String getClientClass() {
        return "org.jdesktop.wonderland.modules.sample.client.SampleCellComponent";
    }

    @Override
    protected void setLive(boolean live) {
        super.setLive(live);
        logger.warning("Setting SampleCellComponentMO to live = " + live);
    }


    @Override
    public CellComponentClientState getClientState(CellComponentClientState state, WonderlandClientID clientID, ClientCapabilities capabilities) {
        if (state == null) {
            state = new SampleCellComponentClientState();
        }
        ((SampleCellComponentClientState)state).setInfo(info);
        return super.getClientState(state, clientID, capabilities);
    }

    @Override
    public CellComponentServerState getServerState(CellComponentServerState state) {
        if (state == null) {
            state = new SampleCellComponentServerState();
        }
        ((SampleCellComponentServerState)state).setInfo(info);
        return super.getServerState(state);
    }

    @Override
    public void setServerState(CellComponentServerState state) {
        super.setServerState(state);
        info = ((SampleCellComponentServerState)state).getInfo();
    }
}
