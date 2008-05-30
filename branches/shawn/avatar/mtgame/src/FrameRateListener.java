/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.jme.app.mtgame;

/**
 * Anyone who wants to get framerate updates, just implement this
 * and pass it along to the WorldManager
 * 
 * @author Doug Twilleager
 */
public interface FrameRateListener {
    /**
     * The current framerate in frames per second
     * @param framerate
     */
    public void currentFramerate(float framerate);
}
