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

package org.jdesktop.wonderland.client.jme.affordances;

import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import com.jme.renderer.ColorRGBA;
import com.jme.scene.Node;
import com.jme.scene.shape.Arrow;
import com.jme.scene.state.MaterialState;
import com.jme.scene.state.RenderState;
import com.jme.scene.state.ZBufferState;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.util.logging.Logger;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.mtgame.RenderComponent;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.cell.Cell.RendererType;
import org.jdesktop.wonderland.client.cell.MovableComponent;
import org.jdesktop.wonderland.client.input.Event;
import org.jdesktop.wonderland.client.input.EventClassListener;
import org.jdesktop.wonderland.client.jme.ClientContextJME;
import org.jdesktop.wonderland.client.jme.cellrenderer.BasicRenderer;
import org.jdesktop.wonderland.client.jme.cellrenderer.CellRendererJME;
import org.jdesktop.wonderland.client.jme.input.MouseButtonEvent3D;
import org.jdesktop.wonderland.client.jme.input.MouseDraggedEvent3D;
import org.jdesktop.wonderland.client.jme.input.MouseEvent3D;
import org.jdesktop.wonderland.common.cell.CellTransform;

/**
 *
 * @author Jordan Slott <jslott@dev.java.net>
 */
public class TranslateAffordance extends Affordance {
    private Node rootNode;
    private MovableComponent movableComp;
    private static ZBufferState zbuf = null;

    
    static {
        zbuf = (ZBufferState) ClientContextJME.getWorldManager().getRenderManager().createRendererState(RenderState.RS_ZBUFFER);
        zbuf.setEnabled(true);
        zbuf.setFunction(ZBufferState.TestFunction.LessThanOrEqualTo);
    }
    
    private TranslateAffordance(Cell cell) {
        super("Translate", cell);
        
        rootNode = new Node();
        movableComp = cell.getComponent(MovableComponent.class);

        RenderComponent rc = ClientContextJME.getWorldManager().getRenderManager().createRenderComponent(rootNode);
        this.addComponent(RenderComponent.class, rc);
        
        /* Arrow in the +z direction, color blue */
        Entity e1 = new Entity("Arrow Z");
        Arrow a1 = new Arrow("Arrow Z", 1.5f, 0.05f);
        Node n1 = new Node();
        a1.setSolidColor(ColorRGBA.blue);
        MaterialState matState1 = (MaterialState) ClientContextJME.getWorldManager().getRenderManager().createRendererState(RenderState.RS_MATERIAL);
        matState1.setDiffuse(ColorRGBA.blue);
        n1.setRenderState(matState1);
        n1.setRenderState(zbuf);
        Quaternion q = new Quaternion().fromAngleAxis(1.5707f, new Vector3f(1, 0, 0));
        n1.setLocalRotation(q);
        n1.setLocalTranslation(new Vector3f(0, 0, 0.75f));
        n1.attachChild(a1);
        RenderComponent rc1 = ClientContextJME.getWorldManager().getRenderManager().createRenderComponent(n1);
        e1.addComponent(RenderComponent.class, rc1);
        BasicRenderer.entityAddChild(this, e1);

        /* Arrow in the +y direction, color green */
        Entity e2 = new Entity("Arrow Y");
        Arrow a2 = new Arrow("Arrow Y", 1.5f, 0.05f);
        Node n2 = new Node();
        a2.setSolidColor(ColorRGBA.green);        
        MaterialState matState2 = (MaterialState) ClientContextJME.getWorldManager().getRenderManager().createRendererState(RenderState.RS_MATERIAL);
        matState2.setDiffuse(ColorRGBA.green);
        n2.setRenderState(matState2);
        n2.setRenderState(zbuf);
        n2.setLocalTranslation(new Vector3f(0, 0.75f, 0));
        n2.attachChild(a2);
        RenderComponent rc2 = ClientContextJME.getWorldManager().getRenderManager().createRenderComponent(n2);
        e2.addComponent(RenderComponent.class, rc2);
        BasicRenderer.entityAddChild(this, e2);
        
        /* Arrow in the +x direction, color red */
        Entity e3 = new Entity("Arrow X");
        Arrow a3 = new Arrow("Arrow X", 1.5f, 0.05f);
        Node n3 = new Node();
        a3.setSolidColor(ColorRGBA.red);
        MaterialState matState3 = (MaterialState) ClientContextJME.getWorldManager().getRenderManager().createRendererState(RenderState.RS_MATERIAL);
        matState3.setDiffuse(ColorRGBA.red);
        n3.setRenderState(matState3);
        n3.setRenderState(zbuf);
        Quaternion q3 = new Quaternion().fromAngleAxis(1.5707f, new Vector3f(0, 0, 1));
        n3.setLocalRotation(q3);
        n3.setLocalTranslation(new Vector3f(-0.75f, 0, 0));
        n3.attachChild(a3);
        RenderComponent rc3 = ClientContextJME.getWorldManager().getRenderManager().createRenderComponent(n3);
        e3.addComponent(RenderComponent.class, rc3);
        BasicRenderer.entityAddChild(this, e3);
        
        // Add collision to the Entities so they get events
        makeEntityPickable(this, rootNode);
        makeEntityPickable(e1, n1);
        makeEntityPickable(e2, n2);
        makeEntityPickable(e3, n3);
        
        // Add listeners to each of the three entities for each axis.
        new MyDragListener().addToEntity(e1);
        new MyDragListener().addToEntity(e2);
        new MyDragListener().addToEntity(e3);
    }
    
    protected Node getRootNode() {
        return rootNode;
    }
    
    public static TranslateAffordance addToCell(Cell cell) {
        TranslateAffordance translateAffordance = new TranslateAffordance(cell);
        CellRendererJME r = (CellRendererJME) cell.getCellRenderer(RendererType.RENDERER_JME);
        BasicRenderer.entityAddChild(r.getEntity(), translateAffordance);
        ClientContextJME.getWorldManager().addToUpdateList(translateAffordance.getRootNode());
        return translateAffordance;
    }
    
    private class MyDragListener extends EventClassListener {

	// TODO: workaround for bug 27
	boolean dragging;

	// The intersection point on the entity over which the button was pressed, in world coordinates.
	Vector3f dragStartWorld;

	// The screen coordinates of the button press event.
	Point dragStartScreen;

	Vector3f translationOnPress = null;

        @Override
	public Class[] eventClassesToConsume () {
	    return new Class[] { MouseEvent3D.class, MouseButtonEvent3D.class };
	}

        @Override
	public void commitEvent (Event event) {
            Logger logger = Logger.getLogger(TranslateAffordance.class.getName());
            logger.warning("COMMIT");
	    CellTransform transform = cell.getLocalTransform();
	    if (event instanceof MouseButtonEvent3D) {
		MouseButtonEvent3D buttonEvent = (MouseButtonEvent3D) event;
		if (buttonEvent.isPressed()) {
		    MouseEvent awtButtonEvent = (MouseEvent) buttonEvent.getAwtEvent();
		    dragStartScreen = new Point(awtButtonEvent.getX(), awtButtonEvent.getY());
		    dragStartWorld = buttonEvent.getIntersectionPointWorld();
		    translationOnPress = transform.getTranslation(null);
		    dragging = true;
		} else {
		    dragging = false;
		}
		return;
	    } 

	    if (!dragging || !(event instanceof MouseDraggedEvent3D)) {
		return;
	    }

	    MouseDraggedEvent3D dragEvent = (MouseDraggedEvent3D) event;
	    Vector3f dragVector = dragEvent.getDragVectorWorld(dragStartWorld, dragStartScreen, 
							       new Vector3f());
            
            // DO SOME TRANSLATION HERE!
            logger.warning("DRAG FOR ENTITY " + event.getEntity().getName());
	}
    }
}
