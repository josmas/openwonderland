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
public class TextureCoordinateSet extends SceneGraphChange {

    private GeometryNode geometryNode;
    private float widthRatio;
    private float heightRatio;

    public TextureCoordinateSet(GeometryNode geometryNode,
            float widthRatio, float heightRatio) {
        super(GEOMETRY_TEX_COORDS_SET);
        this.geometryNode = geometryNode;
        this.widthRatio = widthRatio;
        this.heightRatio = heightRatio;
    }

    public GeometryNode getGeometryNode() {
        return geometryNode;
    }

    public float getHeightRatio() {
        return heightRatio;
    }

    public float getWidthRatio() {
        return widthRatio;
    }
}
