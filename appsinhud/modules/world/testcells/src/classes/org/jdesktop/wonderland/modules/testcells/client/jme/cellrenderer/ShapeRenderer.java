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
package org.jdesktop.wonderland.modules.testcells.client.jme.cellrenderer;

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
import com.jme.scene.state.MaterialState;
import com.jme.scene.state.RenderState;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.jme.ClientContextJME;
import org.jdesktop.wonderland.client.jme.cellrenderer.BasicRenderer;
import org.jdesktop.wonderland.common.cell.config.jme.MaterialJME;
import org.jdesktop.wonderland.modules.testcells.client.cell.SimpleShapeCell;

/**
 * Render basic jme shapes
 *
 * @author paulby
 */
public class ShapeRenderer extends BasicRenderer {

    private Node scene;
    private Geometry geom;

    public ShapeRenderer(Cell cell) {
        super(cell);
    }

    public void shapeChanged() {
        geom.removeFromParent();
        createShapeNode();
    }

    public void colorChanged() {

    }

    @Override
    protected Node createSceneGraph(Entity entity) {
        scene = new Node();
        scene.setName(cell.getCellID().toString());
        
        createShapeNode();

        return scene;
    }

    private void createShapeNode() {

        float xExtent = 1f;
        float yExtent = 1f;
        float zExtent = 1f;

        switch(((SimpleShapeCell)cell).getShape()) {
            case BOX :
                scene.attachChild(geom = new Box("Box", new Vector3f(), xExtent, yExtent, zExtent));
                break;
            case CYLINDER :
                scene.attachChild(geom = new Cylinder("Cylinder", 10, 10, xExtent, yExtent));
                // Make yUp
                geom.setLocalRotation(new Quaternion(new float[] {(float)Math.PI/2, 0f, 0f}));
                break;
            case CONE :
                scene.attachChild(geom = new Cone("Cone", 10, 10, xExtent, yExtent));
                // Make yUp
                geom.setLocalRotation(new Quaternion(new float[] {(float)Math.PI/2, 0f, 0f}));
                break;
            case SPHERE :
                scene.attachChild(geom = new Sphere("Sphere", 10, 10, xExtent));
                break;
            case TEAPOT :
                scene.attachChild(geom = new Teapot());
                ((Teapot)geom).resetData();
                scene.setLocalScale(0.2f);
                break;
        }
        
        if (geom!=null) {

            geom.setModelBound(new BoundingSphere());
            geom.updateModelBound();

            MaterialState matState = (MaterialState) ClientContextJME.getWorldManager().getRenderManager().createRendererState(RenderState.RS_MATERIAL);
            MaterialJME matJME = ((SimpleShapeCell)cell).getMaterialJME();
            if (matJME!=null)
                matJME.apply(matState);
            geom.setRenderState(matState);
        }

    }

}
