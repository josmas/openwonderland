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
package org.jdesktop.wonderland.modules.appbase.client.view;

import com.jme.math.Matrix4f;
import com.jme.math.Vector3f;
import java.awt.Point;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.util.logging.Logger;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.wonderland.client.input.Event;
import org.jdesktop.wonderland.client.input.EventClassListener;
import org.jdesktop.wonderland.client.jme.input.InputPicker3D;
import org.jdesktop.wonderland.client.jme.input.MouseButtonEvent3D;
import org.jdesktop.wonderland.client.jme.input.MouseDraggedEvent3D;
import org.jdesktop.wonderland.client.jme.input.MouseEnterExitEvent3D;
import org.jdesktop.wonderland.client.jme.input.MouseEvent3D;
import org.jdesktop.wonderland.common.InternalAPI;
import org.jdesktop.wonderland.modules.appbase.client.ControlArb;

/**
 * Generic View2D event handler.
 *
 * @author deronj
 */
@InternalAPI
public class Gui2D {

    private static final Logger logger = Logger.getLogger(Gui2D.class.getName());

    /** The type of actions that user events can generate */
    public enum ActionType {

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
    public static class Action {

        /** The type of the action */
        public ActionType type;

        /**
         * Create a new instance of Action.
         *
         * @param type The type of the action.
         */
        public Action(ActionType type) {
            this.type = type;
        }
    }

    /** The possible view configuration GUI states */
    protected enum ConfigState {

        /** No user interaction is underway */
        IDLE,
        /** The user has started a mouse drag */
        DRAG_ACTIVE,
        /** The user has actually dragged the mouse */
        DRAGGING
    };

    /** The possible view configuration drag types */
    protected enum ConfigDragType {

        /** The user is dragging the mouse to move the view within its current plane */
        MOVING_PLANAR,
        /** The user is dragging the mouse to move the view in the Z direction to its current plane */
        MOVING_Z,
        /** The user is dragging the mouse to rotate the view around its Y axis */
            // TODO        ROTATING_Y
    };
    /** The view configuration GUI state */
    protected ConfigState configState = ConfigState.IDLE;
    /** The view configuration drag type (only valid when configState != IDLE */
    protected ConfigDragType configDragType;
    /** The intersection point on the entity over which the button was pressed, in world coordinates. */
    private Vector3f dragStartWorld;
    /** The screen coordinates of the button press event. */
    private Point dragStartScreen;
    /** The amount that the cursor has been dragged in eye coordinates. */
    protected Vector3f dragVectorEye;

    /** A listener for 3D mouse events */
    protected EventClassListener mouseListener;

    /** This Gui's view */
    protected View2DEntity view;

    /**
     * Create a new instance of Gui2D.
     *
     * @param view The view associated with the component that uses this Gui.
     */
    public Gui2D(View2DEntity view) {
        this.view = view;
    }

    /**
     * {@inheritDoc}
     */
    public void cleanup() {
        mouseListener = null;
        view = null;
    }

    /**
     * Attach this GUI controller's event listeners to the given entity.
     */
    public void attachEventListeners(Entity entity) {
        attachMouseListener(entity);
        attachKeyListener(entity);
    }

    /**
     * Detach this GUI controller's event listeners from the entity to which it is attached.
     */
    public void detachEventListeners(Entity entity) {
        detachMouseListener(entity);
        detachKeyListener(entity);
    }

    /**
     * Start listening to mouse events from this entity.
     */
    protected void attachMouseListener(Entity entity) {
        mouseListener = new MouseListener();
        mouseListener.addToEntity(entity);
    }

    /**
     * Stop listening to mouse events from this entity.
     */
    protected void detachMouseListener(Entity entity) {
        if (mouseListener != null && entity != null) {
            mouseListener.removeFromEntity(entity);
        }
    }

    /**
     * Start listening to key events from this entity.
     */
    protected void attachKeyListener(Entity entity) {
    }

    /**
     * Stop listening to keyboard events from this entity.
     */
    protected void detachKeyListener(Entity entity) {
    }

    /** A basic listener for 3D mouse events */
    protected class MouseListener extends EventClassListener {

        /**
         * {@inheritDoc}
         */
        @Override
        public Class[] eventClassesToConsume() {
            return new Class[]{MouseEvent3D.class};
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void commitEvent(Event event) {
            view.deliverEvent(view.getWindow(), (MouseEvent3D) event);
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
    protected Action determineIfMiscAction(MouseEvent me, MouseEvent3D me3d) {

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
            if (me3d instanceof MouseEnterExitEvent3D) {
                return null;
            }

            return new Action(ActionType.TOGGLE_CONTROL);
        }

        return null;
    }

    /**
     * Is this the event which takes or releases control of an app group (which for this LAF is Shift-Left-click)?
     */
    protected boolean isChangeControlEvent(MouseEvent me) {
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
    protected void performMiscAction(Action action, MouseEvent me, MouseEvent3D me3d) {
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
                ControlArb controlArb = view.getWindow().getApp().getControlArb();
                if (controlArb.hasControl()) {
                    logger.info("Release control");
                    controlArb.releaseControl();
                } else {
                    logger.info("Take control");
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
    protected Action determineIfConfigAction(MouseEvent me, MouseEvent3D me3d) {
        Action action = null;

        switch (me.getID()) {

        case MouseEvent.MOUSE_PRESSED:
            MouseButtonEvent3D buttonEvent = (MouseButtonEvent3D) me3d;
            if (configState == ConfigState.IDLE && me.getButton() == MouseEvent.BUTTON1) {

                configState = ConfigState.DRAG_ACTIVE;
                action = new Action(ActionType.DRAG_START);
                dragStartScreen = new Point(me.getX(), me.getY());
                dragStartWorld = buttonEvent.getIntersectionPointWorld();

                if ((me.getModifiersEx() & InputEvent.SHIFT_DOWN_MASK) != 0) {
                    if ((me.getModifiersEx() & InputEvent.CTRL_DOWN_MASK) != 0) {
                        // TODO: temp: shift+control left press means rotate y
                        //TODO: configDragType = ConfigDragType.ROTATING_Y;
                    } else {
                        // TODO: temp: shift left press means move z
                        configDragType = ConfigDragType.MOVING_Z;
                    }
                } else {
                    // Unmodified left press means move planar
                    configDragType = ConfigDragType.MOVING_PLANAR;
                }
            }
            return action;

        case MouseEvent.MOUSE_DRAGGED:
            if (configState == ConfigState.DRAG_ACTIVE ||
                configState == ConfigState.DRAGGING) {
                action = new Action(ActionType.DRAG_UPDATE);
                configState = ConfigState.DRAGGING;

                MouseDraggedEvent3D dragEvent = (MouseDraggedEvent3D) me3d;
                Vector3f dragVectorWorld = dragEvent.getDragVectorWorld(dragStartWorld, dragStartScreen,
                                                                        new Vector3f());

                // Convert world to eye coordinates
                /* TODO: notyet
                Matrix4f camInverse = InputPicker3D.getInputPicker().getCameraModelViewMatrixInverse(null);
                dragVectorEye = new Vector3f();
                camInverse.mult(dragVectorWorld, dragVectorEye);
                */
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
                    view.userMovePlanarFinish();
                }

                configState = ConfigState.IDLE;
                // Note: the coordinates for LG mouse release events are invalid.
                // So we just use the coordinates from the last drag or press.
            }
            return action;
        }

        return null;
    }

    /** 
     * Perform a view configuration action. That is, one of:
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
    protected void performConfigAction(Action action, MouseEvent me, MouseEvent3D me3d) {
        return;

        /*
        switch (action.type) {

        case DRAG_START:
            switch (configDragType) {
            case MOVING_PLANAR:
                view.userMovePlanarStart(dragVectorEye.x, dragVectorEye.y);
                break;
            case MOVING_Z:
                view.userMoveZStart(dragVectorEye.y);
                break;
            case ROTATING_Y:
                view.userRotateYStart(dragVectorEye.y);
                break;
            }
            break;

        case DRAG_UPDATE:
            switch (configDragType) {
            case MOVING_PLANAR:
                view.userMovePlanarUpdate(dragVectorEye.x, dragVectorEye.y);
                break;
            case MOVING_Z:
                view.userMoveZUpdate(dragVectorEye.y);
                break;
            case ROTATING_Y:
                view.userRotateYUpdate(dragVectorEye.y);
                break;
            }
            break;

        case DRAG_FINISH:
            switch (configDragType) {
            case MOVING_PLANAR:
                view.userMovePlanarFinish();
                break;
            case MOVING_Z:
                view.userMoveZFinish();
                break;
            case ROTATING_Y:
                view.userRotateYFinish();
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
    protected Action determineIfToFrontAction(MouseEvent me) {
        if (me.getID() == MouseEvent.MOUSE_CLICKED &&
                me.getButton() == MouseEvent.BUTTON1 &&
                me.getModifiersEx() == 0) {
            return new Action(ActionType.TO_FRONT);
        } else {
            return null;
        }
    }
}
