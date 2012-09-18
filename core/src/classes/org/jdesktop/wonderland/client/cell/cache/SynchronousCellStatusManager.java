/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jdesktop.wonderland.client.cell.cache;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.common.cell.CellStatus;

/**
 *
 * @author Ryan
 */
public class SynchronousCellStatusManager implements CellStatusManager {

    /**
     * executor for modifying values in separate threads
     */
    private final ExecutorService cacheExecutor =
            Executors.newSingleThreadExecutor(new StatusCellFactory());
    private final CellCacheBasicImpl cache;

    public SynchronousCellStatusManager(CellCacheBasicImpl cache) {
        this.cache = cache;
    }

    public void setCellStatus(Cell cell, CellStatus status) {
        cacheExecutor.submit(new CellStatusChanger(cache, cell, status));
    }
}
