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
package org.jdesktop.wonderland.modules.swingwhiteboard.client;

import java.awt.Point;
import java.math.BigInteger;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.logging.Logger;
import org.jdesktop.wonderland.client.cell.CellCache;
import org.jdesktop.wonderland.client.cell.annotation.AutoCellComponent;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.state.CellClientState;
import org.jdesktop.wonderland.modules.appbase.client.AppType;
import org.jdesktop.wonderland.modules.appbase.client.App2DCell;
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
    @AutoCellComponent
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
     * {@inheritDoc}
     */
    public AppType getAppType() {
        return new SwingWhiteboardAppType();
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
    public boolean setStatus(CellStatus status) {
        boolean ret = super.setStatus(status);

        System.err.println("HERE !!!! "+commComponent);

        switch (status) {

            // The cell is now visible
            case ACTIVE:
                setApp(new SwingWhiteboardApp(getAppType(), clientState.getPreferredWidth(),
                        clientState.getPreferredHeight(),
                        clientState.getPixelScale(), commComponent));

                // Associate the app with this cell (must be done before making it visible)
                app.setCell(this);

                // Get the window the app created
                whiteboardWin = ((SwingWhiteboardApp) app).getWindow();

                // Make the app window visible
                ((SwingWhiteboardApp) app).setVisible(true);

                break;

            // The cell is no longer visible
            case DISK:
                ((SwingWhiteboardApp) app).setVisible(false);
                removeComponent(SwingWhiteboardComponent.class);
                commComponent = null;
                break;
        }

        return ret;
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
