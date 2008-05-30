/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.jme.app.mtgame;

import java.util.HashMap;
import com.jme.app.mtgame.entity.*;


/**
 * This class maps out the description of a piece of content.  This is constructed
 * from parsing a file or reading a description from a stream.
 * 
 * @author Doug Twilleager
 */
public class ContentDescriptor {
    /**
     * The name of the content
     */
    private String name = null;
    
    /**
     * The Attributes for this entity
     */
    private HashMap attributes = null;
    
    /**
     * The list of Asset's for this content
     */
    private Asset[] assets = null;
    
    /**
     * The list of components supported by this content.
     */
    private EntityComponentDescriptor[] components = null;
    
    /**
     * The default constructor
     */
    public ContentDescriptor(String name, HashMap attributes, Asset[] assets, EntityComponentDescriptor[] components) {
        this.name = name;
        this.attributes = attributes;
        this.assets = assets;
        this.components = components;
    }
    
    /**
     * Get the name
     */
    public String getName() {
        return (name);
    }
    
    /**
     * Get the attributes
     */
    public HashMap getAttributes() {
        return (attributes);
    }
    
    /**
     * Get the assets
     */
    public Asset[] getAssets() {
        return (assets);
    }
    
    /**
     * Get the components
     */
    public EntityComponentDescriptor[] getComponents() {
        return (components);
    }
}
