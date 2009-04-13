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
package org.jdesktop.wonderland.modules.appbase.client.cell.view.viewdefault;

import java.awt.event.MouseEvent;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.wonderland.client.input.Event;
import org.jdesktop.wonderland.client.jme.input.MouseEnterExitEvent3D;
import org.jdesktop.wonderland.client.jme.input.MouseEvent3D;
import org.jdesktop.wonderland.common.ExperimentalAPI;

/**
 * The GUI code for the frame resize corner.
 *
 * @author deronj
 */
@ExperimentalAPI
class Gui2DResizeCorner extends Gui2DSide {

    /** The associated resize corner component */
    protected FrameResizeCorner resizeCorner;

    /** 
     * Create a new instance of Gui2DResizeCorner.
     *
     * @param view The view associated with the component that uses this Gui.
     */
    public Gui2DResizeCorner(View2DCell view) {
        super(view);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void cleanup() {
        super.cleanup();
        resizeCorner = null;
    }

    /**
     * Specify the resize corner component for which this Gui provides behavior.
     *
     * @param resizeCorner The resize corner component.
     */
    public void setComponent(FrameResizeCorner resizeCorner) {
        this.resizeCorner = resizeCorner;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void attachMouseListener(Entity entity) {
        mouseListener = new ResizeCornerMouseListener();
        mouseListener.addToEntity(entity);
    }

    /**
     * The mouse listener for this GUI.
     */
    protected class ResizeCornerMouseListener extends Gui2DSide.SideMouseListener {

        /**
         * Called when a 3D event has occurred.
         */
        @Override
        public void commitEvent(Event event) {
            Action action;

            MouseEvent3D me3d = (MouseEvent3D) event;

            if (me3d instanceof MouseEnterExitEvent3D &&
                view.getWindow().getApp().getControlArb().hasControl()) {
                resizeCorner.setMouseInside(((MouseEnterExitEvent3D) me3d).isEnter());
            }

            super.commitEvent(event);
        }
    }

    /**
     * Determine if this is a window configuration action.
     * are only recognized when the user has control of the window.
     *
     * @param me The AWT event for this 3D mouse event.
     * @param me3d The 3D mouse event.
     */
    @Override
    protected Action determineIfConfigAction(MouseEvent me, MouseEvent3D me3d) {
        Action action = determineIfToFrontAction(me);
        if (action != null) {
            return action;
        }

        return super.determineIfConfigAction(me, me3d);
    }

    /**
     * Perform the window configuration action.
     *
     * @param action The configuration action the given event provokes.
     * @param me The AWT event for this 3D mouse event.
     * @param me3d The 3D mouse event.
     */
    @Override
    protected void performConfigAction(Action action, MouseEvent me, MouseEvent3D me3d) {
        if (action.type == ActionType.TO_FRONT) {
            view.getWindow().restackToTop();
            return;
        }

        super.performConfigAction(action, me, me3d);
    }
}
