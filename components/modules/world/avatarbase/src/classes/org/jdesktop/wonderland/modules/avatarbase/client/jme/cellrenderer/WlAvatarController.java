/**
 * Project Wonderland
 *
 * Copyright (c) 2004-2009, Sun Microsystems, Inc., All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * Sun designates this particular file as subject to the "Classpath" 
 * exception as provided by Sun in the License file that accompanied 
 * this code.
 */
package org.jdesktop.wonderland.modules.avatarbase.client.jme.cellrenderer;

import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import imi.character.avatar.Avatar;
import org.jdesktop.wonderland.common.cell.CellTransform;

/**
 *
 * @author paulby
 */
public class WlAvatarController extends imi.character.avatar.AvatarController {

    private boolean selectedForInput = false;
    private CellTransform transform = null;

    // Global tmp variable
    private Quaternion quat = new Quaternion();

    // Global tmp variable
    private Vector3f v3f = new Vector3f();

    public WlAvatarController(Avatar master) {
        super(master);
    }

    @Override
    public float getVelocityScalar() {
        if (selectedForInput)
            return super.getVelocityScalar();

        // TODO 1f means the avatar will always be walking
        return 1f;
    }

    @Override
    public void update(float deltaTime) {
        if (selectedForInput)
            super.update(deltaTime);
        else {
            if (!initalized)
            {
                initialize();
                return;
            }
            
            if (transform!=null) {
                body.getTransform().getLocalMatrix(true).set2(transform.getRotation(quat), transform.getTranslation(v3f), 1f);
            }
        }
    }

    /**
     * Select this avatar for input. Avatars that are not selected for input
     * have their transform set via the cellTransformUpdate method.
     * @param selected
     */
    void selectForInput(boolean selected) {
        selectedForInput = selected;
    }

    /**
     * Set the transform for avatars that are not selected for input
     * 
     * @param transform
     */
    void cellTransformUpdate(CellTransform transform) {
        this.transform = transform;
    }
}
