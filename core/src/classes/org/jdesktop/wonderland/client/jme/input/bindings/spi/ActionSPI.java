package org.jdesktop.wonderland.client.jme.input.bindings.spi;


import org.jdesktop.wonderland.client.jme.input.InputEvent3D;


/**
 *
 * @author Ryan
 */
public interface ActionSPI {
    public String getName();
    
    public <T extends InputEvent3D>void performAction(T event);
}
