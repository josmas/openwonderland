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

import org.jdesktop.mtgame.Entity;
import com.jme.image.Image;
import com.jme.image.Texture;
import com.jme.math.Quaternion;
import com.jme.math.Vector2f;
import com.jme.math.Vector3f;
import com.jme.scene.Node;
import java.awt.Point;
import com.jme.scene.state.TextureState;
import java.awt.Button;
import java.awt.Dimension;
import java.awt.event.MouseEvent;
import java.util.LinkedList;
import java.util.logging.Logger;
import org.jdesktop.mtgame.CollisionComponent;
import org.jdesktop.mtgame.EntityComponent;
import org.jdesktop.mtgame.JMECollisionSystem;
import org.jdesktop.mtgame.RenderComponent;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.cell.MovableComponent;
import org.jdesktop.wonderland.client.input.EventListener;
import org.jdesktop.wonderland.client.jme.ClientContextJME;
import org.jdesktop.wonderland.client.jme.input.MouseDraggedEvent3D;
import org.jdesktop.wonderland.client.jme.input.MouseEvent3D;
import org.jdesktop.wonderland.client.jme.input.MouseWheelEvent3D;
import org.jdesktop.wonderland.common.ExperimentalAPI;
import org.jdesktop.wonderland.common.cell.CellTransform;
import org.jdesktop.wonderland.modules.appbase.client.App2D;
import org.jdesktop.wonderland.modules.appbase.client.ControlArb;
import org.jdesktop.wonderland.modules.appbase.client.Window2D;
import org.jdesktop.wonderland.modules.appbase.client.cell.App2DCell;
import org.jdesktop.wonderland.modules.appbase.client.view.GeometryNode;
import org.jdesktop.wonderland.modules.appbase.client.view.View2D;
import org.jdesktop.wonderland.modules.appbase.client.view.View2D.Type;
import org.jdesktop.wonderland.modules.appbase.client.view.View2DDisplayer;
import org.jdesktop.wonderland.modules.appbase.client.view.View2DEntity;

/**
 * TODO
 * Each view has entity -> viewNode -> geometryNode -> Geometry
 * @author dj
 */
@ExperimentalAPI
public class View2DCell extends View2DEntity {

    private static final Logger logger = Logger.getLogger(View2DCell.class.getName());

    /** The amount of space between views in the view stack.*/
    private static float STACK_GAP = 0.01f;

    /** The cell in which this view is displayed. */
    private App2DCell cell;

    /** The next delta rotation to apply. */
    private Quaternion deltaRotationToApply;

    /** The frame of a decorated view. */
    private Frame2DCell frame;

    /** Did we create our own movable component? */
    private boolean selfCreatedMovableComponent;

    /**
     * Create an instance of View2DCell with default geometry node.
     * @param cell The cell in which the view is displayed.
     * @param window The window displayed in this view.
     */
    public View2DCell (App2DCell cell, Window2D window) {
        this(cell, window, null);
    }

    /**
     * Create an instance of View2DCell with a specified geometry node.
     * @param cell The cell in which the view is displayed.
     * @param window The window displayed in this view.
     * @param geometryNode The geometry node on which to display the view.
     */
    public View2DCell (App2DCell cell, Window2D window, GeometryNode geometryNode) {
        super(window, geometryNode);
        this.cell = cell;

        changeMask = CHANGED_ALL;

        // Note: first-visible optimization: don't update now because not visible.
        // A later set visible to true will update everything.
    }

    /** Clean up resources. */
    public synchronized void cleanup () {
        super.cleanup();

        if (frame != null) {
            frame.cleanup();
            frame = null;
        }

        if (selfCreatedMovableComponent) {
            cell.removeComponent(MovableComponent.class);
        }

        cell = null;
    }

    /** Returns this view's cell */
    public App2DCell getCell () {
        return cell;
    }

    /** {@inheritDoc} */
    public View2DDisplayer getDisplayer () {
        return getCell();
    }

    /** {@inheritDoc} */
    public void applyDeltaRotationUser (Quaternion deltaRotation) {
        applyDeltaRotationUser(deltaRotation, true);
    }

    /** {@inheritDoc} */
    public void applyDeltaRotationUser (Quaternion deltaRotation, boolean update) {
        deltaRotationToApply = deltaRotation.clone();
        changeMask |= CHANGED_USER_TRANSFORM;
        if (update) {
            update();
        }
    }
        
    public synchronized void userRotateYStart (float dy) {
    }

    public synchronized void userRotateYUpdate (float dy) {
    }

    public synchronized void userRotateYFinish () {
    }

    // Uses: type, parent
    @Override
    protected Entity getParentEntity () {
        Entity cellEntity = 
            ((App2DCellRendererJME)cell.getCellRenderer(Cell.RendererType.RENDERER_JME)).getEntity();

        switch (type) {

        case UNKNOWN:
        case PRIMARY:
            // Attach primaries directly to cell entity
            return cellEntity;
        
        default:
            // Attach non-primaries to the entity of their parent, if possible
            if (parent == null) {
                logger.warning("Attempt to attach a non-primary view without a parent");
                logger.warning("cell = " + cell);
                logger.warning("view = " + this);
                logger.warning("view type = " + type);
                // This is the best we can do
                return cellEntity;
            } else {
                return parent.getEntity();
            }
        }
    }                

    // Uses: type, ortho, parent, stack
    protected Vector3f calcStackTranslation () {
        if (isOrtho()) {
            return new Vector3f(0f, 0f, 0f);
        } else {
            int stackPos = window.getStackPosition();
            if (stackPos < 0) {
                logger.info("ERROR: stack position cannot be calculated for window " + window);
                stackPos = 0;
            }
            logger.fine("View " + this);
            logger.fine("zOrder = " + window.getZOrder());
            logger.fine("stackPos = " + stackPos);
            float localZ;
            if ((type == Type.POPUP || type == Type.SECONDARY) && parent != null) {
                Window2D parentWindow = ((View2DCell)parent).window;
                if (parentWindow == null) {
                    localZ = 0f;
                } else {
                    int stackPosParent = parentWindow.getStackPosition();
                    logger.fine("stackPosParent = " + stackPosParent);
                    logger.fine("zOrder Parent = " + parentWindow.getZOrder());
                    localZ = (stackPos - stackPosParent) * STACK_GAP;
                }
            } else {
                localZ = stackPos * STACK_GAP;
            }

            logger.fine("localZ = " + localZ);
            return new Vector3f(0f, 0f, localZ);
        }
    }

    /** {@inheritDoc} */
    // Uses: deltaRotationToApply, deltaTranslationToApply
    protected void userTransformApplyDeltas (CellTransform userTransform) {
        userTransformApplyDeltaRotation(userTransform);
        userTransformApplyDeltaTranslation(userTransform);
    }

    // Apply any pending rotation delta to the given user transform.
    protected void userTransformApplyDeltaRotation (CellTransform userTransform) {
        if (deltaRotationToApply != null) {
            CellTransform transform = new CellTransform(null, null);
            transform.setRotation(deltaRotationToApply);
            userTransform.mul(transform);
            deltaRotationToApply = null;
        }
    }

    // TODO: eventually need to complete this
    @Override
    protected void updatePrimaryTransform (CellTransform newTransform) {
        /* TODO
        // To update a user transformations on a primary view we directly change the cell. Create and add
        // a movable component to the cell if it doesn't already have one.
        MovableComponent mc = (MovableComponent) cell.getComponent(MovableComponent.class);
        if (mc == null) {
            mc = new MovableComponent(cell);
            cell.addComponent(mc);
            selfCreatedMovableComponent = true;
        }
        mc.localMoveRequest(newTransform);
        */
    }

    /** {@inheritDoc} */
    @Override
    protected boolean hasFrame () {
        return frame != null;
    }

    /** {@inheritDoc} */
    @Override
    protected void attachFrame () {
        if (frame == null) {
            logger.fine("Create new frame for view " + this);
            frame = new Frame2DCell(this);
        }
        logger.fine("Attach frame to view " + this);
        frame.attachToViewEntity();
    }

    /** {@inheritDoc} */
    @Override
    protected void detachFrame () {
        if (frame != null) {
            logger.fine("Detach frame from view");
            frame.detachFromViewEntity();
        }
    }

    /** {@inheritDoc} */
    @Override
    protected void reattachFrame () {
        logger.fine("Reattach new frame, frame = " + frame);
        detachFrame();
        attachFrame();
    }

    /** {@inheritDoc} */
    @Override
    protected void frameUpdateTitle () {
        if (frame != null) {
            // Note: doesn't need to be done in render updater
            frame.setTitle(getTitle());
        }
    }

    /** {@inheritDoc} */
    @Override
    protected void frameUpdateUserResizable () {
        if (frame != null) {
            frame.setUserResizable(isUserResizable());
        }
    }

    /** {@inheritDoc} */
    @Override
    protected void frameUpdate () {
        if (frame != null) {
            try {
                frame.update();
            } catch (InstantiationException ex) {
                logger.warning("Exception during view frame update, ex = " + ex);
            }
        }
    }

    /** 
     * Returns the frame of this view.
     */
    protected Frame2DCell getFrame () {
        return frame;
    }
}


