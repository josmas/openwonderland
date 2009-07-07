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
package org.jdesktop.wonderland.modules.swingtest.client.cell;

import java.util.logging.Logger;
import org.jdesktop.wonderland.client.cell.CellCache;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.state.CellClientState;
import org.jdesktop.wonderland.modules.appbase.client.cell.App2DCell;
import org.jdesktop.wonderland.modules.swingtest.common.cell.SwingTestCellClientState;
import org.jdesktop.wonderland.common.ExperimentalAPI;
import org.jdesktop.wonderland.common.cell.CellStatus;
import org.jdesktop.wonderland.modules.swingtest.client.SwingTestApp;
import org.jdesktop.wonderland.modules.swingtest.client.SwingTestWindow;

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
    public SwingTestCell(CellID cellID, CellCache cellCache) {
        super(cellID, cellCache);
    }

    /**
     * Initialize the cell with parameters from the server.
     *
     * @param state the client state with which initialize the cell.
     */
    @Override
    public void setClientState(CellClientState state) {
        super.setClientState(state);
        clientState = (SwingTestCellClientState) state;
    }

    /**
     * This is called when the status of the cell changes.
     */
    @Override
    protected void setStatus(CellStatus status, boolean increasing) {
        super.setStatus(status, increasing);

        switch (status) {

            // The cell is now visible
            case ACTIVE:
                if (increasing) {
                    SwingTestApp stApp = new SwingTestApp("Swing Test", clientState.getPixelScale());
                    setApp(stApp);

                    // Tell the app to be displayed in this cell.
                    stApp.addDisplayer(this);

                    // This app has only one window, so it is always top-level
                    try {
                        window = new SwingTestWindow(this, stApp, clientState.getPreferredWidth(),
                                                     clientState.getPreferredHeight(), true, pixelScale);
                    } catch (InstantiationException ex) {
                        throw new RuntimeException(ex);
                    }

                    // Both the app and the user want this window to be visible
                    window.setVisibleApp(true);
                    window.setVisibleUser(this, true);
                }
                break;

            // The cell is no longer visible
            case DISK:
                if (!increasing) {
                    window.setVisibleApp(false);
                    window = null;
                }
                break;
        }
    }
}
