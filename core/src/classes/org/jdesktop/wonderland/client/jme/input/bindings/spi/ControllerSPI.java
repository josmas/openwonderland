package org.jdesktop.wonderland.client.jme.input.bindings.spi;

import java.util.Set;
import javax.swing.KeyStroke;
import org.jdesktop.wonderland.client.jme.input.InputEvent3D;
import org.jdesktop.wonderland.client.jme.input.bindings.ActionBindingContext;
import org.jdesktop.wonderland.client.jme.input.bindings.ActionWrapperObject;
import org.jdesktop.wonderland.client.jme.input.bindings.Couple;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Ryan
 */
public interface ControllerSPI {
    
    public Class consumesEventClass();
    
    public <T extends InputEvent3D> void dispatchEvent(T event);
    
    public Set<Couple<? extends Object, ActionWrapperObject>> getActions();

    public void setContext(ActionBindingContext aThis);
    
    public void bindObject(Object in, String name);

    public void unbindObject(Object key);
}
