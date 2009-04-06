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

    /** The user rotation. */
    private Quaternion userRotation = new Quaternion();

    /** The previous user rotation. */
    private Quaternion userRotationPrev = new Quaternion();

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
        update();
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

    /** Specify the rotation (comes from the user). Update afterward. */
    public synchronized void setRotationUser (Quaternion rotation) {
        setRotationUser(rotation, true);
    }

    /** Specify the rotation (comes from the user). Update afterward. */
    public synchronized void setRotationUser (Quaternion rotation, boolean update) {
        logger.info("change rotationUser = " + rotation);
        userRotationPrev = userRotation;
        userRotation = rotation.clone();
        changeMask |= CHANGED_USER_TRANSFORM;
        if (update) {
            update();
        }
    }

    /** {@inheritDoc} */
    public synchronized Quaternion getRotation () {
        return userRotation.clone();
    }

        
    public synchronized void userRotateYStart (float dy) {
    }

    public synchronized void userRotateYUpdate (float dy) {
    }

    public synchronized void userRotateYFinish () {
    }

    /** {@inheritDoc} */
    public synchronized void update () {
        super.processChanges();
        changeMask = 0;
    }

    // Uses: type, parent
    @Override
    protected Entity getParentEntity () {
        Entity cellEntity = 
            ((App2DCellRendererJME)cell.getCellRenderer(Cell.RendererType.RENDERER_JME)).getEntity();

        switch (type) {

        case UNKNOWN:
            // Can't attach until we know the type
            logger.warning("Attempt to attach a view of unknown type to an app cell");
            logger.warning("cell = " + cell);
            logger.warning("view = " + this);

            // This is the best we can do
            return cellEntity;

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

    protected Vector3f calcStackTranslation () {
        if (isOrtho()) {
            return new Vector3f(0f, 0f, 0f);
        } else {
            // TODO: quick and dirty. Must eventually fix.
            return new Vector3f(0f, 0f, (1-zOrder) * STACK_GAP);
        }
    }

    // Uses: userRotation, userTranslation
    protected CellTransform calcUserDeltaTransform () {

        // Apply the rotation first
        CellTransform rotDeltaTransform = calcUserRotationDeltaTransform();

        // Next, apply the translation
        CellTransform transDeltaTransform = calcUserTranslationDeltaTransform();
        rotDeltaTransform.mul(transDeltaTransform);

        return rotDeltaTransform; 
    }

    // Uses: userRotation
    private CellTransform calcUserRotationDeltaTransform () {
        Quaternion deltaRotation = userRotation.subtract(userRotationPrev);
        CellTransform rotDeltaTransform = new CellTransform(null, null, null);
        rotDeltaTransform.setRotation(deltaRotation);
        return rotDeltaTransform;
    }

    @Override
    protected void updatePrimaryTransform (CellTransform userDeltaTransform) {
        Vector3f translation = getTranslationUserCurrent();
        if (type == Type.PRIMARY && isOrtho()) {
            Vector2f locOrtho = getLocationOrtho();
            translation.addLocal(new Vector3f(locOrtho.x, locOrtho.y, 0f));
        }
        sgChangeTransformUserSet(viewNode, new CellTransform(null, translation, null));

        /*TODO: need to figure this out
        if (isOrtho()) {
            // TODO: temp: just something to prod sgProcessChanges
        } else {
        CellTransform cellTransform = cell.getLocalTransform();
        cellTransform.mul(userDeltaTransform);

        // User transformations on primary directly change the cell. Create and add
        // a movable component to the cell if it doesn't already have one.
        MovableComponent mc = (MovableComponent) cell.getComponent(MovableComponent.class);
        if (mc == null) {
            mc = new MovableComponent(cell);
            cell.addComponent(mc);
            selfCreatedMovableComponent = true;
        }
        ///TODO:        mc.localMoveRequest(cellTransform);
        }
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
        logger.fine("Create new frame");
        frame = new Frame2DCell(this);
    }

    /** {@inheritDoc} */
    @Override
    protected void detachFrame () {
        logger.fine("Destroy frame");
        frame.cleanup();
        frame = null;
    }

    /** {@inheritDoc} */
    @Override
    protected void frameUpdateTitle () {
        if (frame != null) {
            // Note: doesn't need to be done in render updater
            logger.fine("Update title");
            frame.setTitle(title);
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
}


