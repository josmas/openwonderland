/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jdesktop.wonderland.modules.appbase.client.view;

import com.jme.image.Image;
import com.jme.math.Vector2f;
import com.jme.math.Vector3f;
import com.jme.scene.Node;
import java.awt.Dimension;
import java.awt.Point;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;
import org.jdesktop.mtgame.CollisionComponent;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.mtgame.RenderComponent;
import org.jdesktop.wonderland.client.jme.ClientContextJME;
import org.jdesktop.wonderland.common.cell.CellTransform;
import org.jdesktop.wonderland.modules.appbase.client.DrawingSurface;
import org.jdesktop.wonderland.modules.appbase.client.Window2D;
import org.jdesktop.wonderland.modules.appbase.client.view.View2D.Type;
import org.jdesktop.wonderland.modules.appbase.client.swing.WindowSwing;
import static org.jdesktop.wonderland.modules.appbase.client.view.ViewEntityUtils.*;
import static org.jdesktop.wonderland.modules.appbase.client.view.ViewEntityUtils.AttachState.*;
import static org.jdesktop.wonderland.modules.appbase.client.view.ViewEntityUtils.FrameChange.*;
import org.jdesktop.wonderland.modules.appbase.client.view.scenegraph.SceneGraphChange;
import org.jdesktop.wonderland.modules.appbase.client.view.scenegraph.SceneGraphChangeProcessor;
import static org.jdesktop.wonderland.modules.appbase.client.view.scenegraph.ChangeFactory.*;

/**
 *
 * @author JagWire
 */
public class View2DEntityUpdater {

    private final View2DEntity viewEntity;
//    private SceneGraphChangeProcessor processor;
    private static final Logger logger = Logger.getLogger(View2DEntityUpdater.class.getName());
    private final List<SceneGraphChange> changes;

    public View2DEntityUpdater(View2DEntity viewEntity,
            List<SceneGraphChange> changes) {
        this.viewEntity = viewEntity;
        this.changes = changes;
//        this.processor = new SceneGraphChangeProcessor(viewEntity, changes);
    }

    public synchronized void update() {
        int changeMask = viewEntity.changeMask;
        AttachState attachState = viewEntity.getAttachState();
        GeometryNode geometryNode = viewEntity.getGeometryNode();
        Entity parentEntity = viewEntity.getParentEntity();
        Entity entity = viewEntity.getEntity();
        
        // Only-Update-When-Visible Optimization
        // 1. Always perform any visibility or size changes immediately.
        if ((changeMask & (CHANGED_VISIBLE | CHANGED_SIZE_APP)) == 0) {
            // 2. Don't perform other changes unless the view if both the app and the user 
            // have made it visible.
            if (!viewEntity.isVisibleApp() || !viewEntity.isVisibleUser()) {
                return;
            }
        }

        // Note: all of the scene graph changes are queued up and executed at the end
        boolean windowNeedsValidate = false;

        // For Debug - Part 1: Uncomment this to print info for HUD views only
        //if (!("View2DCell".equals(this.getClass().getName()))) {

        logger.info("------------------ Processing changes for view " + viewEntity);
        logger.info("type " + viewEntity.getType());
        logChangeMask(changeMask);

        // For Debug - Part 2: Uncomment this to print info for HUD views only
        //        }

        // React to topology related changes
        if (topologyHasChanged(changeMask)) {
            logger.fine("Update topology for view " + this);

            parentEntity = detachEntity(attachState, parentEntity, entity);

            attachState = DETACHED;

//            GeometryNode geometryNode = viewEntity.getGeometryNode();
            GeometryNode newGeometryNode = viewEntity.newGeometryNode;
            Node viewNode = viewEntity.getViewNode();

            // Does the geometry node itself need to change?
            if (geometryNeedsToChange(changeMask)) {
                if (geometryNode != null) {
                    // Note: don't need to do in RenderUpdater because we've detached our entity
//                    sgChangeGeometryDetachFromView(viewNode, geometryNode);
                    changes.add(detachFromView(viewNode, geometryNode));
                    if (viewEntity.isGeometrySelfCreated()) {
//                        sgChangeGeometryCleanup(geometryNode);
                        changes.add(cleanup(geometryNode));
//                        geometrySelfCreated = false;
                        viewEntity.setGeometrySelfCreated(false);
                    }
                }
                if (newGeometryNode != null) {
                    geometryNode = newGeometryNode;
                    newGeometryNode = null;
                } else {
                    geometryNode = new GeometryNodeQuad(viewEntity);
//                    geometrySelfCreated = true;
                    viewEntity.setGeometrySelfCreated(true);
                }
                // Note: don't need to do in RenderUpdater because we've detached our entity
//                sgChangeGeometryAttachToView(viewNode, geometryNode);
                changes.add(attachToView(viewNode, geometryNode));
            }

            // Uses: window
            if ((changeMask & (CHANGED_GEOMETRY | CHANGED_SIZE_APP)) != 0) {
                logger.fine("Update texture for view " + this);
                if (geometryNode != null) {
                    DrawingSurface surface = viewEntity.getWindow().getSurface();
                    if (surface != null) {
//                        sgChangeGeometryTextureSet(geometryNode, viewEntity.getWindow().getTexture(), surface);
                        changes.add(textureSet(geometryNode, viewEntity.getWindow().getTexture(), surface));
                        windowNeedsValidate = true;
                    }
                }
            }

            // Now reattach geometry if view should be visible
            // Note: MTGame can currently only setOrtho on a visible rc
            // Uses: visible, ortho

            if (viewEntity.isActuallyVisible()) {
                if (viewEntity.isOrtho()) {
                    Type type = viewEntity.getType();
//                    Entity parentEntity = viewEntity.getParentEntity();
                    logger.fine("View is ortho for view " + viewEntity);
                    entity.getComponent(RenderComponent.class).setOrtho(true);
                    entity.getComponent(CollisionComponent.class).setCollidable(false);

                    if (type == View2D.Type.PRIMARY || type == View2D.Type.UNKNOWN) {
                        // Attach top level ortho views directly to world
                        ClientContextJME.getWorldManager().addEntity(entity);
                        attachState = ATTACHED_TO_WORLD;
                        logger.fine("Attached entity " + entity + " to world manager.");
                    } else {
//                        parentEntity = getParentEntity();
                        if (parentEntity == null) {
                            // Has no Parent; attach directly to world
                            ClientContextJME.getWorldManager().addEntity(entity);
                            attachState = ATTACHED_TO_WORLD;
                            logger.fine("Attached parentless entity " + entity + " to world manager.");
                        } else {
                            RenderComponent rc = (RenderComponent) entity.getComponent(RenderComponent.class);
                            // TODO: these two statements appear to be obsolete.
                            RenderComponent rcParent =
                                    (RenderComponent) parentEntity.getComponent(RenderComponent.class);
                            Node attachNode = rcParent.getSceneRoot();

                            // Note: we need to attach non-primaries to the parent geometry node in 
                            // ortho mode, rather than the view node. This way it picks up the parent's
                            // offset translation, which contains locationOrtho
                            // TODO: do this cleaner. Convert attach node to a view and get the
                            // geometry node for this view.
                            attachNode = (Node) attachNode.getChild(0);

//                            sgChangeAttachPointSetAddEntity(rc, attachNode, parentEntity, entity);
                            changes.add(attachPointSetAddEntity(rc, attachNode, parentEntity, entity));
                            attachState = ATTACHED_TO_ENTITY;
                            logger.fine("Attach ortho entity " + entity + " to geometry node of parent entity " + parentEntity);
                        }
                    }
                } else {
                    logger.fine("View is not ortho for view " + this);
//                    parentEntity = getParentEntity();
                    if (parentEntity == null) {
                        logger.warning("getParentEntity() returns null; must be non-null");
                    } else {
                        logger.fine("Attach entity " + entity + " to parent entity " + parentEntity);

                        RenderComponent rc = (RenderComponent) entity.getComponent(RenderComponent.class);
                        RenderComponent rcParent =
                                (RenderComponent) parentEntity.getComponent(RenderComponent.class);
                        Node attachNode = rcParent.getSceneRoot();

                        // SPECIAL NOTE: Here is where special surgery is done on header windows so 
                        // that they are parented to the *geometry node* of their parent view instead of 
                        // the view node, as windows normally are. This way it picks up the offset
                        // translation in the geometry node and stays in sync with the rest of the frame.
                        // See also: SPECIAL NOTE in Frame2DCell.attachViewToEntity.
                        Window2D window = viewEntity.getWindow();
                        if (window instanceof WindowSwingHeader) {
                            WindowSwingHeader wsh = (WindowSwingHeader) window;
                            if (wsh.getView().getType() == View2D.Type.SECONDARY) {
                                // TODO: do this cleaner. Convert attach node to a view and get the
                                // geometry node for this view.
                                attachNode = (Node) attachNode.getChild(0);
                            }
                        }

//                        sgChangeAttachPointSetAddEntity(rc, attachNode, parentEntity, entity);
                        changes.add(attachPointSetAddEntity(rc, attachNode, parentEntity, entity));
                        attachState = ATTACHED_TO_ENTITY;
                        entity.getComponent(RenderComponent.class).setOrtho(false);
                        entity.getComponent(CollisionComponent.class).setCollidable(true);
                    }
                }
            }

            if ((changeMask & CHANGED_VISIBLE) != 0) {
                // Update visibility of children
                logger.fine("Update children visibility for view " + this);

                for (View2DEntity child : viewEntity.getChildren()) {
                    child.updateVisibility(viewEntity.isInCleanup());
                }
            }
        }

        // Determine what frame changes need to be performed. But these aren't executed now;
        // they are executed later by view.updateFrame, which must be invoked outside the window lock.
        // issue 151: prepare for reattachment of frame on resize. 
        if (frameChangeIsNeeded(changeMask)) {
            logger.fine("Update frame for view " + this);
            logger.fine("decorated " + viewEntity.isDecorated());

            handleFrameChanges(changeMask);
        }

        if ((changeMask & (CHANGED_STACK | CHANGED_ORTHO)) != 0) {
            logger.fine("Update geometry ortho Z order for view " + this);

            boolean ortho = viewEntity.isOrtho();
            Window2D window = viewEntity.getWindow();
//            GeometryNode geometryNode = viewEntity.getGeometryNode();

            if (ortho) {
                if (window != null) {
                    int zOrder = window.getZOrder();
                    logger.fine("Z order = " + zOrder);
                    if (zOrder >= 0) {
//                        sgChangeGeometryOrthoZOrderSet(geometryNode, zOrder);
                        changes.add(orthoZOrderSet(geometryNode, zOrder));
                    }
                }
            }
        }

        if ((changeMask & CHANGED_ORTHO) != 0) {
            // MTGame: can currently only setOrtho on a visible rc
            boolean ortho = viewEntity.isOrtho();
            Node viewNode = viewEntity.getViewNode();

            if (viewEntity.isActuallyVisible()) {
                entity.getComponent(RenderComponent.class).setOrtho(ortho);
                entity.getComponent(CollisionComponent.class).setCollidable(!ortho);
            }
//            sgChangeViewNodeOrthoSet(viewNode, ortho);
            changes.add(viewNodeOrthoSet(viewNode, ortho));
        }

        // React to size related changes (must be done before handling transform changes)
        if ((changeMask & (CHANGED_DECORATED | CHANGED_SIZE_APP | CHANGED_PIXEL_SCALE
                | CHANGED_ORTHO)) != 0) {

            float width = viewEntity.getDisplayerLocalWidth();
            float height = viewEntity.getDisplayerLocalHeight();
//            GeometryNode geometryNode = viewEntity.getGeometryNode();

//            sgChangeGeometrySizeSet(geometryNode, width, height);
            changes.add(sizeSet(geometryNode, width, height));
            /**
             * Subtle: Changing the size of the quad will stomp the texture
             * coordinates. We must force them to be restored.
             */
            changeMask |= CHANGED_TEX_COORDS;
        }

        // React to texture coordinate changes
        // Uses: window, texture
        if ((changeMask & (CHANGED_TEX_COORDS | CHANGED_GEOMETRY | CHANGED_SIZE_APP)) != 0) {
            // TODO: for now, texcoords only depend on app size. Eventually this should
            // be the effective aperture rectangle width and height
            float width = (float) viewEntity.getSizeApp().width;
            float height = (float) viewEntity.getSizeApp().height;
            if (viewEntity.getWindow() != null
                    && viewEntity.getWindow().getTexture() != null) {

//                GeometryNode geometryNode = viewEntity.getGeometryNode();
                Image image = viewEntity.getWindow().getTexture().getImage();
                float widthRatio = width / image.getWidth();
                float heightRatio = height / image.getHeight();

//                sgChangeGeometryTexCoordsSet(geometryNode, widthRatio, heightRatio);
                changes.add(textureCoordinateSet(geometryNode, widthRatio, heightRatio));
                windowNeedsValidate = true;
            }
        }

        // React to transform related changes
        // Uses: type, parent, pixelscale, size, offset, ortho, locationOrtho, stack
        if ((changeMask & (CHANGED_TYPE | CHANGED_PARENT | CHANGED_PIXEL_SCALE | CHANGED_SIZE_APP
                | CHANGED_OFFSET | CHANGED_ORTHO | CHANGED_LOCATION_ORTHO | CHANGED_STACK)) != 0) {
            CellTransform transform = null;

            Type type = viewEntity.getType();
            boolean ortho = viewEntity.isOrtho();
            Vector2f locationOrtho = viewEntity.getLocationOrtho();
//            GeometryNode geometryNode = viewEntity.getGeometryNode();

            switch (type) {
                case UNKNOWN:
                case PRIMARY:
                    transform = new CellTransform(null, null);
                    if (ortho) {
                        Vector3f orthoLocTranslation = new Vector3f();
                        orthoLocTranslation.x = locationOrtho.x;
                        orthoLocTranslation.y = locationOrtho.y;
                        transform.setTranslation(orthoLocTranslation);
                    } else {
                        // Note: primaries now also honor the offset.
                        // Uses: type, parent, pixelScale, size, offset, ortho
                        transform = calcOffsetStackTransform();
                    }
                    break;
                case SECONDARY:
                case POPUP:
                    // Uses: type, parent, pixelScale, size, offset, ortho
                    transform = calcOffsetStackTransform();
            }
//            sgChangeGeometryTransformOffsetStackSet(geometryNode, transform);
            changes.add(offsetStackSet(geometryNode, transform));
        }

        // Update the view node's user transform, if necessary
        // Uses: type, deltaTranslationToApply
        if ((changeMask & (CHANGED_TYPE | CHANGED_USER_TRANSFORM | CHANGED_ORTHO)) != 0) {

            // Select the current user transform based on the ortho mode
            CellTransform currentUserTransform;
            boolean ortho = viewEntity.isOrtho();

            CellTransform userTransformOrtho = new CellTransform(null, null);
            CellTransform userTransformCell = new CellTransform(null, null);

            if (ortho) {
                currentUserTransform = userTransformOrtho;
            } else {
                currentUserTransform = userTransformCell;
            }

            if (!viewEntity.userTransformCellReplaced) {
                // Apply any pending user transform deltas (by post-multiplying them
                // into the current user transform
                logger.fine("currentUserTransform (before) = " + currentUserTransform);
                userTransformApplyDeltas(currentUserTransform);
            }

            logger.fine("currentUserTransform (latest) = " + currentUserTransform);

            Type type = viewEntity.getType();
            Window2D window = viewEntity.getWindow();
            boolean userTransformCellChangedLocalOnly = viewEntity.userTransformCellChangedLocalOnly;
            Node viewNode = viewEntity.getViewNode();

            // Now put the update user transformation into effect
            switch (type) {
                case UNKNOWN:
                case PRIMARY:
                    viewEntity.updatePrimaryTransform(currentUserTransform);
                    break;
                case SECONDARY:
//                    sgChangeTransformUserSet(viewNode, currentUserTransform);
                    changes.add(userSet(viewNode, currentUserTransform));
                    // Note: moving a secondary in the cell doesn't change the position
                    // of the secondary in ortho, and vice versa.
                    if (!ortho && !userTransformCellChangedLocalOnly) {
                        window.changedUserTransformCell(userTransformCell, viewEntity);
                    }
                    break;
                case POPUP:
                    // Always set to identity
//                    sgChangeTransformUserSet(viewNode, new CellTransform(null, null));
                    changes.add(userSet(viewNode, new CellTransform(null, null)));
            }

            viewEntity.userTransformCellReplaced = false;
            viewEntity.userTransformCellChangedLocalOnly = false;
        }

        // Changing the 3D size of the app can change the offset of children, such as headers.
        if ((changeMask & (CHANGED_SIZE_APP | CHANGED_PIXEL_SCALE)) != 0) {
            for (View2DEntity childView : viewEntity.getChildren()) {
                childView.changeOffsetSelfAndChildren();
            }
        }

        new SceneGraphChangeProcessor(viewEntity, changes).processChanges();
//        sgProcessChanges();

        /*
         * For Debug System.err.println("************* After
         * View2DEntity.processChanges, viewNode = ");
         * GraphicsUtils.printNode(viewNode);
         */

        /*
         * For debug of ortho entities which should be visible WorldManager wm =
         * ClientContextJME.getWorldManager(); for (int i=0; i <
         * wm.numEntities(); i++) { Entity e = wm.getEntity(i); if
         * (e.toString().equals("<Plug the name of the window in here")) {
         * System.err.println("e = " + e); RenderComponent rc =
         * (RenderComponent) e.getComponent(RenderComponent.class); } }
         */

        // In certain situations, especially after we change the texture, WindowSwings
        // need to be repainted into that texture.
        Window2D window = viewEntity.getWindow();
        if (windowNeedsValidate) {
            if (window instanceof WindowSwing) {
                ((WindowSwing) window).validate();
            }
        }

        // Inform the window's surface of the view visibility.
        if (window != null) {
            DrawingSurface surface = window.getSurface();
            if (surface != null) {
                surface.setViewIsVisible(viewEntity, viewEntity.isActuallyVisible());
            }
        }

        // Make sure that all descendent views are up-to-date
        logger.fine("Update children for view " + this);
        for (View2DEntity child : viewEntity.getChildren()) {
            if (child.changeMask != 0) {
                child.update();
            }
        }

        changeMask = 0;
    }

    private boolean geometryNeedsToChange(int changeMask) {
        return (changeMask & CHANGED_GEOMETRY) != 0;
    }

    private Entity detachEntity(AttachState attachState, Entity parentEntity, Entity entity) {
        // First, detach entity (if necessary)
        switch (attachState) {
            case ATTACHED_TO_ENTITY:

                if (parentEntity != null) {
                    logger.fine("Remove entity " + viewEntity.getEntity() + " from parent entity " + viewEntity.getParentEntity());
                    RenderComponent rc = (RenderComponent) entity.getComponent(RenderComponent.class);
//                        sgChangeAttachPointSetAddEntity(rc, null, null, null);
                    changes.add(attachPointSetAddEntity(rc, null, null, null));
                    parentEntity.removeEntity(entity);
                    parentEntity = null;
                }
                break;
            case ATTACHED_TO_WORLD:
                logger.fine("Remove entity " + entity + " from world manager.");
                ClientContextJME.getWorldManager().removeEntity(entity);
                break;
        }
        return parentEntity;
    }

    private boolean topologyHasChanged(int changeMask) {
        return (changeMask & (CHANGED_GEOMETRY | CHANGED_SIZE_APP | CHANGED_TYPE | CHANGED_PARENT
                | CHANGED_VISIBLE | CHANGED_ORTHO)) != 0;
    }

    private boolean frameChangeIsNeeded(int changeMask) {
        return (changeMask & (CHANGED_DECORATED | CHANGED_TITLE | CHANGED_TYPE | CHANGED_SIZE_APP
                | CHANGED_PIXEL_SCALE | CHANGED_USER_RESIZABLE | CHANGED_VISIBLE)) != 0;
    }

    private void handleFrameChanges(int changeMask) {
        boolean decorated = viewEntity.isDecorated();

        List<FrameChange> frameChanges = viewEntity.getFrameChanges();

        if ((changeMask & (CHANGED_DECORATED | CHANGED_VISIBLE)) != 0) {
            // Some popups initiall are decorated and then are set to undecorated before
            // the popup becomes visible. So to avoid wasting time, wait until the window
            // becomes visible before attaching its frame.
            if (decorated && viewEntity.isActuallyVisible()) {
                if (!viewEntity.hasFrame()) {
                    logger.fine("Attach frame");
                    frameChanges.add(ATTACH_FRAME);
                }
            } else {
                if (viewEntity.hasFrame()) {
                    logger.fine("Detach frame");
                    frameChanges.add(DETACH_FRAME);
                }
            }
        }

        if ((changeMask & CHANGED_TITLE) != 0) {
            if (decorated && viewEntity.hasFrame()) {
                frameChanges.add(UPDATE_TITLE);
            }
        }

        if ((changeMask & (CHANGED_TYPE | CHANGED_SIZE_APP)) != 0) {
            if (decorated) {
                frameChanges.add(REATTACH_FRAME);
            }
        }
        if ((changeMask & (CHANGED_USER_RESIZABLE | CHANGED_VISIBLE)) != 0) {
            if (decorated) {
                frameChanges.add(UPDATE_USER_RESIZABLE);
            }
        }
    }

    private void logChangeMask(int mask) {
        logger.info("changeMask " + Integer.toHexString(mask));
        int bit = 0x1;
        for (int i = 0; i < 32; i++, bit <<= 1) {
            int thisBit = mask & bit;
            if (thisBit != 0) {
                String str;
                switch (thisBit) {
                    case CHANGED_TYPE:
                        str = "CHANGED_TYPE";
                        break;
                    case CHANGED_PARENT:
                        str = "CHANGED_PARENT";
                        break;
                    case CHANGED_VISIBLE:
                        str = "CHANGED_VISIBLE";
                        break;
                    case CHANGED_DECORATED:
                        str = "CHANGED_DECORATED";
                        break;
                    case CHANGED_GEOMETRY:
                        str = "CHANGED_GEOMETRY";
                        break;
                    case CHANGED_SIZE_APP:
                        str = "CHANGED_SIZE_APP";
                        break;
                    case CHANGED_PIXEL_SCALE:
                        str = "CHANGED_PIXEL_SCALE";
                        break;
                    case CHANGED_OFFSET:
                        str = "CHANGED_OFFSET";
                        break;
                    case CHANGED_USER_TRANSFORM:
                        str = "CHANGED_USER_TRANSFORM";
                        break;
                    case CHANGED_TITLE:
                        str = "CHANGED_TITLE";
                        break;
                    case CHANGED_STACK:
                        str = "CHANGED_STACK";
                        break;
                    case CHANGED_ORTHO:
                        str = "CHANGED_ORTHO";
                        break;
                    case CHANGED_LOCATION_ORTHO:
                        str = "CHANGED_LOCATION_ORTHO";
                        break;
                    case CHANGED_TEX_COORDS:
                        str = "CHANGED_TEX_COORDS";
                        break;
                    case CHANGED_USER_RESIZABLE:
                        str = "CHANGED_USER_RESIZABLE";
                        break;
                    default:
                        continue;
                }

                // Printed selected values
                String str2 = null;
                switch (thisBit) {
                    case CHANGED_TYPE:
                        str2 = ": type = " + viewEntity.getType();
                        break;
                    case CHANGED_PARENT:
                        str2 = ": parent = " + viewEntity.getParent();//parent;
                        break;
                    case CHANGED_VISIBLE:
                        str2 = ": visibleApp = " + viewEntity.isVisibleApp()
                                + ", visibleUser = " + viewEntity.isVisibleUser();
                        break;
                    case CHANGED_DECORATED:
                        str2 = ": decorated = " + viewEntity.isDecorated();
                        break;
                    case CHANGED_GEOMETRY:
                        break;
                    case CHANGED_SIZE_APP:
                        str2 = ": sizeApp = " + viewEntity.getSizeApp();//sizeApp;
                        break;
                    case CHANGED_PIXEL_SCALE:
                        str2 = ": pixelScaleCell = " + viewEntity.getPixelScale()
                                + ", pixelScaleOrtho = " + viewEntity.getPixelScaleOrtho();//pixelScaleOrtho;
                        break;
                    case CHANGED_OFFSET:
                        str2 = ": offset = " + viewEntity.getOffset()
                                + ", pixelOffset = " + viewEntity.getPixelOffset();//pixelOffset;
                        break;
                    case CHANGED_USER_TRANSFORM:
                        str2 = ": deltaTranslationToApply = " + viewEntity.getDeltaTranslationToApply();
                        break;
                    case CHANGED_TITLE:
                        str2 = ": title = " + viewEntity.getTitle();
                        break;
                    case CHANGED_STACK:
                        break;
                    case CHANGED_ORTHO:
                        str2 = ": ortho = " + viewEntity.isOrtho();
                        break;
                    case CHANGED_LOCATION_ORTHO:
                        str2 = ": locationOrtho = " + viewEntity.getLocationOrtho();
                        break;
                    case CHANGED_TEX_COORDS:
                        break;
                    case CHANGED_USER_RESIZABLE:
                        str2 = ": userResizable = " + viewEntity.isUserResizable();
                        break;
                }

                str += "(" + Integer.toHexString(thisBit) + ")";
                if (str2 != null) {
                    str += str2;
                }
                logger.info(str);
            }
        }
    }

    protected void userTransformApplyDeltas(CellTransform userTransform) {
        userTransformApplyDeltaTranslation(userTransform);
    }

    // Apply any pending translation delta to the given user transform.
    protected void userTransformApplyDeltaTranslation(CellTransform userTransform) {
        Vector3f deltaTranslationToApply = viewEntity.getDeltaTranslationToApply();

        if (deltaTranslationToApply != null) {
            CellTransform transform = new CellTransform(null, null);
            transform.setTranslation(deltaTranslationToApply);
            //System.err.println("******* delta translation transform = " + transform);
            userTransform.mul(transform);
            deltaTranslationToApply = null;
        }
    }

    /*
     * CALCULATORS SECTION
     *
     */
    private Vector3f calcOffsetTranslation() {
        Vector3f translation = new Vector3f();

        boolean ortho = viewEntity.isOrtho();
        Type type = viewEntity.getType();
        Vector2f locationOrtho = viewEntity.getLocationOrtho();
        View2DEntity parent = (View2DEntity) viewEntity.getParent();
        Dimension sizeApp = viewEntity.getSizeApp();
        Point pixelOffset = viewEntity.getPixelOffset();
        Vector2f offset = viewEntity.getOffset();

        if (ortho) {
            if (type == Type.PRIMARY || type == Type.UNKNOWN) {
                translation.x = locationOrtho.x;
                translation.y = locationOrtho.y;
            } else {

                if (parent == null) {
                    return translation;
                }

                // Initialize to the first part of the offset (the local coordinate translation)
                logger.fine("view = " + this);
                logger.fine("parent = " + parent);
                logger.fine("locationOrtho = " + locationOrtho);
                logger.fine("offset = " + offset);
                translation.x = locationOrtho.x + offset.x;
                translation.y = locationOrtho.y + offset.y;
                logger.fine("translation 1 = " + translation);

                // Convert pixel offset to local coords and add it in
                Dimension parentSize = parent.getSizeApp();
                Vector2f pixelScaleOrtho = parent.getPixelScaleOrtho();
                logger.fine("parentSize = " + parentSize);
                translation.x += -parentSize.width * pixelScaleOrtho.x / 2f;
                translation.y += parentSize.height * pixelScaleOrtho.y / 2f;
                logger.fine("translation 2 = " + translation);
                logger.fine("sizeApp = " + sizeApp);
                pixelScaleOrtho = viewEntity.getPixelScaleOrtho();
                translation.x += sizeApp.width * pixelScaleOrtho.x / 2f;
                translation.y -= sizeApp.height * pixelScaleOrtho.y / 2f;
                logger.fine("translation 3 = " + translation);
                logger.fine("pixelOffset = " + pixelOffset);
                translation.x += pixelOffset.x * pixelScaleOrtho.x;
                translation.y -= pixelOffset.y * pixelScaleOrtho.y;
                logger.fine("translation 4 = " + translation);
            }
        } else {

            // Initialize to the first part of the offset (the local coordinate translation)
            logger.fine("view = " + this);
            logger.fine("parent = " + parent);
            logger.fine("offset = " + offset);
            translation.x = offset.x;
            translation.y = offset.y;
            logger.fine("translation 1 = " + translation);

            if (type != Type.PRIMARY && type != Type.UNKNOWN && parent != null) {

                // Convert pixel offset to local coords and add it in
                // TODO: does the width/height need to include the scroll bars?
                Vector2f pixelScale = parent.getPixelScaleCurrent();
                Dimension parentSize = parent.getSizeApp();
                logger.fine("parentSize = " + parentSize);
                translation.x += -parentSize.width * pixelScale.x / 2f;
                translation.y += parentSize.height * pixelScale.y / 2f;
                logger.fine("translation 2 = " + translation);
                logger.fine("sizeApp = " + sizeApp);
                pixelScale = viewEntity.getPixelScaleCurrent();
                translation.x += sizeApp.width * pixelScale.x / 2f;
                translation.y -= sizeApp.height * pixelScale.y / 2f;
                logger.fine("translation 3 = " + translation);
                logger.fine("pixelOffset = " + pixelOffset);
                translation.x += pixelOffset.x * pixelScale.x;
                translation.y -= pixelOffset.y * pixelScale.y;
                logger.fine("translation 4 = " + translation);
            }
        }

        logger.fine("view = " + this);
        logger.fine("offset translation = " + translation);

        return translation;
    }

    protected Vector3f calcStackTranslation() {
        return new Vector3f(0f, 0f, 0f);
    }

    private CellTransform calcOffsetStackTransform() {
        CellTransform transform = new CellTransform(null, null);

        // Uses: parent, pixelScale, size, offset, ortho
        Vector3f offsetTranslation = calcOffsetTranslation();

        // Uses: type
        Vector3f stackTranslation = calcStackTranslation();

        offsetTranslation.addLocal(stackTranslation);

        // TODO: HACK: Part 3 of 4 temporary workaround for 951
        offsetTranslation.addLocal(new Vector3f(0f, 0f, viewEntity.getHackZEpsilon()));

        transform.setTranslation(offsetTranslation);

        return transform;
    }
}
