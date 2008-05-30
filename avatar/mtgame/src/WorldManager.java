/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.jme.app.mtgame;

import java.util.HashMap;
import com.jme.app.mtgame.entity.*;
import com.jme.scene.state.RenderState;
import com.jme.scene.Spatial;
import com.jme.intersection.CollisionResults;
import java.awt.event.AWTEventListener;
import com.jme.scene.Node;
import java.awt.Canvas;

/**
 * This is the class which manages everything in the system.
 * It creates and intializes all the other system.
 * 
 * @author Doug Twilleager
 */
public class WorldManager {
    /**
     * The name of this world
     */
    private String name = null;
    /**
     * The RenderManager
     */
    private RenderManager renderManager = null;
    
    /**
     * The GUI Manager
     */
    private GUIManager guiManager = null;
    
    /**
     * The EntityManager
     */
    private EntityManager entityManager = null;
    
    /**
     * The ContentManager
     */
    private ContentManager contentManager = null;
    
    /**
     * The NetworkManager
     */
    private NetworkManager networkManager = null;
    
    /**
     * The InputManager
     */
    private InputManager inputManager = null;
    
    /**
     * The Default Constructor
     */
    public WorldManager(String name, HashMap attributes) {
        this.name = name;
        parseAttributes(attributes);
        
        System.out.println("Initializing Render Manager");
        renderManager = new RenderManager();
        
        System.out.println("Initializing GUI");
        guiManager = new GUIManager(renderManager);
        
        System.out.println("Initializing Entity Manager");
        entityManager = new EntityManager(renderManager);
        
        System.out.println("Initializing Input Manager");
        inputManager = new DefaultInputManager();
        inputManager.initialize(renderManager, entityManager);
        
        System.out.println("Initializing  Content Manager");
        contentManager = new ContentManager();
        
        System.out.println("Done Initializing!");
    }
    
    /**
     * Add a new entity to the system.
     */
    public void addEntity(Entity entity) {
        entityManager.addEntity(entity);
    }
        
    /**
     * The entity has moved
     */
    public void entityMoved(Entity entity) {
        entityManager.entityMoved(entity);
    }
    
    /**
     * Add a new entity to the system.
     */
    public void addSpace(Space space) {
        entityManager.addSpace(space);
    }
    
    /**
     * Find the collisions for the scene
     * @param sp
     * @param cr
     */
     public void findCollisions(Spatial sp, CollisionResults cr) {
         renderManager.findCollisions(sp, cr);
     }
    
    /**
     * Add a node to the update list
     */
    public void addToUpdateList(Spatial spatial) {
        renderManager.addToUpdateList(spatial);
    }
    
    /**
     * Set the desired frame rate
     */
    public void setDesiredFrameRate(int fps) {
        // Pass the info onto the renderers
        renderManager.setDesiredFrameRate(fps);      
    }
    
    /**
     * Set a listener for frame rate updates
     */
    public void setFrameRateListener(FrameRateListener l, int frequency) {
        renderManager.setFrameRateListener(l, frequency);
    }
    
    /**
     * Add an entity that wishes to recieve key events
     */
    public void addAWTKeyListener(AWTEventListener listener) {
        inputManager.addAWTKeyListener(listener);
    }
    
    /**
     * Add an entity that wishes to recieve mouse events
     */
    public void addAWTMouseListener(AWTEventListener listener) {
        inputManager.addAWTMouseListener(listener);
    }
    
    /**
     * Get an object from the renderer
     */
    public RenderState createRendererState(int type) {
        return (renderManager.createRendererState(type));
    }
    
    /**
     * Create a canvas usable for jME rendering
     */
    public Canvas createCanvas(int width, int height) {
        return(renderManager.createCanvas(width, height));
    }
    
    /**
     * Parse the known attributes
     */
    private void parseAttributes(HashMap attributes) {
        
    }
  
}
