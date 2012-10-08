/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jdesktop.wonderland.modules.appbase.client.view.scenegraph.changes;

import com.jme.scene.Node;
import org.jdesktop.wonderland.modules.appbase.client.view.View2DEntity;
import org.jdesktop.wonderland.modules.appbase.client.view.scenegraph.SceneGraphChange;
import static org.jdesktop.wonderland.modules.appbase.client.view.scenegraph.AppSceneGraphUtils.SceneGraphChangeOperation.*;
/**
 *
 * @author Ryan
 */
public class ViewNodeOrthoSet extends SceneGraphChange {

    private Node viewNode;
    private boolean ortho;

    public ViewNodeOrthoSet(Node viewNode, boolean ortho) {
        super(VIEW_NODE_ORTHO_SET);
        this.viewNode = viewNode;
        this.ortho = ortho;
    }

    public boolean isOrtho() {
        return ortho;
    }

    public Node getViewNode() {
        return viewNode;
    }
    
    
}
