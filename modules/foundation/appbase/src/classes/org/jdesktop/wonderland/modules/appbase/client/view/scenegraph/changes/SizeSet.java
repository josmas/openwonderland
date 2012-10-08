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
public class SizeSet extends SceneGraphChange {

    private GeometryNode geometryNode;
    private float width;
    private float height;

    public SizeSet(GeometryNode geometryNode, float width, float height) {
        super(GEOMETRY_SIZE_SET);
        this.geometryNode = geometryNode;
        this.width = width;
        this.height = height;
    }

    public GeometryNode getGeometryNode() {
        return geometryNode;
    }

    public float getHeight() {
        return height;
    }

    public float getWidth() {
        return width;
    }
}
