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
import com.jme.math.Vector3f;
import com.jme.scene.Geometry;
import com.jme.scene.Node;
import com.jme.scene.shape.Box;
import org.jdesktop.mtgame.CollisionComponent;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.mtgame.JMECollisionSystem;
import org.jdesktop.mtgame.RenderComponent;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.jme.ClientContextJME;
import org.jdesktop.wonderland.client.jme.cellrenderer.BasicRenderer;

public class DragTestRenderer extends BasicRenderer {

    public DragTestRenderer (Cell cell) {
        super(cell);
    }

    @Override
    protected Node createSceneGraph(Entity entity) {
	return createSceneRootNode(entity);
    }

    private Node createSceneRootNode (Entity rootEntity) {

	// Create the root node
	Node sceneRoot = new Node("Drag Test Scene Root Node");
	Geometry geom = createLargeCubeGeometry();
	sceneRoot.attachChild(geom);
	
	// Attach it to the root entity
	RenderComponent rc = 
		ClientContextJME.getWorldManager().getRenderManager().createRenderComponent(sceneRoot);
	rootEntity.addComponent(RenderComponent.class, rc);

	return sceneRoot;
    }

    private Geometry createLargeCubeGeometry () {
	Geometry geom = new Box("Large Box", new Vector3f(), 1f, 1f, 1f);
	geom.setModelBound(new BoundingSphere());
	geom.updateModelBound();
	return geom;
    }
}
