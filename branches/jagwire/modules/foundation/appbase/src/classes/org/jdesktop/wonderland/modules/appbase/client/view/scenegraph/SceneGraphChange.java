/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jdesktop.wonderland.modules.appbase.client.view.scenegraph;

import org.jdesktop.wonderland.modules.appbase.client.view.scenegraph.AppSceneGraphUtils.SceneGraphChangeOperation;

/**
 *
 * @author Ryan
 */
public class SceneGraphChange {

    private SceneGraphChangeOperation op;

    public SceneGraphChange(SceneGraphChangeOperation operation) {
        this.op = operation;
    }

    public SceneGraphChangeOperation getOperation() {
        return this.op;
    }
}
