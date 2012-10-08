/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jdesktop.wonderland.modules.appbase.client.view.scenegraph.changes;

import com.jme.scene.Node;
import org.jdesktop.wonderland.modules.appbase.client.view.GeometryNode;
import static org.jdesktop.wonderland.modules.appbase.client.view.scenegraph.AppSceneGraphUtils.SceneGraphChangeOperation.*;
import org.jdesktop.wonderland.modules.appbase.client.view.scenegraph.SceneGraphChange;

/**
 *
 * @author JagWire
 */
public class DetachFromView extends SceneGraphChange {

    private final Node viewNode;
    private final GeometryNode geometryNode;

    public DetachFromView(Node viewNode, GeometryNode geometryNode) {
        super(GEOMETRY_DETACH_FROM_VIEW);
        this.viewNode = viewNode;
        this.geometryNode = geometryNode;
    }

    public GeometryNode getGeometryNode() {
        return geometryNode;
    }

    public Node getViewNode() {
        return viewNode;
    }
}
