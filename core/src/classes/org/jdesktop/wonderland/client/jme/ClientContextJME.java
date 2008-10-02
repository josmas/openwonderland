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

import org.jdesktop.mtgame.WorldManager;
import org.jdesktop.wonderland.client.ClientContext;
import org.jdesktop.wonderland.client.jme.input.InputManager3D;

/**
 * A subclass of ClientContext which adds JME client specific context accessors.
 * 
 * @author paulby
 */
public class ClientContextJME extends ClientContext {

    static {
        InputManager3D.getInputManager();
    }

    /**
     * Get the view manager
     * @return
     */
    public static ViewManager getViewManager() {
        return ViewManager.getViewManager();
    }
    
    /**
     * Get the mtgame world manager
     * @return
     */
    public static WorldManager getWorldManager() {
        return JmeClientMain.getWorldManager();
    }
}
