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
package org.jdesktop.wonderland.modules.swingtest.client;

import java.util.logging.Logger;
import org.jdesktop.wonderland.client.cell.CellCache;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.state.CellClientState;
import org.jdesktop.wonderland.modules.appbase.client.AppType;
import org.jdesktop.wonderland.modules.appbase.client.App2DCell;
import org.jdesktop.wonderland.modules.swingtest.common.SwingTestCellClientState;
import org.jdesktop.wonderland.common.ExperimentalAPI;

/**
 * Client cell for the swing test.
 *
 * @author deronj
 */

@ExperimentalAPI
public class SwingTestCell extends App2DCell {
    
    /** The logger used by this class */
    private static final Logger logger = Logger.getLogger(SwingTestCell.class.getName());
    
    /** The (singleton) window created by the Swing test app */
    private SwingTestWindow window;

    /** The cell client state message received from the server cell */
    private SwingTestCellClientState clientState;
    
    /**
     * Create an instance of SwingTestCell.
     *
     * @param cellID The ID of the cell.
     * @param cellCache the cell cache which instantiated, and owns, this cell.
     */
    public SwingTestCell (CellID cellID, CellCache cellCache) {
        super(cellID, cellCache);
    }
    
    /** 
     * {@inheritDoc}
     */
    public AppType getAppType () {
	return new SwingTestAppType();
    }

    /**
     * Initialize the cell with parameters from the server.
     *
     * @param state the client state with which initialize the cell.
     */
    public void setCellClientState (CellClientState state) {

        clientState = (SwingTestCellClientState) state;
        setApp(new SwingTestApp(getAppType(), clientState.getPreferredWidth(), 
				clientState.getPreferredHeight(),
				clientState.getPixelScale()));

	// Associate the app with this cell (must be done before making it visible)
	app.setCell(this);

	// Get the window the app created
	window = ((SwingTestApp)app).getWindow();

	// Make the app window visible
	((SwingTestApp)app).setVisible(true);
    }
}
