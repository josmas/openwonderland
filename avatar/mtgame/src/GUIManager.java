/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.jme.app.mtgame;

import com.jme.system.*;


/**
 * This class is used to create the overall gui for the system.
 * 
 * @author Doug Twilleager
 */
public class GUIManager {
    /**
     * The number of screens for this GUI
     */
    private int numberScreens = 1;
    
    /**
     * The render manager - needed for creating the canvas
     */
    private RenderManager renderManager = null;
    
    /**
     * The constructor
     */
    public GUIManager(RenderManager rm) {
        renderManager = rm;
    }
    
    /**
     * return the number of screens in the gui
     */
    public int numScreens() {
        return (numberScreens);
    }
}
