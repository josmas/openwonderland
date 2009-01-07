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

package org.jdesktop.wonderland.modules.affordances.client.jme;

import com.jme.bounding.BoundingBox;
import com.jme.bounding.BoundingSphere;
import com.jme.bounding.BoundingVolume;
import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import com.jme.renderer.ColorRGBA;
import com.jme.scene.Node;
import com.jme.scene.shape.Arrow;
import com.jme.scene.state.MaterialState;
import com.jme.scene.state.RenderState;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.util.logging.Logger;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.mtgame.RenderComponent;
import org.jdesktop.mtgame.RenderManager;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.cell.Cell.RendererType;
import org.jdesktop.wonderland.client.cell.MovableComponent;
import org.jdesktop.wonderland.client.input.Event;
import org.jdesktop.wonderland.client.input.EventClassListener;
import org.jdesktop.wonderland.client.jme.ClientContextJME;
import org.jdesktop.wonderland.client.jme.cellrenderer.CellRendererJME;
import org.jdesktop.wonderland.client.jme.input.MouseButtonEvent3D;
import org.jdesktop.wonderland.client.jme.input.MouseDraggedEvent3D;
import org.jdesktop.wonderland.client.jme.input.MouseEvent3D;
import org.jdesktop.wonderland.common.cell.CellTransform;

/**
 * Visual affordance (manipulator) to move a cell around in the world.
 * 
 * @author Jordan Slott <jslott@dev.java.net>
 */
public class TranslateAffordance extends Affordance {
    private Node rootNode;
    private MovableComponent movableComp;

    /** An enumeration of the axis along which to effect the drag motion */
    public enum TranslateAxis {
        X_AXIS, Y_AXIS, Z_AXIS
    }

    /**
     * Private constructor, use the addToCell() method instead.
     * @param cell
     */
    private TranslateAffordance(Cell cell) {
        super("Translate", cell);

        // Create the root node of the cell and the render component to attach
        // to the Entity with the node
        rootNode = new Node();
        movableComp = cell.getComponent(MovableComponent.class);
        RenderComponent rc = ClientContextJME.getWorldManager().getRenderManager().createRenderComponent(rootNode);
        this.addComponent(RenderComponent.class, rc);

        // Figure out the bounds of the cell and create an arrow to be just
        // a bit larger than that
        BoundingVolume bounds = cell.getLocalBounds();
        float xExtent = 0, yExtent = 0, zExtent = 0;
        if (bounds instanceof BoundingSphere) {
            xExtent = yExtent = zExtent = ((BoundingSphere)bounds).radius;
        }
        else if (bounds instanceof BoundingBox) {
            xExtent = ((BoundingBox)bounds).xExtent;
            yExtent = ((BoundingBox)bounds).yExtent;
            zExtent = ((BoundingBox)bounds).zExtent;
        }
        
        // Create a red arrow in the +x direction. We arrow we get back is
        // pointed in the +y direction, so we rotate around the -z axis to
        // orient the arrow properly.
        Entity xEntity = new Entity("Entity X");
        Node xNode = createArrow("Arrow X", xExtent * 1.1f, 0.05f, ColorRGBA.red);
        Quaternion xRotation = new Quaternion().fromAngleAxis((float)Math.PI / 2, new Vector3f(0, 0, -1));
        xNode.setLocalRotation(xRotation);
        addSubEntity(xEntity, xNode);
        addDragListener(xEntity, xNode, TranslateAxis.X_AXIS);

        // Create a green arrow in the +y direction. We arrow we get back is
        // pointed in the +y direction.
        Entity yEntity = new Entity("Entity Y");
        Node yNode = createArrow("Arrow Y", yExtent * 1.1f, 0.05f, ColorRGBA.green);
        addSubEntity(yEntity, yNode);
        addDragListener(yEntity, yNode, TranslateAxis.Y_AXIS);

        // Create a red arrow in the +z direction. We arrow we get back is
        // pointed in the +y direction, so we rotate around the +x axis to
        // orient the arrow properly.
        Entity zEntity = new Entity("Entity Z");
        Node zNode = createArrow("Arrow Z", zExtent * 1.1f, 0.05f, ColorRGBA.blue);
        Quaternion zRotation = new Quaternion().fromAngleAxis((float)Math.PI / 2, new Vector3f(1, 0, 0));
        zNode.setLocalRotation(zRotation);
        addSubEntity(zEntity, zNode);
        addDragListener(zEntity, zNode, TranslateAxis.Z_AXIS);
    }
    
    public static TranslateAffordance addToCell(Cell cell) {
        // First check to see if the cell has the moveable component. If not,
        // then do not add the affordance
        if (cell.getComponent(MovableComponent.class) == null) {
            Logger logger = Logger.getLogger(TranslateAffordance.class.getName());
            logger.warning("[AFFORDANCE] Cell " + cell.getName() + " does not " +
                    "have the moveable component.");
            return null;
        }

        TranslateAffordance translateAffordance = new TranslateAffordance(cell);
        CellRendererJME r = (CellRendererJME) cell.getCellRenderer(RendererType.RENDERER_JME);
        Entity parentEntity = r.getEntity();
        RenderComponent thisRC = (RenderComponent)translateAffordance.getComponent(RenderComponent.class);
        RenderComponent parentRC = (RenderComponent)parentEntity.getComponent(RenderComponent.class);
        thisRC.setAttachPoint(parentRC.getSceneRoot());
        parentEntity.addEntity(translateAffordance);
        ClientContextJME.getWorldManager().addToUpdateList(translateAffordance.rootNode);

        return translateAffordance;
    }

    /**
     * Removes the affordance from the cell
     */
    public void remove() {
        CellRendererJME r = (CellRendererJME) cell.getCellRenderer(RendererType.RENDERER_JME);
        Entity entity = r.getEntity();
        entity.removeEntity(this);
    }

    /**
     * Adds a drag listener for the Entity and the node, given the axis along
     * which the drag should take place.
     */
    private void addDragListener(Entity entity, Node node, TranslateAxis direction) {
        makeEntityPickable(entity, node);
        new TranslateDragListener(direction).addToEntity(entity);
    }

    /**
     * Adds an Entity with its root node to the scene graph, using the super-
     * class Entity as the parent
     */
    private void addSubEntity(Entity subEntity, Node subNode) {
        // Create the render component that associates the node with the Entity
        RenderManager rm = ClientContextJME.getWorldManager().getRenderManager();
        RenderComponent thisRC = rm.createRenderComponent(subNode);
        subEntity.addComponent(RenderComponent.class, thisRC);

        // Add this Entity to the parent Entity
        RenderComponent parentRC = (RenderComponent)this.getComponent(RenderComponent.class);
        thisRC.setAttachPoint(parentRC.getSceneRoot());
        this.addEntity(subEntity);
    }

    /**
     * Creates a double-ended arrow, given its half-length, thickness and color.
     * Returns a Node representing the new geometry.
     */
    private Node createArrow(String name, float length, float width, ColorRGBA color) {

        // Create the two arrows with the proper name, length, thickness, and
        // color.
        Arrow a1 = new Arrow(name + " 1", length, width);
        a1.setSolidColor(color);
        Arrow a2 = new Arrow(name + " 2", length, width);
        a2.setSolidColor(color);

        // Create the main node and set the material state on the node so the
        // color shows up
        Node n = new Node();
        RenderManager rm = ClientContextJME.getWorldManager().getRenderManager();
        MaterialState matState3 = (MaterialState)rm.createRendererState(RenderState.RS_MATERIAL);
        matState3.setDiffuse(color);
        n.setRenderState(matState3);

        // Create a sub-node to hold the first arrow. We must translate it up,
        // so that the end is at (0, 0, 0) in the local coordinate space of
        // the node we return
        Node subNode1 = new Node();
        subNode1.setLocalTranslation(0, length / 2, 0);
        subNode1.attachChild(a1);

        // Create a sub-node to hold the second arrow. We must rotate it 180
        // degrees (about the +y axis since arrows by default point up). We
        // also must translate it down. Attach the second arrow to this node.
        Node subNode2 = new Node();
        Quaternion q = new Quaternion().fromAngleAxis((float)Math.PI, new Vector3f(0, 0, 1));
        subNode2.setLocalRotation(q);
        subNode2.setLocalTranslation(0, -length / 2, 0);
        subNode2.attachChild(a2);

        // Attach the first arrow and the subnode to the main node
        n.attachChild(subNode1);
        n.attachChild(subNode2);

        // Set the bounds on the arrows and update them
        a1.setModelBound(new BoundingSphere());
        a1.updateModelBound();
        a2.setModelBound(new BoundingSphere());
        a2.updateModelBound();

        return n;
    }

    /**
     * Inner class that handles the dragging movement and updates the position
     * of the cell accordingly
     */
    private class TranslateDragListener extends EventClassListener {

        // The axis along which to effect the translation
        private TranslateAxis direction;

        // The intersection point on the entity over which the button was
        // pressed, in world coordinates.
        private Vector3f dragStartWorld;

        // The screen coordinates of the button press event.
        private Point dragStartScreen;

        // The translation of the cell when the mouse button is first pressed.
        private Vector3f translationOnPress = null;

        public TranslateDragListener(TranslateAxis direction) {
            this.direction = direction;
        }

        @Override
        public Class[] eventClassesToConsume() {
            return new Class[] { MouseEvent3D.class };
        }

        @Override
        public void commitEvent(Event event) {

            // Figure out where the initial mouse button press happened and
            // store the initial position
            CellTransform transform = cell.getLocalTransform();
            if (event instanceof MouseButtonEvent3D) {
                MouseButtonEvent3D buttonEvent = (MouseButtonEvent3D) event;
                if (buttonEvent.isPressed() && buttonEvent.getButton() == MouseButtonEvent3D.ButtonId.BUTTON1) {
                    MouseEvent awtButtonEvent = (MouseEvent) buttonEvent.getAwtEvent();
                    dragStartScreen = new Point(awtButtonEvent.getX(), awtButtonEvent.getY());
                    dragStartWorld = buttonEvent.getIntersectionPointWorld();
                    translationOnPress = transform.getTranslation(null);
                }
                return;
            }

            // If not a drag motion, just return, we don't care about the event
            if (!(event instanceof MouseDraggedEvent3D)) {
                return;
            }

            // Get the vector of the drag motion from the initial starting
            // point in world coordinates.
            MouseDraggedEvent3D dragEvent = (MouseDraggedEvent3D) event;
            Vector3f dragVector = dragEvent.getDragVectorWorld(dragStartWorld, dragStartScreen,
                    new Vector3f());

            // Figure out how to translate based upon the axis of the affordance
            Vector3f addVector;
            switch (direction) {
                case X_AXIS: addVector = new Vector3f(dragVector.x, 0, 0); break;
                case Y_AXIS: addVector = new Vector3f(0, dragVector.y, 0); break;
                case Z_AXIS: addVector = new Vector3f(0, 0, dragVector.z); break;
                default: addVector = new Vector3f(); break;
            }

            // Move the cell via the moveable comopnent
            Vector3f newTranslation = translationOnPress.add(addVector);
            transform.setTranslation(newTranslation);
            movableComp.localMoveRequest(transform);
        }
    }
}
