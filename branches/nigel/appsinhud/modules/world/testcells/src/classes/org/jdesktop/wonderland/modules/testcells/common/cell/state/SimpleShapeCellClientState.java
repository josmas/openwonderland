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

import org.jdesktop.wonderland.common.cell.state.CellClientState;
import org.jdesktop.wonderland.common.cell.config.jme.MaterialJME;

/**
 * Configuration for the SimpleShapeCell
 *
 * Note This includes a lot of JME detail which would not necesarily be
 * relevant to a 2D client, TODO implement a better abstraction
 *
 * @author paulby
 */
public class SimpleShapeCellClientState extends CellClientState {
    public enum Shape { BOX, CONE, CYLINDER, SPHERE, TEAPOT };

    private Shape shape;

    private float mass = 0f;

    private MaterialJME materialJME = null;

    public SimpleShapeCellClientState(Shape shape) {
        this(shape, 0f);
    }

    public SimpleShapeCellClientState(Shape shape, float mass) {
        this(shape, mass, null);
    }

    public SimpleShapeCellClientState(Shape shape, float mass, MaterialJME material) {
        this.shape = shape;
        this.mass = mass;
        this.materialJME = material;
    }

    public Shape getShape() {
        return shape;
    }

    // TODO this will change to provide much more physics info
    public float getMass() {
        return mass;
    }

    public MaterialJME getMaterialJME() {
        return materialJME;
    }
}
