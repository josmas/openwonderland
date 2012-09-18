package org.jdesktop.wonderland.client.jme.input.bindings.spi;

import javax.swing.KeyStroke;
import org.jdesktop.wonderland.client.jme.input.bindings.ModifiersGroup.Modifier;

/**
 *
 * @author Ryan
 */
public interface KeyActionSPI extends ActionSPI {
    public KeyStroke getKeyStroke();
    
}
