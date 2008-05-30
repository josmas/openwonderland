/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.jme.app.mtgame.entity;

import java.util.ArrayList;
import java.awt.AWTEvent;
import java.awt.event.AWTEventListener;

/**
 * This processor takes the given listener, and consumes it's events
 * when this processor is triggered.
 * 
 * @author Doug Twilleager
 */
public class AWTEventProcessorComponent extends ProcessorComponent implements AWTEventListener {
    /**
     * The listener to grap the events from
     */
    private AWTEventListenerComponent eventListener = null;
    
    /**
     * The list of events that gets sent to us.
     */
    private ArrayList events = new ArrayList();
    
    /**
     * The default constructor
     */
    public AWTEventProcessorComponent(AWTEventListenerComponent listener) {
        eventListener = listener;
    }
    
    /**
     * Arm for AWT Events
     */
    public void initialize() {
        setArmingConditions(ProcessorComponent.AWTEVENT_COND);
    }
    
    /**
     * Just print out the events
     * @param conditions
     */
    public void compute(long conditions) {
        Object[] events = getEvents();
        
        if (events.length == 0) {
            System.out.println("No Events!!!!!");
        }
        //for (int i=0; i<events.length; i++) {
        //    System.out.println("Event " + i + ": " + events[i]);
        //}
    }
    
    public void commit(long conditions) {
        
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

}
