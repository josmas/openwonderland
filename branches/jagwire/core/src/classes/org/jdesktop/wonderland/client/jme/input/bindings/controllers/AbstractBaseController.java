package org.jdesktop.wonderland.client.jme.input.bindings.controllers;


import org.jdesktop.wonderland.client.jme.input.bindings.ActionBindingContext;
import org.jdesktop.wonderland.client.jme.input.bindings.spi.ControllerSPI;

/**
 *
 * @author Ryan
 */
public abstract class AbstractBaseController implements ControllerSPI {
    
    public void initialize(ActionBindingContext context) {        
        
        loadActions(context);
    }   
    
    protected abstract void loadActions(ActionBindingContext context);       
}
