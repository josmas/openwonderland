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
import com.jme.scene.shape.Tube;
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
 * Affordance to rotate a cell along each major axis.
 * 
 * @author Jordan Slott <jslott@dev.java.net>
 */
public class RotateAffordance extends Affordance {
    private Node rootNode;
    private MovableComponent movableComp;

    /** An enumeration of the axis along which to effect the rotate motion */
    public enum RotateAxis {
        X_AXIS, Y_AXIS, Z_AXIS
    }

    /* The scaling of the outer radius of the tube */
    private static final float RADIUS_SCALE = 2f;

    /* The inner radius offset */
    private static final float RADIUS_WIDTH = 0.2f;

    /* The thickness of tube */
    private static final float THICKNESS_SCALE = 0.02f;

    private RotateAffordance(Cell cell) {
        super("Rotate", cell);
        
        // Create the root node of the cell and the render component to attach
        // to the Entity with the node
        rootNode = new Node();
        movableComp = cell.getComponent(MovableComponent.class);
        RenderComponent rc = ClientContextJME.getWorldManager().getRenderManager().createRenderComponent(rootNode);
        this.addComponent(RenderComponent.class, rc);

        // Figure out the bounds of the root entity of the cell and create a
        // tube to be just a bit larger than that
        CellRendererJME cellRC = (CellRendererJME)cell.getCellRenderer(RendererType.RENDERER_JME);
        RenderComponent entityRC = (RenderComponent)cellRC.getEntity().getComponent(RenderComponent.class);
        Node sceneRoot = entityRC.getSceneRoot();
        BoundingVolume bounds = sceneRoot.getWorldBound();
        float outerRadius = 0.0f, innerRadius = 0.0f;
        if (bounds instanceof BoundingSphere) {
            outerRadius = ((BoundingSphere)bounds).radius * RADIUS_SCALE;
            innerRadius = outerRadius - RADIUS_WIDTH;
        }
        else if (bounds instanceof BoundingBox) {
            float xExtent = ((BoundingBox)bounds).xExtent;
            float yExtent = ((BoundingBox)bounds).yExtent;
            float zExtent = ((BoundingBox)bounds).zExtent;
            outerRadius = Math.max(Math.max(xExtent, yExtent), zExtent) * RADIUS_SCALE;
            innerRadius = outerRadius - RADIUS_WIDTH;
        }

        // Set the width of the arrow to be a proportion of the length of the
        // arrows. Use the maximum length of the three axes to determine the
        // width
        float width = outerRadius * THICKNESS_SCALE;

        // Create a tube to rotate about the X axis. The tube is drawn in the
        // X-Z plane, so we must rotate 90 degrees about the +z axis so that the
        // axis of rotation is about +x axis.
        Entity xEntity = new Entity("Tube X");
        Node xNode = createTube("Tube X", outerRadius, innerRadius, width, ColorRGBA.red);
        Quaternion xQ = new Quaternion().fromAngleAxis(1.5707f, new Vector3f(0, 0, 1));
        xNode.setLocalRotation(xQ);
        addSubEntity(xEntity, xNode);
        addRotateListener(xEntity, xNode, RotateAxis.X_AXIS);

        // Create a tube to rotate about the Y axis. The tube is drawn in the
        // X-Z plane already.
        Entity yEntity = new Entity("Tube Y");
        Node yNode = createTube("Tube Y", outerRadius, innerRadius, width, ColorRGBA.green);
        addSubEntity(yEntity, yNode);
        addRotateListener(yEntity, yNode, RotateAxis.Y_AXIS);

        // Create a tube to rotate about the Z axis. The tube is drawn in the
        // X-Z plane, so we must rotate 90 degrees about the +x axis so that the
        // axis of rotation is about +z axis.
        Entity zEntity = new Entity("Tube Z");
        Node zNode = createTube("Tube Z", outerRadius, innerRadius, width, ColorRGBA.blue);
        Quaternion zQ = new Quaternion().fromAngleAxis(1.5707f, new Vector3f(1, 0, 0));
        zNode.setLocalRotation(zQ);
        addSubEntity(zEntity, zNode);
        addRotateListener(zEntity, zNode, RotateAxis.Z_AXIS);
    }

    public void remove() {
        CellRendererJME r = (CellRendererJME) cell.getCellRenderer(RendererType.RENDERER_JME);
        Entity entity = r.getEntity();
        entity.removeEntity(this);
    }
    
    public static RotateAffordance addToCell(Cell cell) {
        // First check to see if the cell has the moveable component. If not,
        // then do not add the affordance
        if (cell.getComponent(MovableComponent.class) == null) {
            Logger logger = Logger.getLogger(TranslateAffordance.class.getName());
            logger.warning("[AFFORDANCE] Cell " + cell.getName() + " does not " +
                    "have the moveable component.");
            return null;
        }

        RotateAffordance rotateAffordance = new RotateAffordance(cell);
        CellRendererJME r = (CellRendererJME) cell.getCellRenderer(RendererType.RENDERER_JME);
        Entity parentEntity = r.getEntity();
        RenderComponent thisRC = (RenderComponent)rotateAffordance.getComponent(RenderComponent.class);
        RenderComponent parentRC = (RenderComponent)parentEntity.getComponent(RenderComponent.class);
        thisRC.setAttachPoint(parentRC.getSceneRoot());
        parentEntity.addEntity(rotateAffordance);
        ClientContextJME.getWorldManager().addToUpdateList(rotateAffordance.rootNode);

        return rotateAffordance;
    }

    /**
     * Adds a rotation listener for the Entity and the node, given the axis along
     * which the rotate should take place.
     */
    private void addRotateListener(Entity entity, Node node, RotateAxis direction) {
        makeEntityPickable(entity, node);
        new RotationDragListener(direction).addToEntity(entity);
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
    * Creates the tube used for the rotation affordance, given its name, its
    * outer and inner radius, its thickness, and its color. Returns the Node
    * representing the geometry.
    */
    private Node createTube(String name, float outerRadius, float innerRadius,
            float thickness, ColorRGBA color) {

        // Create the disc with the name, radii, and thickness given. Set
        // the color of the tube.
        Tube t = new Tube(name, outerRadius, innerRadius, thickness, 50, 50);
        t.setSolidColor(color);

        // Create the main node and set the material state on the node so the
        // color shows up. Attach the tube to the node.
        Node n = new Node();
        RenderManager rm = ClientContextJME.getWorldManager().getRenderManager();
        MaterialState matState3 = (MaterialState)rm.createRendererState(RenderState.RS_MATERIAL);
        matState3.setDiffuse(color);
        n.setRenderState(matState3);
        n.attachChild(t);

        // Set the bound on the tube and update it
        t.setModelBound(new BoundingSphere());
        t.updateModelBound();
        return n;
    }

    /**
     * Inner class that handles the dragging movement and updates the rotation
     * of the cell accordingly
     */
    private class RotationDragListener extends EventClassListener {

        // The axis along which to effect the rotation
        private RotateAxis direction;

        // The intersection point on the entity over which the button was
        // pressed, in world coordinates.
        private Vector3f dragStartWorld;

        // The screen coordinates of the button press event.
        private Point dragStartScreen;

        // The rotation of the cell when the mouse button is first pressed.
        private Quaternion rotationOnPress = null;

        // The center of the affordance in world coordinates
        private Vector3f centerWorld;

        // The vector of the starting point of the drag with respect to the
        // center of the afforance
        private Vector3f dragStartVectorWorld;

        public RotationDragListener(RotateAxis direction) {
            this.direction = direction;
        }

        @Override
        public Class[] eventClassesToConsume() {
            return new Class[] { MouseEvent3D.class };
        }

        @Override
        public void commitEvent(Event event) {
            Logger logger = Logger.getLogger(RotateAffordance.class.getName());
            MouseEvent3D me = (MouseEvent3D) event;

            // Figure out where the initial mouse button press happened and
            // store the initial position. We also store the center of the
            // affordance.
            CellTransform transform = cell.getLocalTransform();
            if (event instanceof MouseButtonEvent3D) {
                MouseButtonEvent3D be = (MouseButtonEvent3D)event;
                if (be.isPressed() && be.getButton() == MouseButtonEvent3D.ButtonId.BUTTON1) {
                    // Figure out where the button press is in screen and world
                    // coordinates. Also fetch the current rotation for cell.
                    MouseEvent awtButtonEvent = (MouseEvent)be.getAwtEvent();
                    dragStartScreen = new Point(awtButtonEvent.getX(), awtButtonEvent.getY());
                    dragStartWorld = be.getIntersectionPointWorld();
                    rotationOnPress = transform.getRotation(null);
                    
                    // Figure out the world coordinates of the center of the
                    // affordance.
                    Entity entity = event.getEntity();
                    RenderComponent rc = (RenderComponent)entity.getComponent(RenderComponent.class);
                    centerWorld = rc.getSceneRoot().getWorldTranslation();
                    
                    // Compute the vector from the starting point of the drag
                    // to the center of the affordance in world coordinates.
                    dragStartVectorWorld = dragStartWorld.subtract(centerWorld);
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
            Vector3f dragWorld = dragEvent.getDragVectorWorld(dragStartWorld,
                    dragStartScreen, new Vector3f());

            // Figure out what the vector is of the current drag location in
            // world coodinates. This gives a vector from the center of the
            // affordance. We just take the vector (from the center) of the
            // start of the drag and add the bit we dragged the mouse.
            Vector3f dragEndVectorWorld = dragStartVectorWorld.add(dragWorld);

            // Formulate the two vectors in two dimensions. For now, we just
            // ignore the third dimension (set it to 0). The two new vectors
            // depend upon what axis we are rotating around. We also figure
            // out the axis normal and the axis of rotation
            Vector3f v1 = null, v2 = null, normal = null, axis = null;
            switch (direction) {
                case X_AXIS:
                    v1 = new Vector3f(0, dragStartVectorWorld.y, dragStartVectorWorld.z).normalize();
                    v2 = new Vector3f(0, dragEndVectorWorld.y, dragEndVectorWorld.z).normalize();
                    normal = new Vector3f(1, 0, 0);
                    axis = new Vector3f(-1, 0, 0);
                    break;

                case Y_AXIS:
                    v1 = new Vector3f(dragStartVectorWorld.x, 0, dragStartVectorWorld.z).normalize();
                    v2 = new Vector3f(dragEndVectorWorld.x, 0, dragEndVectorWorld.z).normalize();
                    normal = new Vector3f(0, 1, 0);
                    axis = new Vector3f(0, -1, 0);
                    break;

                case Z_AXIS:
                    v1 = new Vector3f(dragStartVectorWorld.x, dragStartVectorWorld.y, 0).normalize();
                    v2 = new Vector3f(dragEndVectorWorld.x, dragEndVectorWorld.y, 0).normalize();
                    normal = new Vector3f(0, 0, 1);
                    axis = new Vector3f(0, 0, -1);
                    break;
                    
                default:
                    // This should never happen, so just return
                    return;
            }

            // Compute the signed angle between v1 and v2. We do this with the
            // following formula: angle = atan2(normal dot (v1 cross x2), v1 dot v2)
            float dotProduct = v2.dot(v1);
            Vector3f crossProduct = v2.cross(v1);
            double angle = Math.atan2(normal.dot(crossProduct), dotProduct);

            // Rotate the object along the defined axis and angle.
            Quaternion q = new Quaternion().fromAngleAxis((float)angle, axis);
            Quaternion newRotation = rotationOnPress.mult(q);
            transform.setRotation(newRotation);
            movableComp.localMoveRequest(transform);
        }
    }
}
