/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jdesktop.wonderland.modules.appbase.client.view.scenegraph;

//import com.jme.entity.Entity;
import com.jme.image.Texture2D;
import com.jme.scene.Node;
import org.jdesktop.mtgame.Entity;
//import com.jme.scene.Node;

import org.jdesktop.mtgame.RenderComponent;
import org.jdesktop.wonderland.common.cell.CellTransform;
import org.jdesktop.wonderland.modules.appbase.client.DrawingSurface;
import org.jdesktop.wonderland.modules.appbase.client.view.GeometryNode;
import org.jdesktop.wonderland.modules.appbase.client.view.scenegraph.changes.*;
import org.jdesktop.wonderland.modules.appbase.client.view.scenegraph.changes.transform.OffsetStackSet;
import org.jdesktop.wonderland.modules.appbase.client.view.scenegraph.changes.transform.UserSet;

/**
 *
 * @author JagWire
 */
public class ChangeFactory {

    public static synchronized SceneGraphChange attachToView(Node viewNode, GeometryNode geometryNode) {
        return new AttachToView(viewNode, geometryNode);
    }

    public static synchronized SceneGraphChange detachFromView(Node viewNode, GeometryNode geometryNode) {
        return new DetachFromView(viewNode, geometryNode);
    }

    public static synchronized SceneGraphChange sizeSet(GeometryNode geometryNode,
            float width,
            float height) {
        return new SizeSet(geometryNode, width, height);
    }

    public static synchronized SceneGraphChange textureCoordinateSet(GeometryNode geometryNode,
            float widthRatio,
            float heightRatio) {
        return new TextureCoordinateSet(geometryNode, widthRatio, heightRatio);
    }

    public static synchronized SceneGraphChange textureSet(GeometryNode geometryNode,
            Texture2D texture,
            DrawingSurface surface) {
        return new TextureSet(geometryNode, texture, surface);
    }

    public static synchronized SceneGraphChange orthoZOrderSet(GeometryNode geometryNode, int zOrder) {
        return new OrthoZOrderSet(geometryNode, zOrder);
    }

    public static synchronized SceneGraphChange cleanup(GeometryNode node) {
        return new Cleanup(node);
    }

    public static synchronized SceneGraphChange viewNodeOrthoSet(Node viewNode, boolean ortho) {
        return new ViewNodeOrthoSet(viewNode, ortho);
    }

    public static synchronized SceneGraphChange offsetStackSet(GeometryNode node,
            CellTransform transform) {
        return new OffsetStackSet(node, transform);
    }

    public static synchronized SceneGraphChange userSet(Node viewNode, CellTransform transform) {
        return new UserSet(viewNode, transform);
    }

    public static synchronized SceneGraphChange attachPointSetAddEntity(RenderComponent rc,
            Node node,
            Entity parent,
            Entity entity) {
        return new AttachPointSetAddEntity(rc, node, parent, entity);
    }
}
