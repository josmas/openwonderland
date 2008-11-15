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

import com.jme.scene.CameraNode;
import org.jdesktop.mtgame.ProcessorComponent;
import org.jdesktop.mtgame.WorldManager;
import org.jdesktop.wonderland.common.cell.CellTransform;

/**
 * Interface for various implementations of Camera movement
 * 
 * @author paulby
 */
public abstract class CameraProcessor extends ProcessorComponent {

    protected CameraNode cameraNode;

    /**
     * Create a CameraProcessor for the specified cameraNode.
     *
     * @param cameraNode the cameraNode this processor will manipulate
     */
    public CameraProcessor() {
    }

    protected void initialize(CameraNode cameraNode) {
        this.cameraNode = cameraNode;
    }
    
    /**
     * The view cell has moved, update the camera position
     * @param worldTransform the worldTransform of the view cell
     */
    public abstract void viewMoved(CellTransform worldTransform);


}
