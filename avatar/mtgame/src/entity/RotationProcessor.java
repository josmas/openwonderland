/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.jme.app.mtgame.entity;

import com.jme.scene.Node;
import com.jme.math.Quaternion;
import com.jme.app.mtgame.WorldManager;


/**
 * This is a simple test processor that rotates a node around the Y axis
 * a little bit every frame
 * 
 * @author Doug Twilleager
 */
public class RotationProcessor extends ProcessorComponent {
    /**
     * The WorldManager - used for adding to update list
     */
    private WorldManager worldManager = null;
    /**
     * The current degrees of rotation
     */
    private float degrees = 0.0f;

    /**
     * The increment to rotate each frame
     */
    private float increment = 0.0f;
    
    /**
     * The rotation matrix to apply to the target
     */
    private Quaternion quaternion = new Quaternion();
    
    /**
     * The rotation target
     */
    private Node target = null;
    
    /**
     * A name
     */
    private String name = null;
    
    /**
     * The constructor
     */
    public RotationProcessor(String name, WorldManager worldManager, Node target, float increment) {
        this.worldManager = worldManager;
        this.target = target;
        this.increment = increment;
        this.name = name;
    }
    
    public String toString() {
        return (name);
    }
    
    /**
     * The initialize method
     */
    public void initialize() {
        setArmingConditions(ProcessorComponent.NEW_FRAME_COND);
    }
    
    /**
     * The Calculate method
     */
    public void compute(long conditions) {
        degrees += increment;
        quaternion.fromAngles(0.0f, degrees, 0.0f);
    }

    /**
     * The commit method
     */
    public void commit(long conditions) {
        target.setLocalRotation(quaternion);
        worldManager.addToUpdateList(target);
    }
}
