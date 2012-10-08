/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jdesktop.wonderland.modules.appbase.client.view.scenegraph.changes;

import com.jme.scene.Node;
import org.jdesktop.wonderland.modules.appbase.client.view.GeometryNode;
import org.jdesktop.wonderland.modules.appbase.client.view.scenegraph.SceneGraphChange;
import static org.jdesktop.wonderland.modules.appbase.client.view.scenegraph.AppSceneGraphUtils.SceneGraphChangeOperation.*;

/**
 *
 * @author JagWire
 */
public class AttachToView extends SceneGraphChange {

    private Node viewNode;
    private GeometryNode geometryNode;

    public AttachToView(Node viewNode, GeometryNode geometryNode) {
        super(GEOMETRY_ATTACH_TO_VIEW);
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
