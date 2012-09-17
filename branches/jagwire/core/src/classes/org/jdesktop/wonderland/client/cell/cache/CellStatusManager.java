/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jdesktop.wonderland.client.cell.cache;

import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.common.cell.CellStatus;

/**
 *
 * @author Ryan
 */
public interface CellStatusManager {

    public void setCellStatus(Cell cell, CellStatus status);
}
