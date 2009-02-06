/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.common.cell.messages;

import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.state.CellClientState;

/**
 * A message to set the client state of cell's, sent from the server to clients.
 * 
 * @author Jordan Slott <jslott@dev.java.net>
 */
public class CellClientStateMessage extends CellMessage {

    private CellClientState clientState = null;

    public CellClientStateMessage(CellID cellID, CellClientState clientState) {
        super(cellID);
        this.clientState = clientState;
    }

    /**
     * Returns the cell's client state.
     *
     * @return A CellClientState object
     */
    public CellClientState getClientState() {
        return clientState;
    }
}
