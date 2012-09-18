/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jdesktop.wonderland.client.cell.cache;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.jdesktop.wonderland.client.cell.AssetPreloader;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.cell.CellChildrenChangeListener;
import org.jdesktop.wonderland.client.cell.CellManager;
import org.jdesktop.wonderland.client.cell.CellStatusChangeListener;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.CellStatus;

/**
 *
 * @author Ryan
 */
public class ParallelCellStatusManager implements CellStatusChangeListener, CellChildrenChangeListener,
        CellStatusManager {

    private final Map<CellID, CellStatusEntry> waiting =
            new LinkedHashMap<CellID, CellStatusEntry>();
    private final ExecutorService downloader =
            Executors.newCachedThreadPool(new URLDownloadCellFactory());
    private final ExecutorService executor =
            Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors(),
            new StatusCellFactory());
    private final boolean preloadAssets;
    private final CellCacheBasicImpl cache;
    private static final String ASSET_PRELOAD_PROP =
            CellCacheBasicImpl.class.getSimpleName() + ".PreloadAssets";

    public ParallelCellStatusManager(CellCacheBasicImpl cache) {
        this.cache = cache;
        CellManager.getCellManager().addCellStatusChangeListener(this);

        preloadAssets = Boolean.parseBoolean(
                System.getProperty(ASSET_PRELOAD_PROP, "true"));
    }

    public synchronized void setCellStatus(Cell cell, CellStatus status) {
        // create a status entry for the cell
        CellStatusEntry entry = new CellStatusEntry(cell, status);

        // does the cell have resources to download?
        if (preloadAssets
                && cell instanceof AssetPreloader
                && cell.getStatus() == CellStatus.DISK) {
            entry.setDownloaded(false);
        }

        // is the cell ready now?
        if (eligible(entry)) {
            executor.submit(new CellStatusChanger(cache, entry.getCell(), entry.getStatus()));
            return;
        }

        // if the cell is not ready, add it to our map of
        // waiting cells
        waiting.put(cell.getCellID(), entry);

        // listen for child changes
        cell.addChildrenChangeListener(this);

        // if the cell needs downloading, start now
        if (!entry.isDownloaded()) {
            startDownloading(entry);
        }
    }

    /**
     * Start downloading files associated with the given entry
     *
     * @param entry the entry to download files for
     */
    private void startDownloading(CellStatusEntry entry) {
        downloader.submit(new URLDownloader(cache, this, entry));
    }

    /**
     * Notification that a cell has finished downloading
     *
     * @param cellID the id of the cell that finished
     */
    public synchronized void finishedDownloading(CellID cellID) {
        CellStatusEntry entry = waiting.get(cellID);
        if (entry != null) {
            entry.setDownloaded(true);
        }

        // see if the cell is now eligible to load
        checkCell(cellID);
    }

    /**
     * When a cell's status changes, check this cell and any parents or children
     * that are in the waiting list
     *
     * @param cell the cell that changed
     * @param status the updated status
     */
    public void cellStatusChanged(Cell cell, CellStatus status) {
        // check this cell
        checkCell(cell.getCellID());

        // check the cell's parent (if any)
        if (cell.getParent() != null) {
            checkCell(cell.getParent().getCellID());
        }

        // check the cell's children (if any)
        if (cell.getChildren() != null) {
            for (Cell child : cell.getChildren()) {
                checkCell(child.getCellID());
            }
        }

    }

    /**
     * When a cell's children change, it may become eligible for a status
     * change. Check.
     */
    public void childAdded(Cell cell, Cell child) {
        checkCell(cell.getCellID());
        checkCell(child.getCellID());
    }

    /**
     * When a cell's children change, it may become eligible for a status
     * change. Check.
     */
    public void childRemoved(Cell cell, Cell child) {
        checkCell(cell.getCellID());
        checkCell(child.getCellID());
    }

    /**
     * Check a particular entry in the map, and update it as needed
     *
     * @param cellID this id of the cell to check
     */
    private synchronized void checkCell(CellID cellID) {
        CellStatusEntry entry = waiting.get(cellID);
        if (entry != null && eligible(entry)) {
            waiting.remove(cellID);

            // stop listening for child changes
            entry.getCell().removeChildChangeListener(this);

            executor.submit(new CellStatusChanger(cache, entry.getCell(), entry.getStatus()));
        }
    }

    /**
     * Return true if a cell is eligible to move to the status change queue, or
     * false if not
     *
     * @param entry the status entry describing the cell
     */
    private boolean eligible(CellStatusEntry entry) {
        // does the content of the cell need to be downloaded
        if (!entry.isDownloaded()) {
            return false;
        }

        // is the cell's target state an increase or decrease from
        // the current state?
        boolean increasing = (entry.getCell().getStatus().ordinal()
                <= entry.getStatus().ordinal());
        if (increasing) {
            // a cell is eligible for an increase if the parent's status
            // is at least the target
            Cell parent = entry.getCell().getParent();
            if (parent != null) {
                return parent.getStatus().ordinal()
                        >= entry.getStatus().ordinal();
            }
        } else {
            // a cell is eligible for a decrease if all of the children's
            // statuses are lower than the target
            List<Cell> children = entry.getCell().getChildren();
            if (children != null) {
                for (Cell child : children) {
                    if (child.getStatus().ordinal() > entry.getStatus().ordinal()) {
                        return false;
                    }
                }
            }
        }

        // if we passed all the checks above, we are eligible
        return true;
    }

    public ExecutorService getDownloader() {
        return downloader;
    }
}
