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
import java.util.logging.Logger;
import org.jdesktop.mtgame.CollisionComponent;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.mtgame.JMECollisionSystem;
import org.jdesktop.mtgame.RenderComponent;
import org.jdesktop.mtgame.RenderManager;
import org.jdesktop.mtgame.RenderUpdater;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.cell.Cell.RendererType;
import org.jdesktop.wonderland.client.cell.CellComponent;
import org.jdesktop.wonderland.client.cell.ComponentChangeListener;
import org.jdesktop.wonderland.client.cell.ComponentChangeListener.ChangeType;
import org.jdesktop.wonderland.client.cell.MovableComponent;
import org.jdesktop.wonderland.client.jme.CellRefComponent;
import org.jdesktop.wonderland.client.jme.ClientContextJME;
import org.jdesktop.wonderland.client.jme.cellrenderer.CellRendererJME;
import org.jdesktop.wonderland.common.cell.messages.CellServerComponentMessage;
import org.jdesktop.wonderland.common.messages.ErrorMessage;
import org.jdesktop.wonderland.common.messages.ResponseMessage;
import org.jdesktop.wonderland.modules.affordances.client.cell.AffordanceException;

/**
 * The base class of all Affordances (manipulators)
 * 
 * @author Jordan Slott <jslott@dev.java.net>
 */
public abstract class Affordance extends Entity {

    protected static Logger logger = Logger.getLogger(Affordance.class.getName());
    protected Node rootNode;
    protected MovableComponent movableComp;
    protected Cell cell = null;
    
    /** Constructor, takes the cell */
    public Affordance(String name, Cell cell) throws AffordanceException {
        super(name);
        this.cell = cell;

        // Create the root node of the cell and the render component to attach
        // to the Entity with the node
        rootNode = new Node();
        RenderComponent rc = ClientContextJME.getWorldManager().getRenderManager().createRenderComponent(rootNode);
        this.addComponent(RenderComponent.class, rc);

        // Add a cell ref component to the entity. This will let us associated
        // the entity with the cell and make it easy to detect when we click
        // off of the cell
        CellRefComponent refComponent = new CellRefComponent(cell);
        this.addComponent(CellRefComponent.class, refComponent);

        // Check to see if the movable component already exists on the cell. If
        // not, try to add it
        checkMovableComponent();
        if (movableComp == null) {
            addMovableComponent(cell);
        }

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

    /**
     * Adds this affordance to the scene graph.
     */
    public void addAffordanceToScene() {
        // We should add a listener just in case the movable component gets
        // added. We add the listener first, just in case the component gets
        // added inbetween the time we check and the time we add the listener.
        if (movableComp == null) {
            cell.addComponentChangeListener(new ComponentChangeListener() {
                public void componentChanged(Cell cell, ChangeType type, CellComponent component) {
                    if (type == ChangeType.ADDED && component instanceof MovableComponent) {
                        checkMovableComponent();
                        updateSceneGraph();
                    }
                }
            });
        }

        // Recheck whether the movable component exists here. If so, then add
        // to the scene graph right away.
        checkMovableComponent();
        updateSceneGraph();

    }

    /**
     * Checks whether the movable component exists and sets it if so.
     */
    private synchronized void checkMovableComponent() {
        if (movableComp == null) {
            movableComp = cell.getComponent(MovableComponent.class);
        }
    }

    /* True if the entity has been added to the scene graph. */
    private boolean addedToSceneGraph = false;

    /**
     * Updates the scene graph to add this affordance
     */
    private synchronized void updateSceneGraph() {
        // Since we are updating the scene graph, we need to put this in a
        // special update thread. Also this method is synchronized and we keep
        // track of whether it has been added in a boolean. This ensures that
        // it only gets added once.
        if (addedToSceneGraph == false) {
            ClientContextJME.getWorldManager().addRenderUpdater(new RenderUpdater() {
                public void update(Object arg0) {
                    ClientContextJME.getWorldManager().addEntity((Entity) arg0);
                    ClientContextJME.getWorldManager().addToUpdateList(((Affordance) arg0).rootNode);
                }
            }, this);
            addedToSceneGraph = true;
        }
    }

    /**
     * Adds the movable component, assumes it does not already exist.
     *
     * @param affordance
     * @param cell
     * @throws AffordanceException Upon error adding the component
     */
    private void addMovableComponent(Cell cell) throws AffordanceException {
        
        // Go ahead and try to add the affordance. If we cannot, then throw
        // an exception right away.
        String className = "org.jdesktop.wonderland.server.cell.MovableComponentMO";
        CellServerComponentMessage cscm = CellServerComponentMessage.newAddMessage(cell.getCellID(), className);
        ResponseMessage response = cell.sendCellMessageAndWait(cscm);
        if (response instanceof ErrorMessage) {
            throw new AffordanceException("Unable to add movable component " +
                    "for Cell " + cell.getName() + " with ID " +
                    cell.getCellID());
        }
    }

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
