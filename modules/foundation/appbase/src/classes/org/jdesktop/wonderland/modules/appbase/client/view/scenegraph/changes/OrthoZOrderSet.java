/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jdesktop.wonderland.modules.appbase.client.view.scenegraph.changes;

import org.jdesktop.wonderland.modules.appbase.client.view.GeometryNode;
import org.jdesktop.wonderland.modules.appbase.client.view.View2DEntity;
import org.jdesktop.wonderland.modules.appbase.client.view.scenegraph.SceneGraphChange;
import static org.jdesktop.wonderland.modules.appbase.client.view.scenegraph.AppSceneGraphUtils.SceneGraphChangeOperation.*;

/**
 *
 * @author Ryan
 */
public class OrthoZOrderSet extends SceneGraphChange {

    private GeometryNode geometryNode;
    private int zOrder;

    public OrthoZOrderSet(GeometryNode geometryNode, int zOrder) {
        super(GEOMETRY_ORTHO_Z_ORDER_SET);
        this.geometryNode = geometryNode;
        this.zOrder = zOrder;
    }

    public GeometryNode getGeometryNode() {
        return geometryNode;
    }

    public int getzOrder() {
        return zOrder;
    }
}
