/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jdesktop.wonderland.modules.appbase.client.view.scenegraph.changes;

import org.jdesktop.wonderland.modules.appbase.client.view.GeometryNode;
import org.jdesktop.wonderland.modules.appbase.client.view.View2DEntity;
import static org.jdesktop.wonderland.modules.appbase.client.view.scenegraph.AppSceneGraphUtils.SceneGraphChangeOperation.*;
import org.jdesktop.wonderland.modules.appbase.client.view.scenegraph.SceneGraphChange;

/**
 *
 * @author Ryan
 */
public class Cleanup extends SceneGraphChange {

    private GeometryNode geometryNode;

    public Cleanup(GeometryNode geometryNode) {
        super(GEOMETRY_CLEANUP);
        this.geometryNode = geometryNode;
    }

    public GeometryNode getGeometryNode() {
        return geometryNode;
    }
    
    
}
