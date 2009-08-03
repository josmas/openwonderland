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
package org.jdesktop.wonderland.modules.buttonboxtest1.client.jme.cellrenderer;

import com.jme.bounding.BoundingBox;
import com.jme.math.Vector3f;
import com.jme.renderer.ColorRGBA;
import com.jme.scene.Node;
import com.jme.scene.shape.Box;
import com.jme.scene.state.MaterialState;
import com.jme.scene.state.RenderState;
import com.jme.system.DisplaySystem;
import org.jdesktop.mtgame.CollisionComponent;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.mtgame.JMECollisionSystem;
import org.jdesktop.mtgame.RenderComponent;
import org.jdesktop.wonderland.client.jme.ClientContextJME;
import org.jdesktop.wonderland.client.input.EventListener;
import org.jdesktop.wonderland.client.jme.cellrenderer.BasicRenderer;
import org.jdesktop.mtgame.RenderUpdater;

/**
 * A ButtonBox is a 3D component which provides a configurable number of 3D box
 * shaped buttons embedded in a 3D box which acts as a base. In addition to 
 * specifying the number of buttons, the creator can specify the dimensions and 
 * spacing of the buttons as well as the height and depth of the base. The
 * width of the base is determined from the button dimensions and spacing.
 *
 * The colors of the base and individual buttons can be specified with the methods 
 * <code>setBaseColor</code> and <code>setButtonColor</code>.
 *
 * The button box is made visible by inserting it into a cell's entity tree. This 
 * can be done with the method <code>attachToEntity</code>. An entity in the cell's 
 * entity tree must be provided. This entity will become the parent of the button box 
 * entity.
 *
 * You can register a mouse event listener with the button box. The listener
 * will receive mouse events which occur over any entity in the box. Individual objects may be
 * distinguished by the node of the mouse event returned by <code>MouseEvent3D.getNode</code>.
 *
 * @author dj
 */
public class ButtonBox {

    /** The height of the base box. */
    private float baseHeight;
    /** The depth of the base box. */
    private float baseDepth;
    /** The number of buttons */
    private int numButtons;
    /** The width of an individual button (same for all buttons). */
    private float buttonWidth;
    /** The height of an individual button (same for all buttons). */
    private float buttonHeight;
    /**
     * The depth of an individual button. This is also happens to be
     * the twice the amount infront of the box that the buttons stick out.
     */
    private float buttonDepth;
    /** The amount of space in between each button and at the ends. */
    private float buttonSpacing;
    /** The color of each button. The leftmost button is button 0. */
    private ColorRGBA[] buttonColors;
    /** The color of the base box. */
    private ColorRGBA baseColor = new ColorRGBA(0.6f, 0.6f, 0.6f, 1.0f);
    /** The geometry for the base. */
    private Box baseBox;
    /** The geometries for the buttons. */
    private Box[] buttonBoxes;
    /** The scene graph node for the base. */
    private Node baseNode;
    /** The scene graph nodes for the buttons. */
    private Node[] buttonNodes;
    /** The entity for the base. */
    private Entity baseEntity;
    /** The mouse event listener. */
    private EventListener listener;
    /** The JME collision system. */
    JMECollisionSystem collisionSystem;
    /** The collision component for the topmost node. */
    CollisionComponent cc;

    /**
     * Create a new instance of ButtonBox. The width of the base depends on the width of the buttons,
     * the number of buttons and the inter-button spacing.
     * @param baseHeight The height of the base.
     * @param baseDepth The depth of the base.
     * @param numButtons The number of buttons.
     * @param buttonWidth The width of the buttons. Each button has the same width.
     * @param buttonHeight The height of the buttons. Each button has the same height.
     * @param buttonDepth The depth of the buttons. Each button has the same depth.
     * Each button will stick out depth/2 in front of the base.
     * @param buttonSpacing The space between buttons.
     */
    public ButtonBox(float baseHeight, float baseDepth, int numButtons, float buttonWidth,
            float buttonHeight, float buttonDepth, float buttonSpacing) {

        if (numButtons <= 0) {
            throw new RuntimeException("Invalid number of buttons");
        }

        this.baseHeight = baseHeight;
        this.baseDepth = baseDepth;
        this.numButtons = numButtons;
        this.buttonWidth = buttonWidth;
        this.buttonHeight = buttonHeight;
        this.buttonDepth = buttonDepth;
        this.buttonSpacing = buttonSpacing;

        // Create all of the necessary objects and assemble them together
        createGeometries();
        createBaseNode();
        createCollisionComponent();
        createButtonNodes();
        baseEntity = createEntity("Base Entity", baseNode);
    }

    /**
     * Create the geometry objects. Create a box for the base and a box for each button.
     */
    private void createGeometries() {

        // Create the base geometry. 
	// 
	// Note: the JME documentation for Box(xExtent, yExtent, zExtent) is a big vague
	// on what "extent" means. Extent is half of the desired dimension.
        float baseWidth = numButtons * buttonWidth + (numButtons+1) * buttonSpacing;
        baseBox = new Box("Base Box", new Vector3f(0f, 0f, 0f), 
			  baseWidth/2f, baseHeight/2f, baseDepth/2f);
        baseBox.setModelBound(new BoundingBox());
        baseBox.updateModelBound();

        // Create the button geometries and ensure their bounds are initialized
        buttonBoxes = new Box[numButtons];
        float buttonX = -baseWidth / 2f + buttonSpacing + buttonWidth / 2f;
        float buttonY = 0;
        float buttonZ = baseDepth / 2f + buttonDepth / 2f;
        for (int i = 0; i < numButtons; i++) {
            buttonBoxes[i] = new Box("Button Box " + i, new Vector3f(buttonX, buttonY, buttonZ),
                    buttonWidth/2f, buttonHeight/2f, buttonDepth/2f);
            buttonBoxes[i].setModelBound(new BoundingBox());
            buttonBoxes[i].updateModelBound();
            buttonX += buttonWidth + buttonSpacing;
        }
    }

    /**
     * Create the base scene graph node.
     */
    private void createBaseNode() {
        baseNode = new Node("Base Node");
        baseNode.attachChild(baseBox);
        setBoxColor(baseNode, baseBox, baseColor);
    }

    /** 
     * Creates the collision component for the topmost scene graph node.
     */
    private void createCollisionComponent () {
        collisionSystem = (JMECollisionSystem) ClientContextJME.getWorldManager().getCollisionManager().
            loadCollisionSystem(JMECollisionSystem.class);
        cc = collisionSystem.createCollisionComponent(baseNode);
    }

    /**
     * Create the button nodes and attach them to the base node.
     */
    private void createButtonNodes() {

        // Create a scene graph node for each button. And, because these nodes are not
        // directly connected to the entity, we need to explicitly make them pickable.
        // by adding them to the collision component of the topmost node.
        buttonNodes = new Node[numButtons];
        for (int i = 0; i < numButtons; i++) {
            buttonNodes[i] = new Node("Button Node " + i);
            collisionSystem.addReportingNode(buttonNodes[i], cc);
            buttonNodes[i].attachChild(buttonBoxes[i]);
            baseNode.attachChild(buttonNodes[i]);
            setBoxColor(buttonNodes[i], buttonBoxes[i], baseColor);
        }
    }

    /**
     * Create a pickable, renderable entity which renders the given scene graph.
     * Such an entity has both a render component and a collision component.
     * @param node The top node of the entity's scene graph.
     * @return The new entity.
     */
    private Entity createEntity(String name, Node node) {

        // Create the entity
        Entity entity = new Entity(name);

        // Make the entity renderable by attaching a render component which refers
        // to the given scene graph.
        RenderComponent rc = ClientContextJME.getWorldManager().getRenderManager().
                createRenderComponent(node);
        entity.addComponent(RenderComponent.class, rc);

        // Make the entity pickable by attaching a collision component.
        entity.addComponent(CollisionComponent.class, cc);

        return entity;
    }

    /**
     * Attach this button box as a subentity of the given parent entity.
     * @param parentEntity The parent entity
     */
    public void attachToEntity(Entity parentEntity) {
        BasicRenderer.entityAddChild(parentEntity, baseEntity);
    }

    /**
     * Set the color of the base.
     * @param color The new color of the base.
     */
    public void setBaseColor(ColorRGBA color) {
        baseColor = color;
        setBoxColor(baseNode, baseBox, color);
    }

    /**
     * Set the color of the specified button.
     * @param buttonIndex The button whose color is to be changed.
     * @param color The new button color.
     * @throws java.lang.IllegalArgumentException if buttonIndex is too large or too small.
     */
    public void setButtonColor(int buttonIndex, ColorRGBA color) throws IllegalArgumentException {
        if (buttonIndex < 0 || buttonIndex >= numButtons) {
            throw new IllegalArgumentException("Invalid button index");
        }
        if (buttonColors == null) {
            buttonColors = new ColorRGBA[numButtons];
        }
        buttonColors[buttonIndex] = color;
        setBoxColor(buttonNodes[buttonIndex], buttonBoxes[buttonIndex], color);
    }

    /**
     * Set the color of the given box.
     */
    private static void setBoxColor(final Node node, final Box box, final ColorRGBA color) {

        ClientContextJME.getWorldManager().addRenderUpdater(new RenderUpdater() {
            public void update(Object arg0) {
                MaterialState ms = (MaterialState) box.getRenderState(RenderState.RS_MATERIAL);
                if (ms == null) {
                    ms = DisplaySystem.getDisplaySystem().getRenderer().createMaterialState();
                    box.setRenderState(ms);
                }
                ms.setAmbient(new ColorRGBA(color));
                ms.setDiffuse(new ColorRGBA(color));
                if (node != null) {
                    ClientContextJME.getWorldManager().addToUpdateList(node);
                }
            }
        }, null);
    }

    /**
     * Attach the event listener to the button box. This will allow the components of the box
     * to be mouse input sensitive when they are visible, that is, the listener will receive 
     * mouse events.
     * @param listener The listener for mouse events on the box.
     */
    public void addEventListener (EventListener listener) {
        this.listener = listener;
        listener.addToEntity(baseEntity);
    }

    /**
     * Detaches the mouse event listener from the button box. The button box will no longer 
     * be input sensitive.
     */
    public void removeEventListener () {
        if (listener != null) {
            listener.removeFromEntity(baseEntity);
            listener = null;
        }
     }
}


