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
package org.jdesktop.wonderland.modules.swingwhiteboard.client;

import java.awt.Point;
import java.math.BigInteger;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.logging.Logger;
import org.jdesktop.wonderland.client.cell.CellCache;
import org.jdesktop.wonderland.client.cell.annotation.UsesCellComponent;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.state.CellClientState;
import org.jdesktop.wonderland.modules.appbase.client.cell.App2DCell;
import org.jdesktop.wonderland.modules.swingwhiteboard.common.SwingWhiteboardCellClientState;
import org.jdesktop.wonderland.modules.swingwhiteboard.common.WhiteboardCompoundCellMessage;
import org.jdesktop.wonderland.modules.swingwhiteboard.common.WhiteboardAction.Action;
import org.jdesktop.wonderland.modules.swingwhiteboard.common.WhiteboardCommand.Command;
import org.jdesktop.wonderland.common.ExperimentalAPI;
import org.jdesktop.wonderland.common.cell.CellStatus;

/**
 * Client Cell for a whiteboard shared application based on Swing.
 *
 * @author paulby,nsimpson,deronj
 */
@ExperimentalAPI
public class SwingWhiteboardCell extends App2DCell {

    /** The logger used by this class */
    private static final Logger logger = Logger.getLogger(SwingWhiteboardCell.class.getName());
    /** The (singleton) window created by the whiteboard app */
    private SwingWhiteboardWindow whiteboardWin;
    /** The cell client state message received from the server cell */
    private SwingWhiteboardCellClientState clientState;
    /** The communications component used to communicate with the server */
    @UsesCellComponent
    private SwingWhiteboardComponent commComponent;

    /**
     * Create an instance of SwingWhiteboardCell.
     *
     * @param cellID The ID of the cell.
     * @param cellCache the cell cache which instantiated, and owns, this cell.
     */
    public SwingWhiteboardCell(CellID cellID, CellCache cellCache) {
        super(cellID, cellCache);
    }

    /**
     * Initialize the whiteboard with parameters from the server.
     *
     * @param state the client state data to initialize the cell with
     */
    @Override
    public void setClientState(CellClientState state) {
        super.setClientState(state);
        clientState = (SwingWhiteboardCellClientState) state;
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
                    SwingWhiteboardApp swaApp = new SwingWhiteboardApp("Swing Whiteboard",
                                                                       clientState.getPixelScale(),
                                                                       commComponent);
                    setApp(swaApp);

                    // Tell the app to be displayed in this cell.
                    swaApp.addDisplayer(this);

                    // This app has only one window, so it is always top-level
                    try {
                        whiteboardWin = new SwingWhiteboardWindow(this, swaApp, clientState.getPreferredWidth(),
                                                                  clientState.getPreferredHeight(), true,
                                                                  pixelScale, commComponent);
                    } catch (InstantiationException ex) {
                        throw new RuntimeException(ex);
                    }

                    // Both the app and the user want this window to be visible
                    whiteboardWin.setVisibleApp(true);
                    whiteboardWin.setVisibleUser(this, true);
                }
                break;

            // The cell is no longer visible
            case DISK:
                if (!increasing) {
                    whiteboardWin.setVisibleApp(false);
                    removeComponent(SwingWhiteboardComponent.class);
                    commComponent = null;
                }
                break;
        }
    }

    /**
     * Process the actions in a compound message
     *
     * @param msg a compound message
     */
    void processMessage(WhiteboardCompoundCellMessage msg) {
        switch (msg.getAction()) {
            case SET_COLOR:
                whiteboardWin.setPenColor(msg.getColor());
                break;
            case MOVE_TO:
            case DRAG_TO:
                LinkedList<Point> positions = msg.getPositions();
                Iterator<Point> iter = positions.iterator();

                while (iter.hasNext()) {
                    Point position = iter.next();
                    if (msg.getAction() == Action.MOVE_TO) {
                        whiteboardWin.move(position);
                    } else if (msg.getAction() == Action.DRAG_TO) {
                        whiteboardWin.drag(position);
                    }
                }
                break;
            case EXECUTE_COMMAND:
                if (msg.getCommand() == Command.ERASE) {
                    whiteboardWin.erase();
                }
        }
    }

    /**
     * Returns the client ID of this cell's session.
     */
    BigInteger getClientID() {
        return getCellCache().getSession().getID();
    }
}
