/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jdesktop.wonderland.modules.appbase.client.view.scenegraph.changes.transform;

import com.jme.scene.Node;
import org.jdesktop.wonderland.common.cell.CellTransform;
import org.jdesktop.wonderland.modules.appbase.client.view.scenegraph.Transform;
import static org.jdesktop.wonderland.modules.appbase.client.view.scenegraph.AppSceneGraphUtils.SceneGraphChangeOperation.*;
/**
 *
 * @author Ryan
 */
public class UserSet extends Transform {
    private Node viewNode;
    public UserSet(Node viewNode, CellTransform transform) {
        super(TRANSFORM_USER_SET, transform);
        this.viewNode = viewNode;
    }

    public Node getViewNode() {
        return viewNode;
    }
    
    
}
