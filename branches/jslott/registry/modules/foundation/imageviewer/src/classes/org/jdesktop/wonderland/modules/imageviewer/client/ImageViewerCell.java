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
package org.jdesktop.wonderland.modules.imageviewer.client;

import java.util.logging.Logger;
import org.jdesktop.wonderland.client.cell.CellCache;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.config.CellConfig;
import org.jdesktop.wonderland.modules.appbase.client.AppType;
import org.jdesktop.wonderland.modules.appbase.client.App2DCell;
import org.jdesktop.wonderland.common.ExperimentalAPI;
import org.jdesktop.wonderland.modules.imageviewer.common.ImageViewerCellConfig;

/**
 * Client Cell for a whiteboard shared application.
 *
 * @author nsimpson,deronj
 */

@ExperimentalAPI
public class ImageViewerCell extends App2DCell {
    
    /** The logger used by this class */
    private static final Logger logger = Logger.getLogger(ImageViewerCell.class.getName());
    
    /** The (singleton) window created by the whiteboard app */
    private ImageViewerWindow whiteboardWin;

    /** The cell config message received from the server cell */
    private ImageViewerCellConfig config;

    /**
     * Create an instance of ImageViewerCell.
     *
     * @param cellID The ID of the cell.
     * @param cellCache the cell cache which instantiated, and owns, this cell.
     */
    public ImageViewerCell (CellID cellID, CellCache cellCache) {
        super(cellID, cellCache);
    }
    
    /** 
     * {@inheritDoc}
     */
    public AppType getAppType () {
	return new ImageViewerAppType();
    }

    /**
     * Initialize the whiteboard with parameters from the server.
     *
     * @param configData the config data to initialize the cell with
     */
    public void configure (CellConfig configData) {

        config = (ImageViewerCellConfig)configData;
        setApp(new ImageViewerApp(getAppType(), config.getPreferredWidth(), config.getPreferredHeight(),
				 config.getPixelScale()));

	// Associate the app with this cell (must be done before making it visible)
	app.setCell(this);

	// Get the window the app created
	whiteboardWin = ((ImageViewerApp)app).getWindow();

	// Make the app window visible
	((ImageViewerApp)app).setVisible(true);

	// Note: we used to force a sync here. But in the new implementation we will
	// perform the sync when the cell status becomes BOUNDS.
    }
}
