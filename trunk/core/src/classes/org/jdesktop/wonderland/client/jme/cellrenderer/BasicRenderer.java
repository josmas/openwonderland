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
package org.jdesktop.wonderland.client.jme.cellrenderer;

import com.jme.bounding.BoundingSphere;
import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import com.jme.scene.Node;
import com.jme.scene.Spatial;
import com.jme.scene.state.RenderState;
import com.jme.scene.state.ZBufferState;
import java.util.logging.Logger;
import org.jdesktop.mtgame.CollisionComponent;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.mtgame.JMECollisionSystem;
import org.jdesktop.mtgame.NewFrameCondition;
import org.jdesktop.mtgame.PostEventCondition;
import org.jdesktop.mtgame.ProcessorArmingCollection;
import org.jdesktop.mtgame.ProcessorComponent;
import org.jdesktop.mtgame.RenderComponent;
import org.jdesktop.mtgame.WorldManager;
import org.jdesktop.wonderland.client.cell.MovableComponent;
import org.jdesktop.wonderland.client.jme.ClientContextJME;
import org.jdesktop.wonderland.common.ExperimentalAPI;
import org.jdesktop.wonderland.common.cell.CellTransform;

/**
 *
 * Abstract Renderer class that implements CellRendererJME
 * 
 * @author paulby
 */
@ExperimentalAPI
public abstract class BasicRenderer implements CellRendererJME {
    
    protected static Logger logger = Logger.getLogger(BasicRenderer.class.getName());
    protected Cell cell;
    protected Entity entity;
    protected Node rootNode;
    protected MoveProcessor moveProcessor = null;
    
    private Vector3f tmpV3f = new Vector3f();
    private Quaternion tmpQuat = new Quaternion();
    
    private static ZBufferState zbuf = null;
    
    static {
        zbuf = (ZBufferState) ClientContextJME.getWorldManager().getRenderManager().createRendererState(RenderState.RS_ZBUFFER);
        zbuf.setEnabled(true);
        zbuf.setFunction(ZBufferState.TestFunction.LessThanOrEqualTo);
    }
    
    public BasicRenderer(Cell cell) {
        this.cell = cell;
    }
    
    protected Entity createEntity() {
        Entity ret = new Entity(this.getClass().getName()+"_"+cell.getCellID());
        
        rootNode = createSceneGraph(ret);
        addRenderState(rootNode);

        addDefaultComponents(ret, rootNode);

        return ret;        
    }
    
    /**
     * Add the default renderstate to the root node. Override this method
     * if you want to apply a different RenderState
     * @param node
     */
    protected void addRenderState(Node node) {
        node.setRenderState(zbuf);
    }
    
    protected void addDefaultComponents(Entity entity, Node rootNode) {
        if (cell.getComponent(MovableComponent.class)!=null) {
            if (rootNode==null) {
                logger.warning("Cell is movable, but has no root node !");
            } else {           
                // The cell is movable so create a move processor
                moveProcessor = new MoveProcessor(ClientContextJME.getWorldManager(), rootNode);
                entity.addComponent(MoveProcessor.class, moveProcessor);
            }
        }

        if (rootNode!=null) {
            RenderComponent rc = ClientContextJME.getWorldManager().getRenderManager().createRenderComponent(rootNode);
            entity.addComponent(RenderComponent.class, rc);

            JMECollisionSystem collisionSystem = (JMECollisionSystem)
                    ClientContextJME.getWorldManager().getCollisionManager().loadCollisionSystem(JMECollisionSystem.class);

            CollisionComponent cc = collisionSystem.createCollisionComponent(rootNode);
            entity.addComponent(CollisionComponent.class, cc);
        }

    }

    /**
     * Create the scene graph
     * @return
     */
    protected abstract Node createSceneGraph(Entity entity);

    /**
     * Apply the transform to the jme node
     * @param node
     * @param transform
     */
    public static void applyTransform(Spatial node, CellTransform transform) {
        node.setLocalRotation(transform.getRotation(null));
        node.setLocalScale(transform.getScaling(null));
        node.setLocalTranslation(transform.getTranslation(null));
    }
    
    public Entity getEntity() {
        synchronized(this) {
            System.out.println("Get Entity "+this.getClass().getName());
            if (entity==null)
                entity = createEntity();
        }
        return entity;
    }
    
    public void cellTransformUpdate(CellTransform worldTransform) {
        if (moveProcessor!=null) {
            moveProcessor.cellMoved(worldTransform);
        }
    }
    
    /**
     * An mtgame ProcessorCompoenent to process cell moves.
     */
    public class MoveProcessor extends ProcessorComponent {

        private CellTransform cellTransform;
        private boolean dirty = false;
        private Node node;
        private WorldManager worldManager;

        private boolean isChained = false;
        private NewFrameCondition postCondition = new NewFrameCondition(this);
        
        public MoveProcessor(WorldManager worldManager, Node node) {
            this.node = node;
            this.worldManager = worldManager;
        }
        
        @Override
        public void compute(ProcessorArmingCollection arg0) {
        }

        @Override
        public void commit(ProcessorArmingCollection arg0) {
            synchronized(this) {
                if (dirty) {
                    node.setLocalTranslation(cellTransform.getTranslation(tmpV3f));
                    node.setLocalRotation(cellTransform.getRotation(tmpQuat));
//                    System.err.println("BasicRenderer.cellMoved "+tmpV3f);
                    dirty = false;
                    worldManager.addToUpdateList(node);
//            System.err.println("--------------------------------");
                }
            }
        }

        @Override
        
        public void initialize() {
            setArmingCondition(postCondition);
        }

        /**
         * Notify the MoveProcessor that the cell has moved
         * 
         * @param transform cell transform in world coordinates
         */
        public void cellMoved(CellTransform transform) {
            synchronized(this) {
                this.cellTransform = transform;
                dirty = true;


            }
        }

        public void setChained(boolean isChained) {
            this.isChained = isChained;
            if (isChained)
                setArmingCondition(null);
            else 
                setArmingCondition(postCondition);
        }
    }
}
