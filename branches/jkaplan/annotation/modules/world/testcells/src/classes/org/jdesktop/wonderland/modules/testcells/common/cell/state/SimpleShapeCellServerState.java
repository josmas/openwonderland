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


import java.io.Serializable;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import org.jdesktop.wonderland.common.cell.state.CellServerState;
import org.jdesktop.wonderland.common.cell.state.spi.CellServerStateSPI;
import org.jdesktop.wonderland.modules.testcells.common.cell.state.SimpleShapeCellClientState.Shape;

/**
 * ServerState for the SimpleShapeCell
 * 
 * @author paulby
 */
@XmlRootElement(name="SimpleShape-cell")
public class SimpleShapeCellServerState extends CellServerState
        implements Serializable, CellServerStateSPI {

    @XmlElement(name="shape")
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
