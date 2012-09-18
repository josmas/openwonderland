package org.jdesktop.wonderland.client.jme.input.bindings.spi;

import org.jdesktop.wonderland.client.jme.input.bindings.ModifiersGroup.Modifier;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Ryan
 */
public interface MouseButtonActionSPI extends ActionSPI {
    public int getButton();

    public Modifier getModifier();
}
