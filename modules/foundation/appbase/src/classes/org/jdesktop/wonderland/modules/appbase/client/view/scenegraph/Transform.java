/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jdesktop.wonderland.modules.appbase.client.view.scenegraph;

import org.jdesktop.wonderland.common.cell.CellTransform;
import org.jdesktop.wonderland.modules.appbase.client.view.scenegraph.AppSceneGraphUtils.SceneGraphChangeOperation;

/**
 *
 * @author Ryan
 */
public class Transform extends SceneGraphChange {
    protected CellTransform transform;
    
    public Transform(SceneGraphChangeOperation op, CellTransform transform) {
        super(op);
        this.transform = transform;
    }

    public CellTransform getTransform() {
        return transform;
    }
    
    
}
