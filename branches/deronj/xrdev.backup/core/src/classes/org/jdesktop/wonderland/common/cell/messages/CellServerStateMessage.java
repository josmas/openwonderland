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
package org.jdesktop.wonderland.common.cell.messages;

import org.jdesktop.wonderland.common.ExperimentalAPI;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.state.CellServerState;

/**
 * Message sent to either query for or set the cell server state for a cell
 * given its unique ID.
 * <p>
 * This message can be used as a request from the client to the server, or it
 * may be used as an asynchronous event to notify clients of a state change.
 * Note that when a "GET" cell server state message is sent from a client, the
 * server responds with a CellServerStateResponseMessage.
 *
 * @author Jordan Slott <jslott@dev.java.net>
 */
@ExperimentalAPI
public class CellServerStateMessage extends CellMessage {
    /* The message type: GET or SET */
    public enum StateAction { GET, SET };

    private CellServerState cellServerState;
    private StateAction action;

    /**
     * Constructor that takes the unique ID of the cell and the server state
     * (used to set the state).
     * 
     * @param cellID The id of the cell
     */
    public CellServerStateMessage(CellID cellID, CellServerState cellServerState) {
        super(cellID);
        this.cellServerState = cellServerState;
    }

    /**
     * Constructor that takes the unique ID of the cell (used to get the state).
     *
     * @param cellID The id of the cell
     */
    public CellServerStateMessage(CellID cellID) {
        super(cellID);
        this.cellServerState = null;
    }

    /**
     * Returns the server state in this message
     *
     * @return The CellServerState object
     */
    public CellServerState getCellServerState() {
        return cellServerState;
    }

    /**
     * Returns the type of state message (GET or SET).
     *
     * @param The StateAction type
     */
    public StateAction getStateAction() {
        return action;
    }

    /**
     * Creates a new GET cell state message given the cell id.
     *
     * @param cellID The unique ID of the cell
     * @return A new CellServerStateMessage class
     */
    public static CellServerStateMessage newGetMessage(CellID cellID) {
        CellServerStateMessage msg = new CellServerStateMessage(cellID);
        msg.action = StateAction.GET;
        return msg;
    }

    /**
     * Creates a new SET cell state message given the cell id and cell server
     * server state object.
     *
     * @param cellID The unique ID of the cell
     * @param serverState The cell server state
     * @return A new CellServerStateMessage class
     */
    public static CellServerStateMessage newSetMessage(CellID cellID, CellServerState serverState) {
        CellServerStateMessage msg = new CellServerStateMessage(cellID, serverState);
        msg.action = StateAction.SET;
        return msg;
    }
}
