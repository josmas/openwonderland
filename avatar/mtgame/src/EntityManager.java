/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.jme.app.mtgame;

import com.jme.app.mtgame.entity.*;
import java.util.ArrayList;

/**
 * The EntityManager creates and manages Enities for the system.  Once the entities
 * are created, it with give the renderable components to the RenderManager, while
 * keeping the processor components for execution here.
 * 
 * @author Doug Twilleager
 */
public class EntityManager {
    /**
     * The EntityProcessController
     */
    private EntityProcessController entityProcessController = null;
    
    /**
     * The RenderManager
     */
    private RenderManager renderManager = null;
    
    /**
     * The list of known spaces
     */
    private ArrayList spaces = new ArrayList();
    
    /**
     * The list of known entities
     */
    private ArrayList entities = new ArrayList();
    
    /**
     * The Default Constructor.
     */
    public EntityManager(RenderManager rm) {
        renderManager = rm;
        
        // Create the EntityProcessController
        entityProcessController = new EntityProcessController(rm);
        entityProcessController.initialize();
    }
    
    /**
     * Create and manage a collection of entities given a set of 
     * Content Descriptors
     */
    public void createEntities(ContentDescriptor[] content) {
        Entity e = null;
        
        for (int i=0; i<content.length; i++) {
            e = createEntity(content[i]);
            
            if (e.getComponent(SceneComponent.class) != null ||
                e.getComponent(CameraComponent.class) != null) {
                renderManager.addEntity(e);
            }
            
            if (e.getComponent(ProcessorComponent.class) != null) {
                entityProcessController.addEntity(e);
            }
        }
    }
    
    /**
     * This adds an already created entity to the system
     */
    public void addEntity(Entity e) {
        if (e.getComponent(SceneComponent.class) != null ||
            e.getComponent(CameraComponent.class) != null) {
            renderManager.addEntity(e);
        }

        if (e.getComponent(ProcessorComponent.class) != null ||
            e.getComponent(ProcessorCollectionComponent.class) != null) {
            entityProcessController.addEntity(e);
        }
        
        if (!(e instanceof Space)) {
            entities.add(e);
            entityMoved(e);
        }
    }   
    
    /**
     * Notify that the entity has moved.  Update the space
     */
    public void entityMoved(Entity entity) {
        synchronized (spaces) {
            for (int i=0; i<spaces.size(); i++) {
                Space s = (Space) spaces.get(i);
                if (s.contains(entity)) {
                    s.addToEntityList(entity);
                    entity.addToSpaceList(s);
                } else {
                    s.removeFromEntityList(entity);
                    entity.removeFromSpaceList(s);
                }
            }
        }
    }
        
    /**
     * This adds an already created entity to the system
     */
    public void addSpace(Space s) {
        synchronized (spaces) {
            spaces.add(s);
            for (int i=0; i<entities.size(); i++) {
                Entity e = (Entity) entities.get(i);
                if (s.contains(e)) {
                    s.addToEntityList(e);
                    e.addToSpaceList(s);
                }
            }
        }
        addEntity(s);
    }
    
    /**
     * Create an entity, given a content description
     * @param cd
     * @return
     */
    private Entity createEntity(ContentDescriptor cd) {
        Entity e = new Entity(cd.getName(), cd.getAttributes());
        EntityComponentDescriptor[] components = cd.getComponents();
        EntityComponent ec = null;
        Class cl = null;
        
        for (int i=0; i<components.length; i++) {
            String name = components[i].getName();
            try {
                cl = Class.forName(name);
            } catch (ClassNotFoundException ex) {
                System.out.println(ex);
            }
            ec = EntityComponent.createComponent(cl);
            ec.parseAttributes(components[i].getAttributes());
            e.addComponent(cl, ec);
            
        }
        return (e);
    }
    
    /**
     * Pass along an awt event trigger to the process controller
     */
    public void triggerAWTEvent() {
        entityProcessController.triggerAWTEvent();
    }
}
