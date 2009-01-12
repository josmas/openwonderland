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
package org.jdesktop.wonderland.modules.jeditortest.client;

import java.util.logging.Logger;
import org.jdesktop.wonderland.client.cell.CellCache;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.state.CellClientState;
import org.jdesktop.wonderland.modules.appbase.client.AppType;
import org.jdesktop.wonderland.modules.appbase.client.App2DCell;
import org.jdesktop.wonderland.modules.jeditortest.common.JEditorTestCellConfig;
import org.jdesktop.wonderland.common.ExperimentalAPI;

/**
 * Client cell for the jEditor test.
 *
 * @author deronj
 */

@ExperimentalAPI
public class JEditorTestCell extends App2DCell {
    
    /** The logger used by this class */
    private static final Logger logger = Logger.getLogger(JEditorTestCell.class.getName());
    
    /** The (singleton) window created by the JEditor test app */
    private JEditorTestWindow window;

    /** The cell config message received from the server cell */
    private JEditorTestCellConfig config;
    
    /**
     * Create an instance of JEditorTestCell.
     *
     * @param cellID The ID of the cell.
     * @param cellCache the cell cache which instantiated, and owns, this cell.
     */
    public JEditorTestCell (CellID cellID, CellCache cellCache) {
        super(cellID, cellCache);
    }
    
    /** 
     * {@inheritDoc}
     */
    public AppType getAppType () {
	return new JEditorTestAppType();
    }

    /**
     * Initialize the cell with parameters from the server.
     *
     * @param configData the config data to initialize the cell with
     */
    public void setClientState (CellClientState configData) {

        config = (JEditorTestCellConfig)configData;
        setApp(new JEditorTestApp(getAppType(), config.getPreferredWidth(), config.getPreferredHeight(),
				config.getPixelScale()));

	// Associate the app with this cell (must be done before making it visible)
	app.setCell(this);

	// Get the window the app created
	window = ((JEditorTestApp)app).getWindow();

	// Make the app window visible
	((JEditorTestApp)app).setVisible(true);
    }
}
