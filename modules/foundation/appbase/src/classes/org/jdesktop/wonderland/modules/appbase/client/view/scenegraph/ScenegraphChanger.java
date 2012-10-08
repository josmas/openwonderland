/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jdesktop.wonderland.modules.appbase.client.view.scenegraph;

import com.jme.image.Texture;
import com.jme.image.Texture2D;
import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import com.jme.scene.Node;
import com.jme.scene.Spatial;
import com.jme.scene.state.TextureState;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.mtgame.RenderComponent;
import org.jdesktop.mtgame.RenderUpdater;
import org.jdesktop.wonderland.client.jme.ClientContextJME;
import org.jdesktop.wonderland.common.cell.CellTransform;
import org.jdesktop.wonderland.modules.appbase.client.DrawingSurface;
import org.jdesktop.wonderland.modules.appbase.client.view.GeometryNode;
import org.jdesktop.wonderland.modules.appbase.client.view.View2DEntity;

/**
 *
 * @author Ryan
 */
public class ScenegraphChanger {

    private List<SGChange> sgChanges = new LinkedList<SGChange>();
    private static Logger logger = Logger.getLogger(ScenegraphChanger.class.getName());
    private View2DEntity viewEntity;

    public ScenegraphChanger(View2DEntity viewEntity) {
        this.viewEntity = viewEntity;
    }

    public void processChanges() {
        sgProcessChanges(viewEntity.getViewNode());
    }
    
    private void sgProcessChanges(final Node viewNode) {
        if (sgChanges.size() <= 0) {
            return;
        }

        ClientContextJME.getWorldManager().addRenderUpdater(new RenderUpdater() {

            public void update(Object arg0) {

                for (SGChange sgChange : sgChanges) {
                    switch (sgChange.getOp()) {

                        case GEOMETRY_ATTACH_TO_VIEW: {
                            SGChangeGeometryAttachToView chg = (SGChangeGeometryAttachToView) sgChange;
                            chg.viewNode.attachChild(chg.geometryNode);
                            logger.fine("Attach geometryNode " + chg.geometryNode + " to viewNode "
                                    + chg.viewNode);
                            break;
                        }

                        case GEOMETRY_DETACH_FROM_VIEW: {
                            SGChangeGeometryDetachFromView chg = (SGChangeGeometryDetachFromView) sgChange;
                            chg.viewNode.detachChild(chg.geometryNode);
                            logger.fine("Detach geometryNode " + chg.geometryNode + " from viewNode "
                                    + chg.viewNode);
                            break;
                        }

                        case GEOMETRY_SIZE_SET: {
                            SGChangeGeometrySizeSet chg = (SGChangeGeometrySizeSet) sgChange;
                            chg.geometryNode.setSize(chg.width, chg.height);
                            viewEntity.forceTextureIdAssignment(true);
                            logger.fine("******** Geometry node = " + chg.geometryNode);
                            logger.fine("******** Geometry node setSize, wh = " + chg.width + ", " + chg.height);
                            break;
                        }

                        case GEOMETRY_TEX_COORDS_SET: {
                            SGChangeGeometryTexCoordsSet chg = (SGChangeGeometryTexCoordsSet) sgChange;
                            chg.geometryNode.setTexCoords(chg.widthRatio, chg.heightRatio);
//                            logger.fine("******** viewNode = " + viewNode);
                            logger.fine("******** Geometry node setTexCoords, whRatio = " + chg.widthRatio + ", "
                                    + chg.heightRatio);
                            break;
                        }

                        case GEOMETRY_TEXTURE_SET: {
                            SGChangeGeometryTextureSet chg = (SGChangeGeometryTextureSet) sgChange;

                            DrawingSurface surface = chg.surface;
                            boolean restoreUpdating = false;
                            if (surface.getUpdateEnable()) {
                                surface.setUpdateEnable(false);
                                restoreUpdating = true;
                            }
                            chg.geometryNode.setTexture(chg.texture);

                            if (restoreUpdating) {
                                surface.setUpdateEnable(true);
                            }

                            logger.fine("Geometry node setTexture, texture = " + chg.texture);
                            break;
                        }

                        case GEOMETRY_ORTHO_Z_ORDER_SET: {
                            SGChangeGeometryOrthoZOrderSet chg = (SGChangeGeometryOrthoZOrderSet) sgChange;
                            if (chg.geometryNode != null) {
                                chg.geometryNode.setOrthoZOrder(chg.zOrder);
                            }
                            logger.fine("Geometry set ortho z order = " + chg.zOrder);
                            break;
                        }

                        case GEOMETRY_CLEANUP: {
                            SGChangeGeometryCleanup chg = (SGChangeGeometryCleanup) sgChange;
                            chg.geometryNode.cleanup();
                            logger.fine("Geometry node cleanup");
                            break;
                        }

                        case VIEW_NODE_ORTHO_SET: {
                            SGChangeViewNodeOrthoSet chg = (SGChangeViewNodeOrthoSet) sgChange;
                            if (chg.ortho) {
                                chg.viewNode.setCullHint(Spatial.CullHint.Never);
                            } else {
                                chg.viewNode.setCullHint(Spatial.CullHint.Inherit);
                            }
                            logger.fine("View node ortho cull hint set = " + chg.ortho);
                            break;
                        }

                        case GEOMETRY_TRANSFORM_OFFSET_STACK_SET: {
                            // The offset/stack transform resides in the geometry
                            SGChangeGeometryTransformOffsetStackSet chg =
                                    (SGChangeGeometryTransformOffsetStackSet) sgChange;
                            chg.geometryNode.setTransform(chg.transform);
                            logger.fine("Geometry node set transform, transform = " + chg.transform);
                            break;
                        }

                        case TRANSFORM_USER_SET: {
                            SGChangeTransformUserSet chg = (SGChangeTransformUserSet) sgChange;
                            CellTransform userTransform = chg.transform.clone(null);
                            Quaternion r = userTransform.getRotation(null);
                            chg.viewNode.setLocalRotation(r);
                            logger.fine("View node set rotation = " + r);
                            Vector3f t = userTransform.getTranslation(null);
                            chg.viewNode.setLocalTranslation(t);
                            logger.fine("View node set translation = " + t);
                            break;
                        }

                        case ATTACH_POINT_SET_ADD_ENTITY: {
                            SGChangeAttachPointSetAddEntity chg = (SGChangeAttachPointSetAddEntity) sgChange;
                            chg.rc.setAttachPoint(chg.node);

                            // Note: in the latest MTGame, addEntity makes the entity immediately visible.
                            // So, to avoid having the scene graph come up at the wrong place, we need
                            // to perform the addEntity after setting the attach point.
                            if (chg.parentEntity != null && chg.entity != null) {
                                chg.parentEntity.addEntity(chg.entity);
                            }
                            break;
                        }

                    }
                }


                // Propagate changes to JME
                if (viewNode != null) {
                    ClientContextJME.getWorldManager().addToUpdateList(viewNode);
                }

                sgChanges.clear();
            }
        }, null, true);
        // NOTE: it is critical that this render updater runs to completion before anything else happens
    }

    private enum SGChangeOp {

        GEOMETRY_ATTACH_TO_VIEW,
        GEOMETRY_DETACH_FROM_VIEW,
        GEOMETRY_SIZE_SET,
        GEOMETRY_TEX_COORDS_SET,
        GEOMETRY_TEXTURE_SET,
        GEOMETRY_ORTHO_Z_ORDER_SET,
        GEOMETRY_TRANSFORM_OFFSET_STACK_SET,
        GEOMETRY_CLEANUP,
        VIEW_NODE_ORTHO_SET,
        TRANSFORM_USER_SET,
        ATTACH_POINT_SET_ADD_ENTITY
    };

    //<editor-fold defaultstate="collapsed" desc="SGChange inner classes">
    private static class SGChange {

        private SGChangeOp op;

        private SGChange(SGChangeOp op) {
            this.op = op;
        }

        private SGChangeOp getOp() {
            return op;
        }
    }

    private static class SGChangeGeometryAttachToView extends SGChange {

        private Node viewNode;
        private GeometryNode geometryNode;

        private SGChangeGeometryAttachToView(Node viewNode, GeometryNode geometryNode) {
            super(SGChangeOp.GEOMETRY_ATTACH_TO_VIEW);
            this.viewNode = viewNode;
            this.geometryNode = geometryNode;
        }
    }

    private static class SGChangeGeometryDetachFromView extends SGChange {

        private Node viewNode;
        private GeometryNode geometryNode;

        private SGChangeGeometryDetachFromView(Node viewNode, GeometryNode geometryNode) {
            super(SGChangeOp.GEOMETRY_DETACH_FROM_VIEW);
            this.viewNode = viewNode;
            this.geometryNode = geometryNode;
        }
    }

    private static class SGChangeGeometrySizeSet extends SGChange {

        private GeometryNode geometryNode;
        private float width;
        private float height;

        private SGChangeGeometrySizeSet(GeometryNode geometryNode, float width, float height) {
            super(SGChangeOp.GEOMETRY_SIZE_SET);
            this.geometryNode = geometryNode;
            this.width = width;
            this.height = height;
        }
    }

    private static class SGChangeGeometryTexCoordsSet extends SGChange {

        private GeometryNode geometryNode;
        private float widthRatio;
        private float heightRatio;

        private SGChangeGeometryTexCoordsSet(GeometryNode geometryNode,
                float widthRatio, float heightRatio) {
            super(SGChangeOp.GEOMETRY_TEX_COORDS_SET);
            this.geometryNode = geometryNode;
            this.widthRatio = widthRatio;
            this.heightRatio = heightRatio;
        }
    }

    private static class SGChangeGeometryTextureSet extends SGChange {

        private GeometryNode geometryNode;
        private Texture2D texture;
        private DrawingSurface surface;

        private SGChangeGeometryTextureSet(GeometryNode geometryNode, Texture2D texture,
                DrawingSurface surface) {
            super(SGChangeOp.GEOMETRY_TEXTURE_SET);
            this.geometryNode = geometryNode;
            this.texture = texture;
            this.surface = surface;
        }
    }

    private static class SGChangeGeometryOrthoZOrderSet extends SGChange {

        private GeometryNode geometryNode;
        private int zOrder;

        private SGChangeGeometryOrthoZOrderSet(GeometryNode geometryNode, int zOrder) {
            super(SGChangeOp.GEOMETRY_ORTHO_Z_ORDER_SET);
            this.geometryNode = geometryNode;
            this.zOrder = zOrder;
        }
    }

    private static class SGChangeGeometryCleanup extends SGChange {

        private GeometryNode geometryNode;

        private SGChangeGeometryCleanup(GeometryNode geometryNode) {
            super(SGChangeOp.GEOMETRY_CLEANUP);
            this.geometryNode = geometryNode;
        }
    }

    private static class SGChangeViewNodeOrthoSet extends SGChange {

        private Node viewNode;
        private boolean ortho;

        private SGChangeViewNodeOrthoSet(Node viewNode, boolean ortho) {
            super(SGChangeOp.VIEW_NODE_ORTHO_SET);
            this.viewNode = viewNode;
            this.ortho = ortho;
        }
    }

    private static class SGChangeTransform extends SGChange {

        protected CellTransform transform;

        private SGChangeTransform(SGChangeOp op, CellTransform transform) {
            super(op);
            this.transform = transform;
        }
    }

    private static class SGChangeGeometryTransformOffsetStackSet extends SGChangeTransform {

        private GeometryNode geometryNode;

        private SGChangeGeometryTransformOffsetStackSet(GeometryNode geometryNode, CellTransform transform) {
            super(SGChangeOp.GEOMETRY_TRANSFORM_OFFSET_STACK_SET, transform);
            this.geometryNode = geometryNode;
        }
    }

    private static class SGChangeTransformUserSet extends SGChangeTransform {

        private Node viewNode;

        private SGChangeTransformUserSet(Node viewNode, CellTransform transform) {
            super(SGChangeOp.TRANSFORM_USER_SET, transform);
            this.viewNode = viewNode;
        }
    }

    private static class SGChangeAttachPointSetAddEntity extends SGChange {

        private RenderComponent rc;
        private Node node;
        private Entity parentEntity;
        private Entity entity;

        private SGChangeAttachPointSetAddEntity(RenderComponent rc, Node node, Entity parentEntity,
                Entity entity) {
            super(SGChangeOp.ATTACH_POINT_SET_ADD_ENTITY);
            this.rc = rc;
            this.node = node;
            this.parentEntity = parentEntity;
            this.entity = entity;
        }
    }
    //</editor-fold>
    // The list of scene graph changes (to be applied at the end of update).

    public synchronized void sgChangeGeometryAttachToView(Node viewNode, GeometryNode geometryNode) {
        sgChanges.add(new SGChangeGeometryAttachToView(viewNode, geometryNode));
    }

    public synchronized void sgChangeGeometryDetachFromView(Node viewNode, GeometryNode geometryNode) {
        sgChanges.add(new SGChangeGeometryDetachFromView(viewNode, geometryNode));
    }

    public synchronized void sgChangeGeometrySizeSet(GeometryNode geometryNode, float width, float height) {
        sgChanges.add(new SGChangeGeometrySizeSet(geometryNode, width, height));
    }

    public synchronized void sgChangeGeometryTexCoordsSet(GeometryNode geometryNode, float widthRatio,
            float heightRatio) {
        sgChanges.add(new SGChangeGeometryTexCoordsSet(geometryNode, widthRatio, heightRatio));
    }

    public synchronized void sgChangeGeometryTextureSet(GeometryNode geometryNode, Texture2D texture,
            DrawingSurface surface) {
        sgChanges.add(new SGChangeGeometryTextureSet(geometryNode, texture, surface));
    }

    public synchronized void sgChangeGeometryOrthoZOrderSet(GeometryNode geometryNode, int zOrder) {
        sgChanges.add(new SGChangeGeometryOrthoZOrderSet(geometryNode, zOrder));
    }

    public synchronized void sgChangeGeometryCleanup(GeometryNode geometryNode) {
        sgChanges.add(new SGChangeGeometryCleanup(geometryNode));
    }

    public synchronized void sgChangeViewNodeOrthoSet(Node viewNode, boolean ortho) {
        sgChanges.add(new SGChangeViewNodeOrthoSet(viewNode, ortho));
    }

    public synchronized void sgChangeGeometryTransformOffsetStackSet(GeometryNode geometryNode,
            CellTransform transform) {
        sgChanges.add(new SGChangeGeometryTransformOffsetStackSet(geometryNode, transform));
    }

    public synchronized void sgChangeTransformUserSet(Node viewNode, CellTransform transform) {
        sgChanges.add(new SGChangeTransformUserSet(viewNode, transform));
    }

    public synchronized void sgChangeAttachPointSetAddEntity(RenderComponent rc, Node node,
            Entity parentEntity, Entity entity) {
        sgChanges.add(new SGChangeAttachPointSetAddEntity(rc, node, parentEntity, entity));
    }
}
