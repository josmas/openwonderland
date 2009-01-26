/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.common.cell.messages;

import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.state.CellClientState;

/**
 * A base class for all messages that update the state of a cell.
 * 
 * @author Jordan Slott <jslott@dev.java.net>
 */
public class CellUpdateMessage extends CellMessage {

    private CellClientState clientState = null;

    public CellUpdateMessage(CellID cellID, CellClientState clientState) {
        super(cellID);
        this.clientState = clientState;
    }

    public CellClientState getClientState() {
        return clientState;
    }

    public void setClientState(CellClientState clientState) {
        this.clientState = clientState;
    }
}
