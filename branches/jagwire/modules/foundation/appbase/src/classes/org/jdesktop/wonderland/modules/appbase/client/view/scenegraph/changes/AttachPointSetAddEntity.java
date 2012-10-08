/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jdesktop.wonderland.modules.appbase.client.view.scenegraph.changes;

import com.jme.scene.Node;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.mtgame.RenderComponent;
import org.jdesktop.wonderland.modules.appbase.client.view.View2DEntity;
import org.jdesktop.wonderland.modules.appbase.client.view.scenegraph.SceneGraphChange;
import static org.jdesktop.wonderland.modules.appbase.client.view.scenegraph.AppSceneGraphUtils.SceneGraphChangeOperation.*;

/**
 *
 * @author Ryan
 */
public class AttachPointSetAddEntity extends SceneGraphChange {

    private RenderComponent rc;
    private Node node;
    private Entity parentEntity;
    private Entity entity;

    public AttachPointSetAddEntity(RenderComponent rc, Node node, Entity parentEntity,
            Entity entity) {
        super(ATTACH_POINT_SET_ADD_ENTITY);
        this.rc = rc;
        this.node = node;
        this.parentEntity = parentEntity;
        this.entity = entity;
    }

    public Entity getEntity() {
        return entity;
    }

    public Node getNode() {
        return node;
    }

    public Entity getParentEntity() {
        return parentEntity;
    }

    public RenderComponent getRc() {
        return rc;
    }
}
