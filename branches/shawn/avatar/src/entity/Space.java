/*
* To change this template, choose Tools | Templates
* and open the template in the editor.
*/

package com.jme.app.mtgame.entity;

import java.util.HashMap;
import java.util.ArrayList;

import com.jme.app.mtgame.entity.Entity;
import com.jme.bounding.BoundingVolume;
import com.jme.math.Vector3f;

/**
* This subclass of entity is used for (mostly) static scene data.  It adds
* a spatial data structure to a 3D space.  All entities exist in a Space, and
* can move from Space to Space.
* 
* @author Doug Twilleager
*/
public class Space extends Entity {
    /**
     * The scene comonent for this space
     */
    private SceneComponent scene = null;
    
    /**
     * The overall bounds for this space
     */
    private BoundingVolume bounds = null;
    
    /**
     * The processors associated with the scene
     */
    private ProcessorCollectionComponent processors = new ProcessorCollectionComponent();
    
    /**
     * The list of entities currently in this space
     */
    private ArrayList entities = new ArrayList();
    
    /**
     * A vector to use for includes
     */
    private Vector3f tmpPos = new Vector3f();
    
    /**
     * The name of this entity
     */
    private String name = null;
    
    /**
     * The default constructor
     */
    public Space(String name, HashMap attributes, SceneComponent scene, BoundingVolume bounds) {
        super(name, attributes);
        setScene(scene);
        this.bounds = bounds;
        this.name = name;
    }
    
    /**
     * This sets the scene for the Space
     */
    public void setScene(SceneComponent scene) {
        this.scene = scene;
        addComponent(SceneComponent.class, scene);
    }
    
    /**
     * This gets the scene for the space
     */
    public SceneComponent getScene() {
        return(scene);
    }
    
    /**
     * This adds a set of processors to the Space
     */
    public void addProcessors(ProcessorCollectionComponent pcc) {
        processors = pcc;
        addComponent(ProcessorCollectionComponent.class, processors);
    }
    
    /**
     * Get the list of processors
     */
    public ProcessorCollectionComponent getProcessors() {
        return (processors);
    }
    
    /**
     * Add an entity to the list of entities in this space
     */
    public void addToEntityList(Entity e) {
        if (!entities.contains(e)) {
            entities.add(e);
        }
    }
    
    /**
     * Remove an entity from the list of entities in this space
     */
    public void removeFromEntityList(Entity e) {
        if (entities.contains(e)) {
            entities.remove(e);
        }
    }
    
    /**
     * Checks to see if this entity is in the space (from a spatial perspective)
     */
    public boolean contains(Entity e) {
        e.getPosition(tmpPos);
        return (bounds.contains(tmpPos));
    }
    
    public String toString() {
        return (name);
    }
}