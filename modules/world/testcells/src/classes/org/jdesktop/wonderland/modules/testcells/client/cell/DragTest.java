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
package org.jdesktop.wonderland.modules.testcells.client.cell;

import com.jme.math.Vector3f;
import com.jme.scene.Node;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.mtgame.RenderComponent;
import org.jdesktop.wonderland.client.cell.*;
import org.jdesktop.wonderland.client.input.Event;
import org.jdesktop.wonderland.client.jme.cellrenderer.CellRendererJME;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.client.input.EventClassListener;
import org.jdesktop.wonderland.client.jme.input.MouseButtonEvent3D;
import org.jdesktop.wonderland.client.jme.input.MouseDraggedEvent3D;
import org.jdesktop.wonderland.client.jme.input.MouseEvent3D;
import org.jdesktop.wonderland.common.cell.CellTransform;

/**
 * Test for MouseDraggedEvent3D events. Click on the object and drag it left or right.
 * The object (and its containing cell) should move as you drag it.
 * 
 * @author deronj
 */
public class DragTest extends SimpleShapeCell {
    
    private MovableComponent movableComp;
    
    public DragTest(CellID cellID, CellCache cellCache) {
        super(cellID, cellCache);
        addComponent(new ChannelComponent(this));
        addComponent(new MovableComponent(this));
        movableComp = getComponent(MovableComponent.class);
    }
    
    @Override
    protected CellRenderer createCellRenderer(RendererType rendererType) {
        CellRenderer ret = super.createCellRenderer(rendererType);

	MyDragListener dragListener = new MyDragListener();
        Entity entity = ((CellRendererJME)ret).getEntity();
	dragListener.addToEntity(entity);

        return ret;
    }
    
    
    static Node getSceneRoot (Entity entity) {
        RenderComponent renderComp = (RenderComponent) entity.getComponent(RenderComponent.class);
        if (renderComp == null) {
            return null;
        }
        Node node = renderComp.getSceneRoot();
        return node;
    }

    private class MyDragListener extends EventClassListener {

	// TODO: workaround for bug 27
	boolean dragging;

	Vector3f dragStartWorld;

	Vector3f translationOnPress = null;

	public Class[] eventClassesToConsume () {
	    return new Class[] { MouseEvent3D.class };
	}

	public void commitEvent (Event event) {

	    CellTransform transform = getLocalTransform();

	    if (event instanceof MouseButtonEvent3D) {
		MouseButtonEvent3D buttonEvent = (MouseButtonEvent3D) event;
		if (buttonEvent.isPressed()) {
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
	    Vector3f dragVector = dragEvent.getDragVectorWorld(dragStartWorld, new Vector3f());

	    // Now add the drag vector the node translation and move the cell.
	    Vector3f newTranslation = translationOnPress.add(dragVector);
	    transform.setTranslation(newTranslation);
	    movableComp.localMoveRequest(transform);
	}
    }
}
