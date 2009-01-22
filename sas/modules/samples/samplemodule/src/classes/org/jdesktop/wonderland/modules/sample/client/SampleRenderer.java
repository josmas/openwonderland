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
package org.jdesktop.wonderland.modules.sample.client;

import com.jme.bounding.BoundingBox;
import com.jme.bounding.BoundingSphere;
import com.jme.math.Vector3f;
import com.jme.scene.Node;
import com.jme.scene.TriMesh;
import com.jme.scene.shape.Box;
import com.jme.scene.shape.Sphere;
import com.jme.scene.state.RenderState;
import com.jme.scene.state.WireframeState;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.jme.ClientContextJME;
import org.jdesktop.wonderland.client.jme.cellrenderer.BasicRenderer;
import org.jdesktop.wonderland.common.cell.CellTransform;

/**
 * @author jkaplan
 */
public class SampleRenderer extends BasicRenderer {
    
    public SampleRenderer(Cell cell) {
        super(cell);
    }

    /**
     * Creates a wireframe box or sphere with the same size as the bounds.
     */
    @Override
    public Node createSceneGraph(Entity entity) {
        /* Fetch the basic info about the cell */
        String name = cell.getCellID().toString();
        CellTransform transform = cell.getLocalTransform();
        
        /* Create the new object -- either a Box or Sphere */
        TriMesh mesh = null;
        if (cell.getLocalBounds() instanceof BoundingBox) {
            Vector3f extent = ((BoundingBox)cell.getLocalBounds()).getExtent(null);
            mesh = new Box(name, new Vector3f(), extent.x, extent.y, extent.z);
        }
        else if (cell.getLocalBounds() instanceof BoundingSphere) {
            float radius = ((BoundingSphere)cell.getLocalBounds()).getRadius();
            mesh = new Sphere(name, new Vector3f(), 10, 10, radius);
        }
        else {
            logger.warning("Unsupported Bounds type " +cell.getLocalBounds().getClass().getName());
            return new Node();
        }
        
        /* Create the scene graph object and set its wireframe state */
        Node node = new Node();
        node.attachChild(mesh);
        node.setModelBound(new BoundingBox());
        node.updateModelBound();
        applyTransform(node, transform);

        WireframeState wiState = (WireframeState)ClientContextJME.getWorldManager().getRenderManager().createRendererState(RenderState.RS_WIREFRAME);
        wiState.setEnabled(true);
        node.setRenderState(wiState);
        node.setName("Cell_"+cell.getCellID()+":"+cell.getName());

        return node;
    }
}
