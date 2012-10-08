/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jdesktop.wonderland.modules.appbase.client.view.scenegraph;

/**
 *
 * @author JagWire
 */
public class AppSceneGraphUtils {

    public static enum SceneGraphChangeOperation {

        GEOMETRY_ATTACH_TO_VIEW,
        GEOMETRY_DETACH_FROM_VIEW,
        GEOMETRY_SIZE_SET,
        GEOMETRY_TEX_COORDS_SET,
        GEOMETRY_TEXTURE_SET,
        GEOMETRY_ORTHO_Z_ORDER_SET,
        GEOMETRY_TRANSFORM_OFFSET_STACK_SET,
        GEOMETRY_CLEANUP,
        VIEW_NODE_ORTHO_SET,
        TRANSFORM_USER_SET,
        ATTACH_POINT_SET_ADD_ENTITY
    };
}
