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
package org.jdesktop.wonderland.modules.affordances.client.jme.cellrenderer;

import com.jme.bounding.BoundingSphere;
import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import com.jme.scene.Geometry;
import com.jme.scene.Node;
import com.jme.scene.shape.Box;
import com.jme.scene.shape.Cone;
import com.jme.scene.shape.Cylinder;
import com.jme.scene.shape.Sphere;
import com.jme.scene.shape.Teapot;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.jme.cellrenderer.BasicRenderer;
import org.jdesktop.wonderland.modules.affordances.client.cell.AffordanceTestCell;

/**
 * Render basic jme shapes
 *
 * @author paulby
 */
public class AffordanceTestCellRenderer extends BasicRenderer {

    public AffordanceTestCellRenderer(Cell cell) {
        super(cell);
    }
    
    @Override
    protected Node createSceneGraph(Entity entity) {
        float xExtent = 1f;
        float yExtent = 1f;
        float zExtent = 1f;
        
        Node ret = new Node();
        ret.setName(cell.getCellID().toString());
        Geometry geom=null;
        
        String shapeType = ((AffordanceTestCell)cell).getShape();
        if (shapeType.equals("BOX") == true) {
            ret.attachChild(geom = new Box("Box", new Vector3f(), xExtent, yExtent, zExtent));
        }
        else if (shapeType.equals("CYLINDER") == true) {
            ret.attachChild(geom = new Cylinder("Cylinder", 10, 10, xExtent, yExtent));
            // Make yUp
            ret.setLocalRotation(new Quaternion(new float[]{(float) Math.PI / 2, 0f, 0f}));
        }
        else if (shapeType.equals("CONE") == true) {
            ret.attachChild(geom = new Cone("Cone", 10, 10, xExtent, yExtent));
            // Make yUp
            ret.setLocalRotation(new Quaternion().fromAngleAxis((float) Math.toRadians(90), new Vector3f(1.0f, 0.0f, 0.0f)));
        }
        else if (shapeType.equals("SPHERE") == true) {
            ret.attachChild(geom = new Sphere("Sphere", 10, 10, xExtent));
        }
        else {
            ret.attachChild(geom = new Teapot());
            ((Teapot) geom).resetData();
            ret.setLocalScale(0.2f);
        }

        if (geom!=null) {
            geom.setModelBound(new BoundingSphere());
            geom.updateModelBound();
        }

        return ret;
    }
}
