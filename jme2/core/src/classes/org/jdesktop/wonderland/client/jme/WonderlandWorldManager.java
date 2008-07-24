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

import com.jme.entity.Entity;
import com.jme.scene.Node;
import java.util.HashMap;
import org.jdesktop.mtgame.WorldManager;


/**
 *
 *  TEMPORARY CLASS, WILL BE REMOVED ONCE WE HAVE entityMoved listener interface in WorldManager
 * 
 * @author paulby
 */
public class WonderlandWorldManager extends WorldManager {

    private ClientManager clientManager;
    
    public WonderlandWorldManager(String name, HashMap attributes, ClientManager clientManager) {
        super(name);
        this.clientManager = clientManager;
    }
    
    // TODO replace with listener
    public void nodeMoved(Node entity) {
        super.nodeMoved(entity);
        clientManager.nodeMoved(entity);
    }
    

}
