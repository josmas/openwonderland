/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jdesktop.wonderland.client.assetmgr;

import org.jdesktop.wonderland.common.AssetURI;

/**
 * No-op default implementation
 */
public class NoopAssetStatisticsSPI implements AssetStatisticsSPI {

    public void assetStatistic(AssetURI uri, AssetManager.AssetStat stat, long time) {
        // ignore
        // ignore
    }
    
}
