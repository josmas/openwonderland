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

import com.jme.bounding.BoundingBox;
import com.jme.bounding.BoundingSphere;
import com.jme.bounding.BoundingVolume;
import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import com.jme.renderer.ColorRGBA;
import com.jme.scene.GeometricUpdateListener;
import com.jme.scene.Node;
import com.jme.scene.Spatial;
import com.jme.scene.shape.Arrow;
import com.jme.scene.state.MaterialState;
import com.jme.scene.state.RenderState;
import com.jme.scene.state.ZBufferState;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.util.logging.Logger;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.mtgame.RenderComponent;
import org.jdesktop.mtgame.RenderManager;
import org.jdesktop.mtgame.RenderUpdater;
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

    /* The length scaling factor for each arrow */
    private static final float LENGTH_SCALE = 1.5f;

    /* The width scaling factor for each arrow */
    private static final float WIDTH_SCALE = 0.10f;

    /* The current scale of the affordance w.r.t the size of the cell */
    private float currentScale = LENGTH_SCALE;

    /* The original extent of the object, before it was modified */
    private float xExtent = 0.0f, yExtent = 0.0f, zExtent = 0.0f;

    /* The root of the scene graph of the cell */
    private Node sceneRoot = null;

    /** An enumeration of the axis along which to effect the drag motion */
    public enum TranslateAxis {
        X_AXIS, Y_AXIS, Z_AXIS
    }

    /* The nodes representing the double-edged arrows for each axis */
    private Node xNode = null, yNode = null, zNode = null;

    private static ZBufferState zbuf = null;
    static {
        zbuf = (ZBufferState) ClientContextJME.getWorldManager().getRenderManager().createRendererState(RenderState.RS_ZBUFFER);
        zbuf.setEnabled(true);
        zbuf.setFunction(ZBufferState.TestFunction.LessThanOrEqualTo);
    }

    /* Listener for changes in the translation of the cell */
    private GeometricUpdateListener updateListener = null;

    /**
     * Private constructor, use the addToCell() method instead.
     * @param cell
     */
    private TranslateAffordance(Cell cell) {
        super("Translate", cell);

        // Figure out the bounds of the root entity of the cell and create an
        // arrow to be just a bit larger than that
        sceneRoot = getSceneGraphRoot();
        BoundingVolume bounds = sceneRoot.getWorldBound();
        if (bounds instanceof BoundingSphere) {
            xExtent = yExtent = zExtent = ((BoundingSphere)bounds).radius;
        }
        else if (bounds instanceof BoundingBox) {
            xExtent = ((BoundingBox)bounds).xExtent;
            yExtent = ((BoundingBox)bounds).yExtent;
            zExtent = ((BoundingBox)bounds).zExtent;
        }

        // Set the width of the arrow to be a proportion of the length of the
        // arrows. Use the maximum length of the three axes to determine the
        // width
        float width = /*Math.max(Math.max(xExtent, yExtent), zExtent) **/ WIDTH_SCALE;

        // Fetch the world translation for the root node of the cell and set
        // the translation for this entity root node
        Vector3f translation = sceneRoot.getWorldTranslation();
        rootNode.setLocalTranslation(translation);
        
        // Create a red arrow in the +x direction. We arrow we get back is
        // pointed in the +y direction, so we rotate around the -z axis to
        // orient the arrow properly.
        Entity xEntity = new Entity("Entity X");
        xNode = createArrow("Arrow X", xExtent, width, ColorRGBA.red);
        Quaternion xRotation = new Quaternion().fromAngleAxis((float)Math.PI / 2, new Vector3f(0, 0, -1));
        xNode.setLocalRotation(xRotation);
        xNode.setLocalScale(new Vector3f(1.0f, LENGTH_SCALE, 1.0f));
        xNode.setRenderState(zbuf);
        addSubEntity(xEntity, xNode);
        addDragListener(xEntity, xNode, TranslateAxis.X_AXIS);

        // Create a green arrow in the +y direction. We arrow we get back is
        // pointed in the +y direction.
        Entity yEntity = new Entity("Entity Y");
        yNode = createArrow("Arrow Y", yExtent, width, ColorRGBA.green);
        yNode.setLocalScale(new Vector3f(1.0f, LENGTH_SCALE, 1.0f));
        yNode.setRenderState(zbuf);
        addSubEntity(yEntity, yNode);
        addDragListener(yEntity, yNode, TranslateAxis.Y_AXIS);

        // Create a red arrow in the +z direction. We arrow we get back is
        // pointed in the +y direction, so we rotate around the +x axis to
        // orient the arrow properly.
        Entity zEntity = new Entity("Entity Z");
        zNode = createArrow("Arrow Z", zExtent, width, ColorRGBA.blue);
        Quaternion zRotation = new Quaternion().fromAngleAxis((float)Math.PI / 2, new Vector3f(1, 0, 0));
        zNode.setLocalRotation(zRotation);
        zNode.setRenderState(zbuf);
        zNode.setLocalScale(new Vector3f(1.0f, LENGTH_SCALE, 1.0f));
        addSubEntity(zEntity, zNode);
        addDragListener(zEntity, zNode, TranslateAxis.Z_AXIS);

        // Listen for changes to the cell's translation and apply the same
        // update to the root node of the affordances. We also re-set the size
        // of the affordances: this handles the case where the bounds of the
        // scene graph has changed and we need to update the affordances
        // accordingly.
        final Node[] nodeArray = new Node[1];
        nodeArray[0] = rootNode;
        sceneRoot.addGeometricUpdateListener(updateListener = new GeometricUpdateListener() {
            public void geometricDataChanged(Spatial arg0) {
                Vector3f translation = arg0.getWorldTranslation();
                nodeArray[0].setLocalTranslation(translation);
                setSize(currentScale);
                ClientContextJME.getWorldManager().addToUpdateList(nodeArray[0]);
            }
        });
    }
    
    public static TranslateAffordance addToCell(Cell cell) {
        Logger logger = Logger.getLogger(TranslateAffordance.class.getName());

        // First check to see if the cell has the moveable component. If not,
        // then do not add the affordance
        if (cell.getComponent(MovableComponent.class) == null) {
            logger.warning("[AFFORDANCE] Cell " + cell.getName() + " does not " +
                    "have the moveable component.");
            return null;
        }

        // Create the translate affordance entity and add it to the scene graph.
        // Since we are updating the scene graph, we need to put this in a
        // special update thread.
        TranslateAffordance affordance = new TranslateAffordance(cell);
        ClientContextJME.getWorldManager().addRenderUpdater(new RenderUpdater() {
            public void update(Object arg0) {
                ClientContextJME.getWorldManager().addEntity((Entity)arg0);
                ClientContextJME.getWorldManager().addToUpdateList(((TranslateAffordance)arg0).rootNode);
            }}, affordance);
        return affordance;
    }

    public void setSize(float size) {
        // To set the scale properly, we need to compute the scale w.r.t the
        // current size of the object as a ratio of the original size of the
        // object (in case the size of the object has changed).
        currentScale = size;
        float xScale = 0.0f, yScale = 0.0f, zScale = 0.0f;
        BoundingVolume bounds = sceneRoot.getWorldBound();
        if (bounds instanceof BoundingSphere) {
            float newExtent = ((BoundingSphere)bounds).radius;
            xScale = (newExtent / xExtent) * currentScale;
            yScale = (newExtent / yExtent) * currentScale;
            zScale = (newExtent / zExtent) * currentScale;
        }
        else if (bounds instanceof BoundingBox) {
            float newXExtent = ((BoundingBox)bounds).xExtent;
            float newYExtent = ((BoundingBox)bounds).yExtent;
            float newZExtent = ((BoundingBox)bounds).zExtent;
            xScale = (newXExtent / xExtent) * currentScale;
            yScale = (newYExtent / yExtent) * currentScale;
            zScale = (newZExtent / zExtent) * currentScale;
        }

        // In order to set the size of the arrows, we just set the scaling. Note
        // that we set the scaling along the +y axis since all arrows are
        // created facing that direction.
        xNode.setLocalScale(new Vector3f(1.0f, xScale, 1.0f));
        yNode.setLocalScale(new Vector3f(1.0f, yScale, 1.0f));
        zNode.setLocalScale(new Vector3f(1.0f, zScale, 1.0f));
        ClientContextJME.getWorldManager().addToUpdateList(xNode);
        ClientContextJME.getWorldManager().addToUpdateList(yNode);
        ClientContextJME.getWorldManager().addToUpdateList(zNode);
    }

    /**
     * Removes the affordance from the cell
     */
    public void remove() {
        // Remove the Entity from the scene graph. We also want to unregister
        // the listener from the cell's node. We need to do this in a special
        // update thread
        ClientContextJME.getWorldManager().addRenderUpdater(new RenderUpdater() {
            public void update(Object arg0) {
                ClientContextJME.getWorldManager().removeEntity(TranslateAffordance.this);
                CellRendererJME renderer = (CellRendererJME) cell.getCellRenderer(RendererType.RENDERER_JME);
                RenderComponent cellRC = (RenderComponent) renderer.getEntity().getComponent(RenderComponent.class);
                cellRC.getSceneRoot().removeGeometricUpdateListener(updateListener);
            }}, null);
    }

    /**
     * Adds a drag listener for the Entity and the node, given the axis along
     * which the drag should take place.
     */
    private void addDragListener(Entity entity, Node node, TranslateAxis direction) {
        makeEntityPickable(entity, node);
        TranslateDragListener l = new TranslateDragListener(direction);
        l.addToEntity(entity);
    }

    /**
     * Creates a double-ended arrow, given its half-length, thickness and color.
     * Returns a Node representing the new geometry. Fills in the affordance
     * arrow object with each jME arrow object.
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
