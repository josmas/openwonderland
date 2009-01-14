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
package org.jdesktop.wonderland.modules.swingsettest.server;

import com.jme.math.Vector2f;
import com.sun.sgs.app.ClientSession;
import java.util.logging.Logger;
import org.jdesktop.wonderland.common.ExperimentalAPI;
import org.jdesktop.wonderland.common.cell.ClientCapabilities;
import org.jdesktop.wonderland.common.cell.state.CellClientState;
import org.jdesktop.wonderland.common.cell.state.CellServerState;
import org.jdesktop.wonderland.modules.swingsettest.common.SwingSetTestCellClientState;
import org.jdesktop.wonderland.modules.swingsettest.common.SwingSetTestCellServerState;
import org.jdesktop.wonderland.modules.swingsettest.common.SwingSetTestTypeName;
import org.jdesktop.wonderland.modules.appbase.server.App2DCellMO;
import org.jdesktop.wonderland.modules.appbase.server.AppTypeMO;
import org.jdesktop.wonderland.server.comms.WonderlandClientID;

/**
 * A server cell associated with a Swing test.
 *
 * @author nsimpson,deronj
 */

@ExperimentalAPI
public class SwingSetTestCellMO extends App2DCellMO {

    /** The preferred width (from the WFS file) */
    private int preferredWidth;

    /** The preferred height (from the WFS file) */
    private int preferredHeight;

    /** Default constructor, used when the cell is created via WFS */
    public SwingSetTestCellMO() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getClientCellClassName(WonderlandClientID clientID, ClientCapabilities capabilities) {
        return "org.jdesktop.wonderland.modules.swingsettest.client.SwingSetTestCell";
    }

    /** 
     * {@inheritDoc}
     */
    public AppTypeMO getAppType () {
	return new SwingSetTestAppTypeMO();
    }

    /** 
     * {@inheritDoc}
     */
    @Override
    protected CellClientState getCellClientState (CellClientState cellClientState, WonderlandClientID clientID, ClientCapabilities capabilities) {
        if (cellClientState == null) {
            cellClientState = new SwingSetTestCellClientState(pixelScale);
        }
        ((SwingSetTestCellClientState)cellClientState).setPreferredWidth(preferredWidth);
        ((SwingSetTestCellClientState)cellClientState).setPreferredHeight(preferredHeight);
        return super.getCellClientState(cellClientState, clientID, capabilities);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setCellServerState(CellServerState state) {
	super.setCellServerState(state);

	SwingSetTestCellServerState serverState = (SwingSetTestCellServerState) state;
	preferredWidth = serverState.getPreferredWidth();
	preferredHeight = serverState.getPreferredHeight();
	pixelScale = new Vector2f(serverState.getPixelScaleX(), serverState.getPixelScaleY());
    }
}
