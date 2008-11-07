/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.server.spatial;

import com.jme.bounding.BoundingVolume;
import org.jdesktop.wonderland.common.cell.CellTransform;
import org.jdesktop.wonderland.server.cell.CellMO;
import org.jdesktop.wonderland.server.cell.TransformChangeListenerSrv;
import org.jdesktop.wonderland.server.cell.view.ViewCellMO;

/**
 *
 * @author paulby
 */
public class UniverseServiceManagerImpl implements UniverseServiceManager {

    private UniverseService service;

    public UniverseServiceManagerImpl(UniverseService service) {
        this.service = service;
    }

    public void addChild(CellMO parent, CellMO child) {
        service.addChild(parent, child);
    }

    public void createCell(CellMO cellMO) {
        service.createCell(cellMO);
    }

    public void removeCell(CellMO cellMO) {
        service.removeCell(cellMO);
    }

    public void removeChild(CellMO parent, CellMO child) {
        service.removeChild(parent, child);
    }

    public void addRootToUniverse(CellMO rootCellMO) {
        service.addRootToUniverse(rootCellMO);
    }

    public void removeRootFromUniverse(CellMO rootCellMO) {
        service.removeRootFromUniverse(rootCellMO);
    }

    public void setLocalTransform(CellMO cell, CellTransform localTransform) {
        service.setLocalTransform(cell, localTransform);
    }

    public CellTransform getWorldTransform(CellMO cell, CellTransform result) {
        return service.getWorldTransform(cell, result);
    }

    public BoundingVolume getWorldBounds(CellMO cell, BoundingVolume result) {
        return service.getWorldBounds(cell, result);
    }

    public void viewLogin(ViewCellMO viewCell) {
        service.viewLogin(viewCell);
    }

    public void viewLogout(ViewCellMO viewCell) {
        service.viewLogout(viewCell);
    }

    public void addTransformChangeListener(CellMO cell, TransformChangeListenerSrv listener) {
        service.addTransformChangeListener(cell, listener);
    }

    public void removeTransformChangeListener(CellMO cell, TransformChangeListenerSrv listener) {
        service.removeTransformChangeListener(cell, listener);
    }
}
