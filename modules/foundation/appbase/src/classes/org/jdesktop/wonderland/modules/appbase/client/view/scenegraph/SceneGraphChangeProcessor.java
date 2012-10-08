/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jdesktop.wonderland.modules.appbase.client.view.scenegraph;

import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import com.jme.scene.Spatial;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;
import org.jdesktop.mtgame.RenderUpdater;
import org.jdesktop.wonderland.client.jme.ClientContextJME;
import org.jdesktop.wonderland.common.cell.CellTransform;
import org.jdesktop.wonderland.modules.appbase.client.DrawingSurface;
import org.jdesktop.wonderland.modules.appbase.client.view.View2DEntity;
import org.jdesktop.wonderland.modules.appbase.client.view.scenegraph.changes.*;
import org.jdesktop.wonderland.modules.appbase.client.view.scenegraph.changes.transform.OffsetStackSet;
import org.jdesktop.wonderland.modules.appbase.client.view.scenegraph.changes.transform.UserSet;

/**
 *
 * @author JagWire
 */
public class SceneGraphChangeProcessor {

    private List<SceneGraphChange> changes;
    private View2DEntity viewEntity;
    private static final Logger logger = Logger.getLogger(SceneGraphChangeProcessor.class.getName());

    public SceneGraphChangeProcessor(View2DEntity viewEntity,
                                     List<SceneGraphChange> changes) {
        this.changes = changes;
        this.viewEntity = viewEntity;
    }

    public void processChanges() {
        if (changes.size() <= 0) {
            return;
        }

        ClientContextJME.getWorldManager().addRenderUpdater(_update(), null, true);
        // NOTE: it is critical that this render updater runs to completion before anything else happens
    }

    private RenderUpdater _update() {
        return new RenderUpdater() {

            public void update(Object arg0) {
                for (SceneGraphChange change : changes) {
                    switch (change.getOperation()) {
                        case GEOMETRY_ATTACH_TO_VIEW: {
                            AttachToView chg = (AttachToView) change;
                            chg.getViewNode().attachChild(chg.getGeometryNode());
                            break;
                        }

                        case GEOMETRY_DETACH_FROM_VIEW: {
                            DetachFromView chg = (DetachFromView) change;
                            chg.getViewNode().detachChild(chg.getGeometryNode());
                            break;
                        }

                        case GEOMETRY_SIZE_SET: {
//                            SGChangeGeometrySizeSet chg = (SGChangeGeometrySizeSet) sgChange;
                            SizeSet chg = (SizeSet) change;
                            chg.getGeometryNode().setSize(chg.getWidth(), chg.getHeight());
                            viewEntity.forceTextureIdAssignment(true);
                            break;
                        }

                        case GEOMETRY_TEX_COORDS_SET: {
//                            SGChangeGeometryTexCoordsSet chg = (SGChangeGeometryTexCoordsSet) sgChange;
                            TextureCoordinateSet chg = (TextureCoordinateSet) change;
                            chg.getGeometryNode().setTexCoords(chg.getWidthRatio(), chg.getHeightRatio());
                            break;
                        }

                        case GEOMETRY_TEXTURE_SET: {
//                            SGChangeGeometryTextureSet chg = (SGChangeGeometryTextureSet) sgChange;
                            TextureSet chg = (TextureSet) change;
                            DrawingSurface surface = chg.getSurface();
                            boolean restoreUpdating = false;
                            if (surface.getUpdateEnable()) {
                                surface.setUpdateEnable(false);
                                restoreUpdating = true;
                            }
                            chg.getGeometryNode().setTexture(chg.getTexture());

                            if (restoreUpdating) {
                                surface.setUpdateEnable(true);
                            }

                            break;
                        }

                        case GEOMETRY_ORTHO_Z_ORDER_SET: {
//                            SGChangeGeometryOrthoZOrderSet chg = (SGChangeGeometryOrthoZOrderSet) sgChange;
                            OrthoZOrderSet chg = (OrthoZOrderSet) change;
                            if (chg.getGeometryNode() != null) {
                                chg.getGeometryNode().setOrthoZOrder(chg.getzOrder());
                            }
                            break;
                        }

                        case GEOMETRY_CLEANUP: {
//                            SGChangeGeometryCleanup chg = (SGChangeGeometryCleanup) sgChange;
                            Cleanup chg = (Cleanup) change;
                            chg.getGeometryNode().cleanup();
                            logger.fine("Geometry node cleanup");
                            break;
                        }

                        case VIEW_NODE_ORTHO_SET: {
//                            SGChangeViewNodeOrthoSet chg = (SGChangeViewNodeOrthoSet) sgChange;
                            ViewNodeOrthoSet chg = (ViewNodeOrthoSet) change;
                            if (chg.isOrtho()) {
                                chg.getViewNode().setCullHint(Spatial.CullHint.Never);
                            } else {
                                chg.getViewNode().setCullHint(Spatial.CullHint.Inherit);
                            }
                            break;
                        }

                        case GEOMETRY_TRANSFORM_OFFSET_STACK_SET: {
                            // The offset/stack transform resides in the geometry
//                            SGChangeGeometryTransformOffsetStackSet chg =
//                                    (SGChangeGeometryTransformOffsetStackSet) sgChange;
                            OffsetStackSet chg = (OffsetStackSet) change;
                            chg.getGeometryNode().setTransform(chg.getTransform());

                            break;
                        }

                        case TRANSFORM_USER_SET: {
//                            SGChangeTransformUserSet chg = (SGChangeTransformUserSet) sgChange;
                            UserSet chg = (UserSet) change;
                            CellTransform userTransform = chg.getTransform().clone(null);
                            Quaternion r = userTransform.getRotation(null);
                            chg.getViewNode().setLocalRotation(r);

                            Vector3f t = userTransform.getTranslation(null);
                            chg.getViewNode().setLocalTranslation(t);
                            break;
                        }

                        case ATTACH_POINT_SET_ADD_ENTITY: {
//                            SGChangeAttachPointSetAddEntity chg = (SGChangeAttachPointSetAddEntity) sgChange;
                            AttachPointSetAddEntity chg = (AttachPointSetAddEntity) change;
                            chg.getRc().setAttachPoint(chg.getNode());

                            // Note: in the latest MTGame, addEntity makes the entity immediately visible.
                            // So, to avoid having the scene graph come up at the wrong place, we need
                            // to perform the addEntity after setting the attach point.
                            if (chg.getParentEntity() != null && chg.getEntity() != null) {
                                chg.getParentEntity().addEntity(chg.getEntity());
                            }
                            break;
                        }
                    }
                }
                
                // Propagate changes to JME
                if(viewEntity.getViewNode() != null) {
                    ClientContextJME.getWorldManager().addToUpdateList(viewEntity.getViewNode());
                }
                
                changes.clear();
            }
        };
    }
}
