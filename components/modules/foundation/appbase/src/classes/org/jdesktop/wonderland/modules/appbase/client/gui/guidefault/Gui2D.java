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
package org.jdesktop.wonderland.modules.appbase.client.gui.guidefault;

import com.jme.math.Vector3f;
import com.jme.scene.Node;
import java.awt.event.MouseEvent;
import java.util.logging.Logger;
import org.jdesktop.mtgame.CollisionComponent;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.mtgame.JMECollisionSystem;
import org.jdesktop.mtgame.RenderComponent;
import org.jdesktop.wonderland.client.input.Event;
import org.jdesktop.wonderland.client.input.EventClassListener;
import org.jdesktop.wonderland.client.jme.ClientContextJME;
import org.jdesktop.wonderland.client.jme.input.MouseEnterExitEvent3D;
import org.jdesktop.wonderland.client.jme.input.MouseEvent3D;
import org.jdesktop.wonderland.common.ExperimentalAPI;
import org.jdesktop.wonderland.modules.appbase.client.Gui;
import org.jdesktop.wonderland.modules.appbase.client.Window2D;
import org.jdesktop.wonderland.modules.appbase.client.Window2DView;

/**
 * Generic app 2D window view event handler.
 *
 * @author deronj
 */

@ExperimentalAPI
public class Gui2D extends Gui { 

    private static final Logger logger = Logger.getLogger(Gui2D.class.getName());

    /** The type of actions that user events can generate */
    protected enum ActionType {
	TOGGLE_CONTROL,
	MOVE_CAMERA_TO_BEST_VIEW,
	MOVE_AVATAR_TO_BEST_VIEW,
	MOVE_WINDOW_TO_BEST_VIEW,
	CLOSE_BUTTON_ENTER,
	CLOSE_BUTTON_EXIT,
	CLOSE_BUTTON_PRESSED,
        DRAG_START, 
	DRAG_UPDATE, 
        DRAG_FINISH,
	TO_FRONT;
    };

    /** A basic action object */
    protected static class Action {

	/** The type of the action */
	protected ActionType type;

	/**
	 * Create a new instance of Action.
	 *
	 * @param type The type of the action.
	 */
	protected Action (ActionType type) {
	    this.type = type;
	}
    }

    /** The possible window configuration GUI states */
    protected enum ConfigState { 

	/** No user interaction is underway */
	IDLE, 

	/** The user has started a mouse drag */
	DRAG_ACTIVE, 

	/** The user has actually dragged the mouse */
	DRAGGING 
    };

    /** The possible window configuration drag types */
    protected enum ConfigDragType { 

	/** The user is dragging the mouse to move the window within its current plane */
	MOVING_PLANAR, 

	/** The user is dragging the mouse to move the window in the Z direction to its current plane */
	MOVING_Z, 

        /** The user is dragging the mouse to rotate the window around its Y axis */
	ROTATING_Y 
    };

    /** The window configuration GUI state */
    protected ConfigState configState = ConfigState.IDLE;

    /** The window configuration drag type (only valid when configState != IDLE */
    protected ConfigDragType configDragType;

    /** 
     * The current drag point (only valid when configState != IDLE.
     * Note: z is always 0.
     */
    protected Vector3f configDragPoint = new Vector3f();

    /** A listener for 3D mouse events */
    protected EventClassListener mouseListener;

    /**
     * Create a new instance of Gui2D.
     *
     * @param view The view associated with the component that uses this Gui.
     */
    public Gui2D (Window2DView view) {
	super(view);
    }

    /**
     * {@inheritDoc}
     */
    public void cleanup () {
	super.cleanup();
	mouseListener = null;
    }

    /**
     * Attach this GUI controller's event listeners to the given entity.
     */
    public void attachEventListeners (Entity entity) {
	attachCollisionComponent(entity);
	attachMouseListener(entity);
	attachKeyListener(entity);
    }

    /**
     * Detach this GUI controller's event listeners from the entity to which it is attached.
     */
    public void detachEventListeners (Entity entity) {
	detachCollisionComponent(entity);
	detachMouseListener(entity);
	detachKeyListener(entity);
    }

    /**
     * Start listening to mouse events from this entity.
     */
    protected void attachMouseListener (Entity entity) {
	MouseListener mouseListener = new MouseListener();
	mouseListener.addToEntity(entity);
    }

    /**
     * Stop listening to mouse events from this entity.
     */
    protected void detachMouseListener (Entity entity) {
	if (mouseListener != null && entity != null) {
	    mouseListener.removeFromEntity(entity);
	}
    }

    /**
     * Start listening to key events from this entity.
     */
    protected void attachKeyListener (Entity entity) {}

    /**
     * Stop listening to keyboard events from this entity.
     */
    protected void detachKeyListener (Entity entity) {}

    /**
     * Attach a collision component to the given entity to make it pickable.
     */
    protected void attachCollisionComponent (Entity entity) {
	JMECollisionSystem collisionSystem = (JMECollisionSystem)
	    ClientContextJME.getWorldManager().getCollisionManager().loadCollisionSystem(JMECollisionSystem.class);
	
	// Get the render component of this entity
	RenderComponent rc = (RenderComponent) entity.getComponent(RenderComponent.class);
	if (rc == null) {
	    logger.warning("Attempt to attach collision component to entity with no render component");
	    return;
	}

	// Get the root node of the render component
	Node rootNode = rc.getSceneRoot();
	if (rootNode == null) {
	    logger.warning("Attempt to attach collision component to entity with no root node");
	    return;
	}

	CollisionComponent cc = collisionSystem.createCollisionComponent(rootNode);
	entity.addComponent(CollisionComponent.class, cc);
    }

    /**
     * Detach an existing collision component from the given entity.
     */
    protected void detachCollisionComponent (Entity entity) {
	entity.removeComponent(CollisionComponent.class);
    }

    /** A basic listener for 3D mouse events */
    protected class MouseListener extends EventClassListener {

	/**
	 * {@inheritDoc}
	 */
	@Override
        public Class[] eventClassesToConsume () {
	    return new Class[] { MouseEvent3D.class };
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void commitEvent (Event event) {
	    ((Window2DView)view).deliverEvent((Window2D)window, (MouseEvent3D)event);
	}
    }
    
    /** 
     * Determine if this 3D mouse event provokes a miscellaneous action. That is, one of:
     * <br><br>
     *    + Move camera to best view
     * <br>
     *    + Move avatar to best view
     * <br>
     *    + Move window to best view
     * <br>
     *    + Change control 
     *
     * @param me The AWT event for this 3D mouse event.
     * @param me3d The 3D mouse event.
     */
    protected Action determineIfMiscAction  (MouseEvent me, MouseEvent3D me3d) {

	/* TODO
	// Is this move-camera-to-best-view?
	if (EventController.isMoveCameraToBestViewEvent(me)) {
	    return new Action(ActionType.MOVE_CAMERA_TO_BEST_VIEW);
	}

	// Is this move-avatar-to-best-view?
	if (EventController.isMoveAvatarToBestViewEvent(me)) {
	    return new Action(ActionType.MOVE_AVATAR_TO_BEST_VIEW);
	}

	// Is this move-window-to-best-view?
	if (EventController.isMoveWindowToBestViewEvent(me)) {
	    return new Action(ActionType.MOVE_WINDOW_TO_BEST_VIEW);
	}
	*/

	// Is this the Take Control or Release Control event?
	if (isChangeControlEvent(me)) {

	    // Ignore any enter/exit events that LG generates for the click event
	    if (me3d instanceof MouseEnterExitEvent3D) return null;

	    return new Action(ActionType.TOGGLE_CONTROL);
	}

	return null;
    }

    /**
     * Is this the event which takes or releases control of an app group (which for this LAF is Shift-Left-click)?
     */
    private boolean isChangeControlEvent (MouseEvent me) {
	return me.getID() == MouseEvent.MOUSE_CLICKED &&
	       me.getButton() == MouseEvent.BUTTON1 && 
    	       (me.getModifiersEx() & MouseEvent.SHIFT_DOWN_MASK) != 0;
    }

    /** 
     * Perform miscellaneous action. (Refer to determineIfMiscAction)
     *
     * @param action The miscellaneous action the given event provokes.
     * @param me The AWT event for this 3D mouse event.
     * @param me3d The 3D mouse event.
     */
    protected void performMiscAction (Action action, MouseEvent me, MouseEvent3D me3d) {
	//logger.severe("Gui misc action = " + action.type);

	switch (action.type) {
	    
        /* TODO
	case MOVE_CAMERA_TO_BEST_VIEW:
	    logger.severe("AW: " + action.type);
	    // TODO window.moveToBestView(AppWindowImage.MoveMode.CAMERA);
	    break;

	case MOVE_AVATAR_TO_BEST_VIEW:
	    logger.severe("AW: " + action.type);
	    // TODO window.moveToBestView(AppWindowImage.MoveMode.AVATAR);
	    break;

	case MOVE_WINDOW_TO_BEST_VIEW:
	    logger.severe("AW: " + action.type);
	    //TODO window.moveToBestView(AppWindowImage.MoveMode.WINDOW);
	    break;
	*/

	case TOGGLE_CONTROL: 
	    if (controlArb.hasControl()) {
		controlArb.releaseControl();
	    } else {
		controlArb.takeControl();
	    }
	    break;
	}
    }

    /** 
     * Determine if this is a window configuration action. That is, one of:
     * <br><br>
     *     + Planar move (move within the cell local z=0 plane).
     * <br>
     *     + Z move (move along the cell local z axis).
     * <br>
     *     + Y rotation.
     *
     * @param me The AWT event for this 3D mouse event.
     * @param me3d The 3D mouse event.
     */
    protected Action determineIfConfigAction (MouseEvent me, MouseEvent3D me3d) {
	Action action = null;

	/* TODO: window config
	switch (me.getID()) {

	case MouseEvent.MOUSE_PRESSED:
	    if (configState == ConfigState.IDLE && me.getButton() == MouseEvent.BUTTON1) {
	    // TODO: augmented modifiers to longer exist. need alternative GUI
		if ((me3d.getAugmentedModifiers() & InputEvent3D.AUGMENTED_MODIFIERS_F) != 0) {
		    action = new Action(ActionType.DRAG_START);
		    configDragType = ConfigDragType.MOVING_Z;
		} else if ((me3d.getAugmentedModifiers() & InputEvent3D.AUGMENTED_MODIFIERS_R) != 0) {
		    action = new Action(ActionType.DRAG_START);
		    configDragType = ConfigDragType.ROTATING_Y;
		} else {
		    action = new Action(ActionType.DRAG_START);
		    configDragType = ConfigDragType.MOVING_PLANAR;
		}
		configState = ConfigState.DRAG_ACTIVE;
		calcWorldDragPointFromImagePlate(me);
	    }
	    return action;

	case MouseEvent.MOUSE_DRAGGED:
	    if (configState == ConfigState.DRAG_ACTIVE ||
		configState == ConfigState.DRAGGING) {
		action = new Action(ActionType.DRAG_UPDATE);
		configState = ConfigState.DRAGGING;
		calcWorldDragPointFromImagePlate(me);
	    }
	    return action;

	case MouseEvent.MOUSE_RELEASED:
	    if (me.getButton() == MouseEvent.BUTTON1) {
		if (configState == ConfigState.DRAGGING) {
		    // Note: the misc action ToggleControl may produce an mouse 
		    // press/release without a drag in between so only perform 
		    // a dragfinish if an actual drag occurred between the
		    // mouse press and release
		    action = new Action(ActionType.DRAG_FINISH);

		} else if (configDragType == ConfigDragType.MOVING_PLANAR &&
			   me.getModifiersEx() == 0) {

		    // Note: A bit of uncleanliness: Even though we haven't been
		    // actually dragging we still need to restore the cursor to
		    // its original form.
		    window.userMovePlanarFinish();
		}

		configState = ConfigState.IDLE;
		// Note: the coordinates for LG mouse release events are invalid.
		// So we just use the coordinates from the last drag or press.
	    }
	    return action;
	}
	*/

	return null;
    }

    /** 
     * Perform a window configuration action. That is, one of:
     * <br><br>
     *     + Planar move (move within the cell local z=0 plane).
     * <br>
     *     + Z move (move along the cell local z axis).
     * <br>
     *     + Y rotation.
     *
     * @param action The configuration action the given event provokes.
     * @param me The AWT event for this 3D mouse event.
     * @param me3d The 3D mouse event.
     */
    protected void performConfigAction (Action action, MouseEvent me, MouseEvent3D me3d) {
	/* TODO: window config
	switch (action.type) {

	case DRAG_START:
	    switch (configDragType) {
	    case MOVING_PLANAR:
		window.userMovePlanarStart(configDragPoint.x, configDragPoint.y);
		break;
	    case MOVING_Z:
		window.userMoveZStart(configDragPoint.y);
		break;
	    case ROTATING_Y:
		window.userRotateYStart(configDragPoint.y);
		break;
	    }
	    break;

	case DRAG_UPDATE: 
	    switch (configDragType) {
	    case MOVING_PLANAR:
		window.userMovePlanarUpdate(configDragPoint.x, configDragPoint.y);
		break;
	    case MOVING_Z:
		window.userMoveZUpdate(configDragPoint.y);
		break;
	    case ROTATING_Y:
		window.userRotateYUpdate(configDragPoint.y);
		break;
	    }
	    break;

	case DRAG_FINISH:
	    switch (configDragType) {
	    case MOVING_PLANAR:
		window.userMovePlanarFinish();
		break;
	    case MOVING_Z:
		window.userMoveZFinish();
		break;
	    case ROTATING_Y:
		window.userRotateYFinish();
		break;
	    }
	    break;

	default:
	    throw new RuntimeException("Unrecognized action");
	}
	*/
    }

    /**
     * Determine if this is an action which moves the window to the front of the stack.
     *
     * @param me The AWT event corresponding to a 3D mouse event which has been received.
     */
    protected Action determineIfToFrontAction (MouseEvent me) {
	if (me.getID() == MouseEvent.MOUSE_CLICKED &&
	    me.getButton() == MouseEvent.BUTTON1 &&
	    me.getModifiersEx() == 0) {
	    return new Action(ActionType.TO_FRONT);
	} else {
	    return null;
	}
    }
}
