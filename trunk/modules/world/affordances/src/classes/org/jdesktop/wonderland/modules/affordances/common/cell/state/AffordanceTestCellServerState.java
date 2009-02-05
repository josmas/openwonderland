/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.modules.affordances.common.cell.state;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import org.jdesktop.wonderland.common.cell.state.CellServerState;
import org.jdesktop.wonderland.common.cell.state.annotation.ServerState;

/**
 *
 * @author jordanslott
 */
@ServerState
@XmlRootElement(name="affordance-test-cell")
public class AffordanceTestCellServerState extends CellServerState {

    @XmlElement(name="shape-type")
    private String shapeType = "BOX";

    @Override
    public String getServerClassName() {
        return "org.jdesktop.wonderland.modules.affordances.server.cell.AffordanceTestCellMO";
    }

    @XmlTransient public String getShapeType() {
        return shapeType;
    }

    public void setShapeType(String shapeType) {
        this.shapeType = shapeType;
    }
}
