/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.jme.app.mtgame.entity;

import java.util.HashMap;

/**
 * This describes the attributes of an entity.
 * 
 * @author Doug Twilleager
 */
public class EntityDescriptor {
    /**
     * The name of the EntityComponent
     */
    private String name = null;
    
    /**
     * The attribute map for this EntityComponent
     */
    private HashMap attributeMap = new HashMap();
    
    /**
     * The default constructor
     */
    public EntityDescriptor(String name, HashMap attributeMap) {
        this.name = name;
        this.attributeMap = attributeMap;
    }
    
    /**
     * Get the name
     */
    public String getName() {
        return (name);
    }
    
    /**
     * Get the attribute map
     */
    public HashMap getAttributes() {
        return (attributeMap);
    }
}
