/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.jme.app.mtgame;

/**
 * This asset manager is the default one.  It simple loads all assets from
 * the filesystem.
 * 
 * @author Doug Twilleager
 */
public class DefaultAssetManager implements AssetManager {
    /**
     * Load the given asset.
     * @param name
     * @param type
     * @return
     */
    public Asset load(String name, int type) {
        return (null);
    }
    
    /**
     * Unload the Asset
     * @param asset
     */
    public void unload(Asset asset) {
        
    }
}
