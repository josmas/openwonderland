/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.jme.app.mtgame;

import com.jme.app.mtgame.entity.*;
import java.awt.event.AWTEventListener;

/**
 * This class handles input management for the system.  A custom input manager
 * can be created to handle the receiving and distribution of events
 * @author runner
 */
public abstract class InputManager {
    /**
     * The default constructor
     */
    public InputManager() {      
    }
    
    /**
     * The initialize method
     */
    public abstract void initialize(RenderManager rm, EntityManager em);
    
    /**
     * Adds an Entity to the list of Entities interested in key events
     */
    public abstract void addAWTKeyListener(AWTEventListener listener);
 
    /**
     * Removes an Entity from the list of Entities interested in key events
     */
    public abstract void removeAWTKeyListener(AWTEventListener listener);
    
    /**
     * Adds an Entity to the list of Entities interested in mouse events
     */
    public abstract void addAWTMouseListener(AWTEventListener listener);

    /**
     * Removes an Entity from the list of Entities interested in mouse events
     */
    public abstract void removeAWTMouseListener(AWTEventListener listener);
}
