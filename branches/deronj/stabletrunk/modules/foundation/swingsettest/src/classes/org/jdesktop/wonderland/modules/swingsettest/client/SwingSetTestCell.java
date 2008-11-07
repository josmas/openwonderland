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
package org.jdesktop.wonderland.modules.swingsettest.client;

import java.util.logging.Logger;
import org.jdesktop.wonderland.client.cell.CellCache;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.config.CellConfig;
import org.jdesktop.wonderland.modules.appbase.client.AppType;
import org.jdesktop.wonderland.modules.appbase.client.App2DCell;
import org.jdesktop.wonderland.modules.swingsettest.common.SwingSetTestCellConfig;
import org.jdesktop.wonderland.common.ExperimentalAPI;

/**
 * Client cell for the swing test.
 *
 * @author deronj
 */

@ExperimentalAPI
public class SwingSetTestCell extends App2DCell {
    
    /** The logger used by this class */
    private static final Logger logger = Logger.getLogger(SwingSetTestCell.class.getName());
    
    /** The (singleton) window created by the Swing test app */
    private SwingSetTestWindow window;

    /** The cell config message received from the server cell */
    private SwingSetTestCellConfig config;
    
    /**
     * Create an instance of SwingSetTestCell.
     *
     * @param cellID The ID of the cell.
     * @param cellCache the cell cache which instantiated, and owns, this cell.
     */
    public SwingSetTestCell (CellID cellID, CellCache cellCache) {
        super(cellID, cellCache);
    }
    
    /** 
     * {@inheritDoc}
     */
    public AppType getAppType () {
	return new SwingSetTestAppType();
    }

    /**
     * Initialize the cell with parameters from the server.
     *
     * @param configData the config data to initialize the cell with
     */
    public void configure (CellConfig configData) {

        config = (SwingSetTestCellConfig)configData;
        setApp(new SwingSetTestApp(getAppType(), config.getPreferredWidth(), config.getPreferredHeight(),
				config.getPixelScale()));

	// Associate the app with this cell (must be done before making it visible)
	app.setCell(this);

	// Get the window the app created
	window = ((SwingSetTestApp)app).getWindow();

	// Make the app window visible
	((SwingSetTestApp)app).setVisible(true);
    }
}
