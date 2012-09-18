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
public class CellStatusEntry {

    private final Cell cell;
    private final CellStatus status;
    private boolean downloaded = true;

    public CellStatusEntry(Cell cell, CellStatus status) {
        this.cell = cell;
        this.status = status;
    }

    public Cell getCell() {
        return cell;
    }

    public CellStatus getStatus() {
        return status;
    }

    public synchronized boolean isDownloaded() {
        return downloaded;
    }

    public synchronized void setDownloaded(boolean downloaded) {
        this.downloaded = downloaded;
    }
}
