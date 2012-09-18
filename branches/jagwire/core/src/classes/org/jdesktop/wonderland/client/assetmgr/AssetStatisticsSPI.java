/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jdesktop.wonderland.client.assetmgr;

import org.jdesktop.wonderland.common.AssetURI;

/**
 * Provider that will be notified of asset statistics
 */
public interface AssetStatisticsSPI {

    /**
     * Asset statistic value.
     * @param uri the uri of the asset
     * @param stat the statistic
     * @param time the time in milliseconds
     */
    public void assetStatistic(AssetURI uri, AssetManager.AssetStat stat, long time);
    
}
