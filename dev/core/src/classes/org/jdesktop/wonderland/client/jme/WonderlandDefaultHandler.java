/**
 * Project Wonderland
 *
 * Copyright (c) 2004-2008, Sun Microsystems, Inc., All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * $Revision$
 * $Date$
 * $State$
 */
package org.jdesktop.wonderland.client.jme;

import com.jme.input.InputHandler;
import com.jme.input.KeyBindingManager;
import com.jme.input.KeyInput;
import com.jme.input.action.KeyNodeBackwardAction;
import com.jme.input.action.KeyNodeForwardAction;
import com.jme.input.action.KeyNodeRotateLeftAction;
import com.jme.input.action.KeyNodeRotateRightAction;
import com.jme.scene.Spatial;

/**
 * The default InputHandler for wonderland. Inspired by Mark Powells Flag Rush demo
 *
 */
public class WonderlandDefaultHandler extends InputHandler {
    
    public WonderlandDefaultHandler(Spatial node, String api) {
        setKeyBindings();
        setActions(node);

    }

    /**
     * Define key bindings
     */
    private void setKeyBindings() {
        KeyBindingManager keyboard = KeyBindingManager.getKeyBindingManager();

        keyboard.set("forward", KeyInput.KEY_W);
        keyboard.set("backward", KeyInput.KEY_S);
        keyboard.set("turnRight", KeyInput.KEY_D);
        keyboard.set("turnLeft", KeyInput.KEY_A);
    }

    /**
     * Bind actions
     * @param node
     */
    private void setActions(Spatial node) {
        KeyNodeForwardAction forward = new KeyNodeForwardAction(node, 30f);
        addAction(forward, "forward", true);
        
        KeyNodeBackwardAction backward = new KeyNodeBackwardAction(node, 15f);
        addAction(backward, "backward", true);
        
        KeyNodeRotateRightAction rotateRight = new KeyNodeRotateRightAction(node, 5f);
        rotateRight.setLockAxis(node.getLocalRotation().getRotationColumn(1));
        addAction(rotateRight, "turnRight", true);
        
        KeyNodeRotateLeftAction rotateLeft = new KeyNodeRotateLeftAction(node, 5f);
        rotateLeft.setLockAxis(node.getLocalRotation().getRotationColumn(1));
        addAction(rotateLeft, "turnLeft", true);
    }
}
