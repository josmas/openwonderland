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
package org.jdesktop.wonderland.modules.sample.common;

import org.jdesktop.wonderland.common.cell.state.CellClientState;

/**
 * The SampleCellConfig class represents the information communicated
 * between the client and Darkstar server for the sample model.
 *
 * @author jkaplan
 */
public class SampleCellClientState extends CellClientState {
    /* The shape type: BOX or SPHERE */
    private String shapeType = null;

    /** Default constructor */
    public SampleCellClientState() {
    }

    public String getShapeType() {
        return shapeType;
    }

    public void setShapeType(String shapeType) {
        this.shapeType = shapeType;
    }
}
