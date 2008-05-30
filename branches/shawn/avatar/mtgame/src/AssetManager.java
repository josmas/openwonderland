/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.jme.app.mtgame;

/**
 * The AssetManager interface allows for a pluggable implementation of asset
 * retrieval.
 * 
 * @author Doug Twilleager
 */
public interface AssetManager {    
    /**
     * Load the given asset.
     * @param name
     * @param type
     * @return
     */
    public Asset load(String name, int type);
    
    /**
     * Unload the Asset
     * @param asset
     */
    public void unload(Asset asset);
}
