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
package org.jdesktop.wonderland.modules.affordances.client.jme;

import com.jme.scene.Node;
import org.jdesktop.mtgame.CollisionComponent;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.mtgame.JMECollisionSystem;
import org.jdesktop.mtgame.RenderComponent;
import org.jdesktop.mtgame.RenderManager;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.cell.Cell.RendererType;
import org.jdesktop.wonderland.client.cell.MovableComponent;
import org.jdesktop.wonderland.client.jme.CellRefComponent;
import org.jdesktop.wonderland.client.jme.ClientContextJME;
import org.jdesktop.wonderland.client.jme.cellrenderer.CellRendererJME;

/**
 * The base class of all Affordances (manipulators)
 * 
 * @author Jordan Slott <jslott@dev.java.net>
 */
public abstract class Affordance extends Entity {

    protected Node rootNode;
    protected MovableComponent movableComp;
    protected Cell cell = null;
    
    /** Constructor, takes the cell */
    public Affordance(String name, Cell cell) {
        super(name);
        this.cell = cell;

        // Create the root node of the cell and the render component to attach
        // to the Entity with the node
        rootNode = new Node();
        movableComp = cell.getComponent(MovableComponent.class);
        RenderComponent rc = ClientContextJME.getWorldManager().getRenderManager().createRenderComponent(rootNode);
        this.addComponent(RenderComponent.class, rc);

        // Add a cell ref component to the entity. This will let us associated
        // the entity with the cell and make it easy to detect when we click
        // off of the cell
        CellRefComponent refComponent = new CellRefComponent(cell);
        this.addComponent(CellRefComponent.class, refComponent);
    }

    /**
     * Returns the affordance's cell.
     *
     * @return The Cell
     */
    protected Cell getCell() {
        return this.cell;
    }

    /**
     * Returns the scene root for the Cell's scene graph
     *
     * @return The scene graph root Node
     */
    protected Node getSceneGraphRoot() {
        CellRendererJME renderer = (CellRendererJME) cell.getCellRenderer(RendererType.RENDERER_JME);
        RenderComponent cellRC = (RenderComponent)renderer.getEntity().getComponent(RenderComponent.class);
        return cellRC.getSceneRoot();
    }

    /**
     * Sets the size of the affordance as a floating point value. Generally,
     * a size of 1.0 means the same size as the Cell geometry.
     *
     * @param size The size of the affordances.
     */
    public abstract void setSize(float size);

    /**
     * Removes the affordance from the cell.
     */
    public abstract void remove();

    // Make this entity pickable by adding a collision component to it
    protected void makeEntityPickable(Entity entity, Node node) {
        JMECollisionSystem collisionSystem = (JMECollisionSystem)ClientContextJME.getWorldManager().getCollisionManager().
                loadCollisionSystem(JMECollisionSystem.class);

        CollisionComponent cc = collisionSystem.createCollisionComponent(node);
        entity.addComponent(CollisionComponent.class, cc);
    }

    /**
     * Adds an Entity with its root node to the scene graph, using the super-
     * class Entity as the parent
     */
    protected void addSubEntity(Entity subEntity, Node subNode) {
        // Create the render component that associates the node with the Entity
        RenderManager rm = ClientContextJME.getWorldManager().getRenderManager();
        RenderComponent thisRC = rm.createRenderComponent(subNode);
        subEntity.addComponent(RenderComponent.class, thisRC);

        // Add this Entity to the parent Entity
        RenderComponent parentRC = (RenderComponent)this.getComponent(RenderComponent.class);
        thisRC.setAttachPoint(parentRC.getSceneRoot());
        this.addEntity(subEntity);
    }
}
