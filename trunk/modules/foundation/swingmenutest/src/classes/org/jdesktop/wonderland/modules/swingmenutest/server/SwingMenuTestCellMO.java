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
package org.jdesktop.wonderland.modules.swingmenutest.server;

import com.jme.math.Vector2f;
import com.sun.sgs.app.ClientSession;
import java.util.logging.Logger;
import org.jdesktop.wonderland.common.ExperimentalAPI;
import org.jdesktop.wonderland.common.cell.ClientCapabilities;
import org.jdesktop.wonderland.common.cell.state.CellClientState;
import org.jdesktop.wonderland.common.cell.state.CellServerState;
import org.jdesktop.wonderland.modules.swingmenutest.common.SwingMenuTestCellConfig;
import org.jdesktop.wonderland.modules.swingmenutest.common.SwingMenuTestTypeName;
import org.jdesktop.wonderland.modules.appbase.server.App2DCellMO;
import org.jdesktop.wonderland.modules.appbase.server.AppTypeMO;
import org.jdesktop.wonderland.server.comms.WonderlandClientID;
import org.jdesktop.wonderland.server.setup.BeanSetupMO;

/**
 * A server cell associated with a Swing test.
 *
 * @author nsimpson,deronj
 */

@ExperimentalAPI
public class SwingMenuTestCellMO extends App2DCellMO implements BeanSetupMO {

    /** The preferred width (from the WFS file) */
    private int preferredWidth;

    /** The preferred height (from the WFS file) */
    private int preferredHeight;

    /** Default constructor, used when the cell is created via WFS */
    public SwingMenuTestCellMO() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getClientCellClassName(WonderlandClientID clientID, ClientCapabilities capabilities) {
        return "org.jdesktop.wonderland.modules.swingmenutest.client.SwingMenuTestCell";
    }

    /** 
     * {@inheritDoc}
     */
    public AppTypeMO getAppType () {
	return new SwingMenuTestAppTypeMO();
    }

    /** 
     * {@inheritDoc}
     */
    @Override
    protected CellClientState getCellClientState (CellClientState cellClientState, WonderlandClientID clientID, ClientCapabilities capabilities) {
        if (cellClientState == null) {
            cellClientState = new SwingMenuTestCellConfig(pixelScale);
        }
        ((SwingMenuTestCellConfig)cellClientState).setPreferredWidth(preferredWidth);
        ((SwingMenuTestCellConfig)cellClientState).setPreferredHeight(preferredHeight);
        return super.getCellClientState(cellClientState, clientID, capabilities);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setServerState(CellServerState setupData) {
	super.setServerState(setupData);

	SwingMenuTestCellSetup setup = (SwingMenuTestCellSetup) setupData;
	preferredWidth = setup.getPreferredWidth();
	preferredHeight = setup.getPreferredHeight();
	pixelScale = new Vector2f(setup.getPixelScaleX(), setup.getPixelScaleY());
    }
}
