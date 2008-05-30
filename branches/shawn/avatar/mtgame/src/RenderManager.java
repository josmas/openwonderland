/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.jme.app.mtgame;

import java.util.ArrayList;
import com.jme.app.mtgame.entity.*;
import com.jme.scene.state.RenderState;
import com.jme.scene.Spatial;
import com.jme.intersection.CollisionResults;
import com.jme.scene.Node;
import com.jme.input.*;
import java.awt.Canvas;

/**
 * The RenderManager creates and controls the renderer threads.  It also acts as
 * the management interface to the rest of the system for anything rendering 
 * related.
 * 
 * @author Doug Twilleager
 */
public class RenderManager {    
    /**
     * The list of entities that have Camera elements
     */
    private ArrayList cameras = new ArrayList();
    
    /**
     * The list of entities that have scene elements
     */
    private ArrayList scenes = new ArrayList();
    
    /**
     * The list of avatar entities
     */
    private ArrayList avatars = new ArrayList();
    
    /**
     * The list of HUD elements
     */
    private ArrayList huds = new ArrayList();
    
    /**
     * The list of other visual entities
     */
    private ArrayList entities = new ArrayList();
    
    /**
     * The array of Renderer's - one per screen.
     * TODO: Only 1 screen supported for now
     */
    private Renderer[] renderer = new Renderer[1];
    
    /**
     * The entity process controller - used for new frame triggers
     */
    private EntityProcessController entityProcessController = null;
    
    /**
     * A flag indicating that the renderer has been initialized
     */
    private boolean initialized = false;
    
    /**
     * The default constructor
     */
    public RenderManager() {
        // Wait until we have a canvas to render into before doing anything
        renderer[0] = new Renderer(this, 0);
    }
    
    /**
     * Create a window
     */
    public Canvas createCanvas(int width, int height) {
        Canvas canvas = null;
        
        synchronized (renderer) {
            if (!initialized) {
                renderer[0].initialize();
                initialized = true;
            }
            canvas = renderer[0].createCanvas(width, height);
        }
        return (canvas);
    }
    
    /**
     * This method blocks until the renderer is ready to go
     */
    public void waitUntilReady() {
        renderer[0].waitUntilReady();
    }
    
    /**
     * set the entity process controller
     */
    public void setEntityProcessController(EntityProcessController epc) {
        entityProcessController = epc;
        renderer[0].setEntityProcessController(epc);
    }
    
    /**
     * Get an object from the renderer
     */
    public RenderState createRendererState(int type) {
        renderer[0].waitUntilReady();
        return(renderer[0].createRendererState(type));
    }
    
    
    /**
     * Add an entity to our processing list
     */
    public void addEntity(Entity e) {
        // Pass the entity onto the renderer
        renderer[0].addEntity(e);
    }
    
    /**
     * Find the collisions for the scene
     * @param sp
     * @param cr
     */
     public void findCollisions(Spatial sp, CollisionResults cr) {
         renderer[0].findCollisions(sp, cr);
     }
    
    /**
     * Add a processor which has triggerd to the Renderer Processor List
     */
    public void addTriggeredProcessor(ProcessorComponent pc) {
        renderer[0].addTriggeredProcessor(pc);
    }
    
    /**
     * Set the desired frame rate
     */
    public void setDesiredFrameRate(int fps) {
        // Pass the info onto the renderers
        for (int i=0; i < renderer.length; i++) {
                renderer[i].setDesiredFrameRate(fps);
        }        
    }
        
    /**
     * Set a listener for frame rate updates
     */
    public void setFrameRateListener(FrameRateListener l, int frequency) {
        for (int i = 0; i < renderer.length; i++) {
            renderer[i].setFrameRateListener(l, frequency);
        }      
    }
    
    /**
     * Add a node to the update lists
     */
    public void addToUpdateList(Spatial spatial) {
        // What about multiple renderers
        renderer[0].addToUpdateList(spatial);
    }
    
    /**
     * Run the processes component commit list
     * For now, we'll just run them on screen 0
     */
    public void runCommitList(ProcessorComponent[] runList) {
        // This method will block until the renderer processes 
        // the whole list
        renderer[0].runCommitList(runList);
    }
    
    /**
     * Start tracking key input.
     */
    public void trackKeyInput(Object listener) {
        renderer[0].trackKeyInput(listener);
    }
    
    /**
     * Stop tracking key input
     */
    public void untrackKeyInput(Object listener) {
        renderer[0].untrackKeyInput(listener);
    }
    
    /**
     * Set the MouseInput to track.  Null means stop tracking
     */
    public void trackMouseInput(Object listener) {
        renderer[0].trackMouseInput(listener);
    }
    
    /**
     * Stop tracking key input
     */
    public void untrackMouseInput(Object listener) {
        renderer[0].untrackMouseInput(listener);
    }
    
    /**
     * Let the entity manager know that a frame has ticked
     */
    public void triggerNewFrame() {
        // Don't try to count a frame tick if the epc isn't ready
        if (entityProcessController != null) {
            entityProcessController.triggerNewFrame();
        }
    }
}
