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
package org.jdesktop.wonderland.modules.testcells.client.jme.cellrenderer;

import com.jme.bounding.BoundingSphere;
import com.jme.math.Vector3f;
import com.jme.scene.Node;
import com.jme.scene.shape.Box;
import com.jme.scene.shape.Cone;
import com.jme.scene.shape.Cylinder;
import com.jme.scene.shape.Sphere;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.jme.cellrenderer.BasicRenderer;
import org.jdesktop.wonderland.modules.testcells.client.cell.SimpleShapeCell;
import org.jdesktop.wonderland.modules.testcells.common.cell.config.SimpleShapeConfig;

/**
 * Render basic jme shapes
 *
 * @author paulby
 */
public class ShapeRenderer extends BasicRenderer {

    public ShapeRenderer(Cell cell) {
        super(cell);
    }
    
    @Override
    protected Node createSceneGraph(Entity entity) {
        float xExtent = 1f;
        float yExtent = 1f;
        float zExtent = 1f;
        
        Node ret = new Node();
        ret.setName(cell.getCellID().toString());
        
        switch(((SimpleShapeCell)cell).getShape()) {
            case BOX :
                ret.attachChild(new Box("Box", new Vector3f(), xExtent, yExtent, zExtent));
                break;
            case CYLINDER :
                ret.attachChild(new Cylinder("Cylinder", 10, 10, xExtent, yExtent));
                break;
            case CONE :
                ret.attachChild(new Cone("Cone", 10, 10, xExtent, yExtent));
                break;
            case SPHERE :
                ret.attachChild(new Sphere("Sphere", 10, 10, xExtent));
                break;
        }

        // Set the transform
        applyTransform(ret, cell.getLocalTransform());

        ret.setModelBound(new BoundingSphere());
        ret.updateModelBound();
        
        return ret;
    }

}
