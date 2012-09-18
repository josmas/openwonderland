/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jdesktop.wonderland.client.cell.cache;

import java.io.InputStream;
import java.net.URL;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.wonderland.client.cell.AssetPreloader;
import org.jdesktop.wonderland.client.cell.Cell;

/**
 *
 * @author Ryan
 */
public class URLDownloader implements Runnable {

    private final ParallelCellStatusManager pcsm;
    private final CellStatusEntry entry;
    private final CellCacheBasicImpl cache;

    private static final Logger logger = Logger.getLogger(URLDownloader.class.getName());
    
    public URLDownloader(CellCacheBasicImpl cache,
            ParallelCellStatusManager pcsm,
            CellStatusEntry entry) {
        this.pcsm = pcsm;
        this.entry = entry;
        this.cache = cache;
    }

    public ThreadLocal<Cell> currentCell() {
        return cache.getCurrentCell();
    }

    public void run() {
        Queue<Future<InputStream>> queue = new LinkedList<Future<InputStream>>();

        try {
            currentCell().set(entry.getCell());

            for (URL u : ((AssetPreloader) entry.getCell()).getAssets()) {
                enqueue(entry.getCell(), u, queue);
            }

            Future<InputStream> f = null;
            while ((f = queue.poll()) != null) {
                // wait for the object to be done loading
                try {
                    f.get();
                } catch (InterruptedException ie) {
                    // ignore
                } catch (ExecutionException ee) {
                    logger.log(Level.WARNING, "Error in execution", ee);
                }
            }

            // all done
            pcsm.finishedDownloading(entry.getCell().getCellID());
        } finally {
            currentCell().set(null);
        }
    }

    private void enqueue(final Cell cell, final URL u,
            final Queue<Future<InputStream>> queue) {
        Future<InputStream> f = pcsm.getDownloader().submit(new Callable<InputStream>() {
            public InputStream call() throws Exception {
                try {
                    currentCell().set(cell);

                    // open the stream -- this will cause the item to be
                    // downloaded from the server using the asset manager
                    InputStream is = u.openStream();

                    // once the stream is ready, see if it leads to any
                    // additional URLs (enqueue them if it does)
                    for (URL n : ((AssetPreloader) cell).assetLoaded(u, is)) {
                        enqueue(cell, n, queue);
                    }

                    // return the stream
                    return is;
                } finally {
                    currentCell().set(null);
                }
            }
        });

        queue.add(f);
    }
}

