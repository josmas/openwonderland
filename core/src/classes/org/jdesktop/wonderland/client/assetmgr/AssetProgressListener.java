/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jdesktop.wonderland.client.assetmgr;

import org.jdesktop.wonderland.common.ExperimentalAPI;

/**
 * Used to indicate the status of an asset that is being downloaded
 */
@ExperimentalAPI
public interface AssetProgressListener {

    /**
     * Updates the amount the asset has been downloaded.
     *
     * @param asset The Asset being downloaded
     * @param readBytes The number of bytes that have been ready
     * @param percentage The percentage of the bytes read, or -1 if unknown
     */
    public void downloadProgress(Asset asset, int readBytes, int percentage);

    /**
     * Indicates the download of the asset has failed
     *
     * @param asset The Asset whose download has failed
     */
    public void downloadFailed(Asset asset);

    /**
     * Indicates the download of the asset has finished successfull.
     *
     * @param asset The Asset whose download has completed
     */
    public void downloadCompleted(Asset asset);
    
}
