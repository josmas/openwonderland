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
package org.jdesktop.wonderland.modules.testcells.common.cell.state;


import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import org.jdesktop.wonderland.common.cell.state.CellServerState;
import org.jdesktop.wonderland.common.cell.state.annotation.ServerState;
import org.jdesktop.wonderland.modules.testcells.common.cell.state.SimpleShapeCellClientState.Shape;

/**
 * ServerState for the SimpleShapeCell
 *
 * TODO add support for color etc
 *
 * @author paulby
 */
@XmlRootElement(name="SimpleShape-cell")
@ServerState
public class SimpleShapeCellServerState extends CellServerState {

    private Shape shape;

    /** Default constructor */
    public SimpleShapeCellServerState() {
    }
    
    public SimpleShapeCellServerState(Shape shape) {
        this.shape = shape;
    }

    public String getServerClassName() {
        return "org.jdesktop.wonderland.modules.testcells.server.cell.SimpleShapeCellMO";
    }

    /**
     * @return the shape
     */
    @XmlElement(name="shape")
    public Shape getShape() {
        return shape;
    }

    /**
     * @param shape the shape to set
     */
    public void setShape(Shape shape) {
        this.shape = shape;
    }


}
