/**
 * Copyright (c) 2014, WonderBuilders, Inc., All Rights Reserved
 */

package com.wonderbuilders.modules.capabilitybridge.client;

import org.jdesktop.wonderland.client.input.EventClassListener;

/**
 *
 * When we need to call a method of a component from different component.
 * we can use this interface to remove tightly coupled relation between those
 * two components
 * 
 * Here we need to call getMouseEventListener() method from certain components.
 * You can also add more methods here.
 * So we need to implement this interface for the component which 
 * has getMouseEventListener() method.
 * 
 * This will remove the dependency between modules. 
 * 
 * @author Abhishek Upadhyay
 */
public interface CapabilityBridge {
    
    public EventClassListener getMouseEventListener();
    
}
