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

public class DisappearTestRenderer extends BasicRenderer {

    Entity primaryEntity;
    Entity smallCubeEntity;
    Node smallCubeNode;
    
    public DisappearTestRenderer (Cell cell) {
        super(cell);
    }

    @Override
    protected Node createSceneGraph(Entity entity) {
	
	// Initialize primary object
	primaryEntity = entity;
	Node sceneRoot = createSceneRootNode(entity);

	// Create secondary object entity and attach to entity tree
	smallCubeEntity = new Entity("Small Cube");
	entity.addEntity(smallCubeEntity);

	// Create secondary object node and render component
	smallCubeNode = new Node("Small Cube Node"); 
	RenderComponent rc = 
		ClientContextJME.getWorldManager().getRenderManager().createRenderComponent(smallCubeNode);
	smallCubeEntity.addComponent(RenderComponent.class, rc);

	// Make the secondary object pickable separately from the primary object
	makeEntityPickable(smallCubeEntity, smallCubeNode);

	// Create secondary object node and geometry and attach to scene graph
	Geometry geom = createSmallCubeGeometry();
	smallCubeNode.attachChild(geom);
	smallCubeNode.setLocalTranslation(new Vector3f(0f, 3f, 0f));
	sceneRoot.attachChild(smallCubeNode);

	return sceneRoot;
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

    private Geometry createSmallCubeGeometry () {
	Geometry geom = new Box("Small Box", new Vector3f(), 0.3f, 0.3f, 0.3f);
	geom.setModelBound(new BoundingSphere());
	geom.updateModelBound();
	return geom;
    }

    // Make this entity pickable by adding a collision component to it
    protected void makeEntityPickable (Entity entity, Node node) {
	JMECollisionSystem collisionSystem = (JMECollisionSystem)
	    ClientContextJME.getWorldManager().getCollisionManager().
	    loadCollisionSystem(JMECollisionSystem.class);

	CollisionComponent cc = collisionSystem.createCollisionComponent(node);
	entity.addComponent(CollisionComponent.class, cc);
    }

    public void disconnectPrimaryObject () {
	ClientContextJME.getWorldManager().removeEntity(entity);
	entity = null;
    }

    public Entity getSecondaryEntity () {
	return smallCubeEntity;
    }

    public void disconnectSecondaryObject () {
	getEntity().removeEntity(smallCubeEntity);
	rootNode.detachChild(smallCubeNode);
    }
}
