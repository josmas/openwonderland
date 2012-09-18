/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jdesktop.wonderland.client.cell.cache;

import java.util.concurrent.ThreadFactory;

/**
 *
 * @author Ryan
 */
public class URLDownloadCellFactory implements ThreadFactory {

    private static int count = 0;

    public synchronized static int nextCount() {
        return count++;
    }

    public Thread newThread(Runnable r) {
        return new Thread(r, "URL Download " + nextCount());
    }
}
