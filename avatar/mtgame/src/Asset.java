/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.jme.app.mtgame;

/**
 * This class represents an asset loaded from the asset manager
 * 
 * @author Doug Twilleager
 */
public class Asset {
    /**
     * The asset types
     */
    public static final int ASSET_EMPTY = -1;
    public static final int ASSET_SCENE = 0;
    public static final int ASSET_CODE = 1;
   
    /**
     * The name of the asset
     */
    private String name = null;
    
    /** 
     * The type of Asset
     */
    private int type = ASSET_EMPTY;
    
    /**
     * The data for the asset
     */
    private Object data = null;
    
    /**
     * The contructor assigns the name, type, and data.
     */
    public Asset(String name, int type, Object data) {
        this.name = name;
        this.type = type;
        this.data = data;
    }
    
    /**
     * Get the data for the asset
     * 
     * The current return values are as follows
     *      Spacial for a SCENE
     *      A Class for CODE
     */
    public Object getData() {
        return (data);
    }
}