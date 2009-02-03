/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.modules.affordances.common.cell.state;

import org.jdesktop.wonderland.common.cell.state.CellServerState;
import org.jdesktop.wonderland.common.cell.state.annotation.ServerState;

/**
 *
 * @author jordanslott
 */
@ServerState
public class AffordanceTestCellServerState extends CellServerState {

    @Override
    public String getServerClassName() {
        return "org.jdesktop.wonderland.modules.affordances.server.cell.AffordanceTestCellMO";
    }

}
