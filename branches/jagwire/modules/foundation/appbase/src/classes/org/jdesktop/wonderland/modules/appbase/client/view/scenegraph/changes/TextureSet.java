/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jdesktop.wonderland.modules.appbase.client.view.scenegraph.changes;

import com.jme.image.Texture2D;
import org.jdesktop.wonderland.modules.appbase.client.DrawingSurface;
import org.jdesktop.wonderland.modules.appbase.client.view.GeometryNode;
import org.jdesktop.wonderland.modules.appbase.client.view.View2DEntity;
import org.jdesktop.wonderland.modules.appbase.client.view.scenegraph.SceneGraphChange;
import static org.jdesktop.wonderland.modules.appbase.client.view.scenegraph.AppSceneGraphUtils.SceneGraphChangeOperation.*;

/**
 *
 * @author Ryan
 */
public class TextureSet extends SceneGraphChange {

    private GeometryNode geometryNode;
    private Texture2D texture;
    private DrawingSurface surface;

    public TextureSet(GeometryNode geometryNode, Texture2D texture,
            DrawingSurface surface) {
        super(GEOMETRY_TEXTURE_SET);
        this.geometryNode = geometryNode;
        this.texture = texture;
        this.surface = surface;
    }

    public GeometryNode getGeometryNode() {
        return geometryNode;
    }

    public DrawingSurface getSurface() {
        return surface;
    }

    public Texture2D getTexture() {
        return texture;
    }
}
