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
package org.jdesktop.wonderland.modules.testcells.client.cell;

import java.awt.Point;
import java.awt.event.MouseEvent;
import com.jme.math.Vector3f;
import com.jme.scene.Node;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.wonderland.client.cell.*;
import org.jdesktop.wonderland.client.input.Event;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.client.input.EventClassListener;
import org.jdesktop.wonderland.client.jme.input.MouseButtonEvent3D;
import org.jdesktop.wonderland.client.jme.input.MouseDraggedEvent3D;
import org.jdesktop.wonderland.client.jme.input.MouseEvent3D;
import org.jdesktop.wonderland.common.cell.CellStatus;
import org.jdesktop.wonderland.common.cell.CellTransform;
import org.jdesktop.wonderland.modules.testcells.client.jme.cellrenderer.DisappearTestRenderer;

/**
 * Test for object removal from the scene graph. Click on the small cube to make it disappear.
 * Click on the large cube to make both disappear (the small cube is a child of the large cube).
 * You can also drag the cubes around by left button dragging either cube (similar to DragTest).
 *
 * Note: the entities disappear, but the cell does not.
 * 
 * @author deronj
 */
public class DisappearTest extends SimpleShapeCell {
    
    MyDragListener dragListener = new MyDragListener();
    MyDisappearListener disappearListener = new MyDisappearListener();
    private MovableComponent movableComp;
    private DisappearTestRenderer cellRenderer;

    static Node sceneRoot;
    Entity smallCubeEntity;

    public DisappearTest(CellID cellID, CellCache cellCache) {
        super(cellID, cellCache);
    }
    
    @Override
    protected CellRenderer createCellRenderer(RendererType rendererType) {
        switch(rendererType) {
	case RENDERER_2D :
	    // No 2D Renderer yet
	    return null;
	case RENDERER_JME :
	    cellRenderer = new DisappearTestRenderer(this);
	    break;                
        }

        return cellRenderer;
    }
    
    @Override
    public void setStatus (CellStatus status, boolean increasing) {
	super.setStatus(status, increasing);

	switch(status) {

	case ACTIVE:
        if (increasing) {
            movableComp = getComponent(MovableComponent.class);
            dragListener.addToEntity(cellRenderer.getEntity());
            disappearListener.addToEntity(cellRenderer.getEntity());
            disappearListener.addToEntity(cellRenderer.getSecondaryEntity());
        }
	    break;

        case DISK:
            if (!increasing) {
                dragListener.removeFromEntity(cellRenderer.getEntity());
                disappearListener.removeFromEntity(cellRenderer.getEntity());
                disappearListener.removeFromEntity(cellRenderer.getSecondaryEntity());
            }
	}

    }

    private class MyDragListener extends EventClassListener {

	// TODO: workaround for bug 27
	boolean dragging;

	// The intersection point on the entity over which the button was pressed, in world coordinates.
	Vector3f dragStartWorld;

	// The screen coordinates of the button press event.
	Point dragStartScreen;

	Vector3f translationOnPress = null;

	public Class[] eventClassesToConsume () {
	    return new Class[] { MouseEvent3D.class };
	}

	public void commitEvent (Event event) {
	    CellTransform transform = getLocalTransform();

	    if (event instanceof MouseButtonEvent3D) {
		MouseButtonEvent3D buttonEvent = (MouseButtonEvent3D) event;
		if (buttonEvent.isPressed() && buttonEvent.getButton() == MouseButtonEvent3D.ButtonId.BUTTON1) {
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

	    // Now add the drag vector the node translation and move the cell.
	    Vector3f newTranslation = translationOnPress.add(dragVector);
	    transform.setTranslation(newTranslation);
	    movableComp.localMoveRequest(transform);
	}
    }

    private class MyDisappearListener extends EventClassListener {

	public Class[] eventClassesToConsume () {
	    return new Class[] { MouseEvent3D.class };
	}

	public boolean propagatesToParent (Event event) {
	    return false;
	}

	public void commitEvent (Event event) {
	    if (event instanceof MouseButtonEvent3D) {
		MouseButtonEvent3D buttonEvent = (MouseButtonEvent3D) event;
		if (buttonEvent.isClicked() && buttonEvent.getButton() == MouseButtonEvent3D.ButtonId.BUTTON1) {
 		    if (event.getEntity().equals(cellRenderer.getSecondaryEntity())) {
			cellRenderer.disconnectSecondaryObject();
		    } else {
			cellRenderer.disconnectPrimaryObject();
		    }
		}
		return;
	    } 
	}
    }
}
