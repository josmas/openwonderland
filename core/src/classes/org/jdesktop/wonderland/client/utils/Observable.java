/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jdesktop.wonderland.client.utils;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Ryan
 */
public class Observable<O extends Observer> {
    
    private List<O> observers = new ArrayList<O>();
    
    public Observable() {
        
    }
    
    public void addObserver(O observer) {
        synchronized(observers) {
            observers.add(observer);
        }
    }
    
    public void removeObserver(O observer) {
        synchronized(observers) {
            observers.add(observer);
        }
    }
    
    public void fire(String property, Object value) {
        synchronized(observers) {
            for(O observer: observers) {
                observer.eventObserved(property, value);
            }
        }
    }
    
    public void clear() {
        synchronized(observers) {
            observers.clear();
        }
    }
    
}
