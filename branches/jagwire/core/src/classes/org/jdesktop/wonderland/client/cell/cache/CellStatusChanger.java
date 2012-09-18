/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jdesktop.wonderland.client.cell.cache;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.cell.CellStatistics;
import org.jdesktop.wonderland.common.cell.CellStatus;

/**
 *
 * @author JagWire
 */
public class CellStatusChanger implements Runnable {

    private final Cell cell;
    private final CellStatus cellStatus;
    private static final Logger logger = Logger.getLogger(CellStatusChanger.class.getName());
    private final CellCacheBasicImpl cache;

    public CellStatusChanger(CellCacheBasicImpl cache, Cell cell, CellStatus status) {
        this.cell = cell;
        this.cellStatus = status;
        this.cache = cache;
    }

    public void run() {
        try {
            // set the currently active cell
            currentCell().set(cell);

            setCellStatus(cell, cellStatus);
        } catch (Throwable t) {
            // Report the exception, otherwise it will get swallowed
            logger.log(Level.WARNING, "Exception thrown in Cell.setStatus " + t.getLocalizedMessage(), t);
        } finally {
            currentCell().remove();
        }
    }

    private ThreadLocal<Cell> currentCell() {
        return cache.getCurrentCell();
    }

    /**
     * Set the cell status, ensuring that the cell passes through any
     * intermediate status.
     *
     * @param cell
     * @param status
     */
    private void setCellStatus(Cell cell, CellStatus status) {
        logger.fine("Set status of cell " + cell.getCellID()
                + " to " + status);

        synchronized (cell) {
            int currentStatus = cell.getStatus().ordinal();
            int requiredStatus = status.ordinal();

            if (currentStatus == requiredStatus) {
                return;
            }

            int dir = (requiredStatus > currentStatus ? 1 : -1);
            boolean increasing = (dir == 1);

            while (currentStatus != requiredStatus) {
                currentStatus += dir;

                CellStatus nextStatus = CellStatus.values()[currentStatus];
                long startTime = System.currentTimeMillis();
                CellStatistics.TimeCellStat loadStat = getLoadStat(cell, nextStatus);
                try {
                    cell.setStatus(nextStatus, increasing);
                } finally {
                    long time = System.currentTimeMillis() - startTime;
                    loadStat.changeValue(time);
                }

                cell.fireCellStatusChanged(nextStatus);
            }
        }
    }

    private CellStatistics.TimeCellStat getLoadStat(Cell cell, CellStatus status) {
        String statId = status.name() + "-time";
        CellStatistics.TimeCellStat loadStat;

        synchronized (cache.getStatistics()) {
            loadStat = (CellStatistics.TimeCellStat) cache.getStatistics().get(cell, statId);
            if (loadStat == null) {
                loadStat = new CellStatistics.TimeCellStat(statId);
                cache.getStatistics().add(cell, loadStat);
            }
        }

        return loadStat;
    }
}
