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
package org.jdesktop.wonderland.modules.simplewhiteboard.client.cell;

import java.awt.Point;
import java.math.BigInteger;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.logging.Logger;
import org.jdesktop.wonderland.client.cell.CellCache;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.state.CellClientState;
import org.jdesktop.wonderland.modules.appbase.client.AppType;
import org.jdesktop.wonderland.modules.appbase.client.cell.App2DCell;
import org.jdesktop.wonderland.modules.simplewhiteboard.common.cell.WhiteboardCellClientState;
import org.jdesktop.wonderland.modules.simplewhiteboard.common.cell.WhiteboardCompoundCellMessage;
import org.jdesktop.wonderland.modules.simplewhiteboard.common.WhiteboardAction.Action;
import org.jdesktop.wonderland.modules.simplewhiteboard.common.WhiteboardCommand.Command;
import org.jdesktop.wonderland.common.ExperimentalAPI;
import org.jdesktop.wonderland.common.cell.CellStatus;
import org.jdesktop.wonderland.modules.simplewhiteboard.client.WhiteboardApp;
import org.jdesktop.wonderland.modules.simplewhiteboard.client.WhiteboardAppType;
import org.jdesktop.wonderland.modules.simplewhiteboard.client.WhiteboardComponent;
import org.jdesktop.wonderland.modules.simplewhiteboard.client.WhiteboardWindow;
import org.jdesktop.wonderland.modules.simplewhiteboard.common.cell.WhiteboardCompoundCellMessage;

/**
 * Client Cell for a whiteboard shared application.
 *
 * @author nsimpson,deronj
 */
@ExperimentalAPI
public class WhiteboardCell extends App2DCell {

    /** The logger used by this class */
    private static final Logger logger = Logger.getLogger(WhiteboardCell.class.getName());
    /** The (singleton) window created by the whiteboard app */
    private WhiteboardWindow whiteboardWin;
    /** The cell client state message received from the server cell */
    private WhiteboardCellClientState clientState;
    /** The communications component used to communicate with the server */
    private WhiteboardComponent commComponent;

    /**
     * Create an instance of WhiteboardCell.
     *
     * @param cellID The ID of the cell.
     * @param cellCache the cell cache which instantiated, and owns, this cell.
     */
    public WhiteboardCell(CellID cellID, CellCache cellCache) {
        super(cellID, cellCache);
    }

    /** 
     * {@inheritDoc}
     */
    public AppType getAppType() {
        return new WhiteboardAppType();
    }

    /**
     * Initialize the whiteboard with parameters from the server.
     *
     * @param clientState the client state to initialize the cell with
     */
    @Override
    public void setClientState(CellClientState state) {
        super.setClientState(state);
        clientState = (WhiteboardCellClientState) state;
    }

    /**
     * This is called when the status of the cell changes.
     */
    @Override
    public boolean setStatus(CellStatus status) {
        boolean ret = super.setStatus(status);

        switch (status) {

            // The cell is now visible
            case ACTIVE:

                commComponent = getComponent(WhiteboardComponent.class);

                WhiteboardApp whiteboardApp = new WhiteboardApp(getAppType(), clientState.getPixelScale());
                setApp(whiteboardApp);

                // Associate the app with this cell (must be done before making it visible)
                whiteboardApp.setDisplayer(this);

                // This app has only one window, so it is always top-level 
                try {
                    whiteboardWin = new WhiteboardWindow(whiteboardApp, clientState.getPreferredWidth(),
                                                         clientState.getPreferredHeight(), true, 
                                                         clientState.getPixelScale(),
                                                         commComponent);
                } catch (InstantiationException ex) {
                    throw new RuntimeException(ex);
                }

                // Make the app window visible
                whiteboardWin.setVisible(true);
                break;

            // The cell is no longer visible
            case DISK:
                whiteboardWin.setVisible(false);
                removeComponent(WhiteboardComponent.class);
                commComponent = null;
                whiteboardWin = null;
                break;
        }

        return ret;
    }

    /**
     * Process the actions in a compound message
     *
     * @param msg a compound message
     */
    public void processMessage(WhiteboardCompoundCellMessage msg) {
        switch (msg.getAction()) {
            case SET_TOOL:
                whiteboardWin.setTool(msg.getTool());
                break;
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
                        whiteboardWin.moveTo(position);
                    } else if (msg.getAction() == Action.DRAG_TO) {
                        whiteboardWin.dragTo(position);
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
    public BigInteger getClientID() {
        return getCellCache().getSession().getID();
    }
}
