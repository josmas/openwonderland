/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jdesktop.wonderland.client.assetmgr;

import org.jdesktop.wonderland.client.assetmgr.TrackingInputStream.ProgressListener;

/**
 * A class that implements the progress listener for a tracking stream,
 * and also associates an Asset. Signals the AssetManager's progress
 * listener
 */
public class StreamProgressListener implements ProgressListener {
    private Asset asset = null;
    private final AssetManager outer;

    public StreamProgressListener(Asset asset, final AssetManager outer) {
        this.outer = outer;
        this.asset = asset;
    }

    public void downloadProgress(int readBytes, int percentage) {
        // notify the listeners of progress
        outer.fireDownloadProgress(asset, readBytes, percentage);
    }
    
}
