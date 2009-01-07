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
package org.jdesktop.wonderland.modules.jeditortest.server;

import com.jme.math.Vector2f;
import com.sun.sgs.app.ClientSession;
import java.util.logging.Logger;
import org.jdesktop.wonderland.common.ExperimentalAPI;
import org.jdesktop.wonderland.common.cell.ClientCapabilities;
import org.jdesktop.wonderland.common.cell.config.CellConfig;
import org.jdesktop.wonderland.common.cell.setup.BasicCellSetup;
import org.jdesktop.wonderland.modules.jeditortest.common.JEditorTestCellConfig;
import org.jdesktop.wonderland.modules.jeditortest.common.JEditorTestTypeName;
import org.jdesktop.wonderland.modules.appbase.server.App2DCellMO;
import org.jdesktop.wonderland.modules.appbase.server.AppTypeMO;
import org.jdesktop.wonderland.server.comms.WonderlandClientID;
import org.jdesktop.wonderland.server.setup.BeanSetupMO;

/**
 * A server cell associated with a JEditor test.
 *
 * @author nsimpson,deronj
 */

@ExperimentalAPI
public class JEditorTestCellMO extends App2DCellMO implements BeanSetupMO {

    /** The preferred width (from the WFS file) */
    private int preferredWidth;

    /** The preferred height (from the WFS file) */
    private int preferredHeight;

    /** Default constructor, used when the cell is created via WFS */
    public JEditorTestCellMO() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getClientCellClassName(WonderlandClientID clientID, ClientCapabilities capabilities) {
        return "org.jdesktop.wonderland.modules.jeditortest.client.JEditorTestCell";
    }

    /** 
     * {@inheritDoc}
     */
    public AppTypeMO getAppType () {
	return new JEditorTestAppTypeMO();
    }

    /** 
     * {@inheritDoc}
     */
    @Override
    protected CellConfig getCellConfig (WonderlandClientID clientID, ClientCapabilities capabilities) {
	JEditorTestCellConfig config = new JEditorTestCellConfig(pixelScale);
	config.setPreferredWidth(preferredWidth);
	config.setPreferredHeight(preferredHeight);
        return config;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setupCell(BasicCellSetup setupData) {
	super.setupCell(setupData);

	JEditorTestCellSetup setup = (JEditorTestCellSetup) setupData;
	preferredWidth = setup.getPreferredWidth();
	preferredHeight = setup.getPreferredHeight();
	pixelScale = new Vector2f(setup.getPixelScaleX(), setup.getPixelScaleY());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void reconfigureCell(BasicCellSetup setup) {
        super.reconfigureCell(setup);
        setupCell(setup);
    }
}
