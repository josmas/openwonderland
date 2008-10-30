/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.server.spatial.impl;

import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.server.spatial.SpatialCell;

/**
 *
 * @author paulby
 */
public class ViewCellImpl extends SpatialCellImpl {

    private ViewCache viewCache = null;

    public ViewCellImpl(CellID id, SpaceManager spaceManager) {
        super(id);
        viewCache = new ViewCache(this, spaceManager);
    }

    public ViewCache getViewCache() {
        return viewCache;
    }

    @Override
    public void setRoot(SpatialCell root) {
        super.setRoot(root);
        viewCache.cellMoved(this, getWorldTransform());
    }
}
