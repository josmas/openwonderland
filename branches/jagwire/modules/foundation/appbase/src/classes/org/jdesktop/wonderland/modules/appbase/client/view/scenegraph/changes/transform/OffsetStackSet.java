/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jdesktop.wonderland.modules.appbase.client.view.scenegraph.changes.transform;

import org.jdesktop.wonderland.common.cell.CellTransform;
import org.jdesktop.wonderland.modules.appbase.client.view.GeometryNode;
import org.jdesktop.wonderland.modules.appbase.client.view.scenegraph.Transform;
import static org.jdesktop.wonderland.modules.appbase.client.view.scenegraph.AppSceneGraphUtils.SceneGraphChangeOperation.*;
/**
 *
 * @author Ryan
 */
public class OffsetStackSet extends Transform {
    private GeometryNode geometryNode;
    public OffsetStackSet(GeometryNode geometryNode, CellTransform transform) {
        super(GEOMETRY_TRANSFORM_OFFSET_STACK_SET, transform);
        this.geometryNode = geometryNode;
    }

    public GeometryNode getGeometryNode() {
        return geometryNode;
    }
    
    
}
