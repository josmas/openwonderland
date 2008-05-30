/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.jme.app.mtgame.entity;

import java.util.ArrayList;
import java.awt.AWTEvent;
import java.awt.event.AWTEventListener;
import java.util.HashMap;

/**
 * This component is used to receive AWT events from the input manager
 * It simply buffers the events, and then consumes them in the Entities
 * processor @see AWTEventProcessor
 * 
 * @author Doug Twilleager
 */
public class AWTEventListenerComponent extends EntityComponent implements AWTEventListener {
    /**
     * The buffered events
     */
    private ArrayList events = new ArrayList();

    /**
     * The default constructor
     */
    public AWTEventListenerComponent() {        
    }
    
    /**
     * This method gets called when the input manager sends an event to us
     * @param e
     */
    public void eventDispatched(AWTEvent e) {
        synchronized (events) {
            // TODO: should this be a copy?
            events.add(e);
        }
    }
    
    /**
     * This returns our currently queued events to the caller
     * @return
     */
    public Object[] getEvents() {
        Object[] ret = null;
        
        synchronized (events) {
            ret = events.toArray();
            events.clear();
        }  
        return (ret);
    }
    
    /**
     * A boolean that says whether or not there are events pending
     */
    public boolean eventsPending() {
        return (!events.isEmpty());
    }
    
    /**
     * Parse the known attributes.
     */
    public void parseAttributes(HashMap attributes) {

    }
}
