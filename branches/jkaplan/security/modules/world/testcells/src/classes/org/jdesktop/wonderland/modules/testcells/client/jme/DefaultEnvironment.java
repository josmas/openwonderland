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
package org.jdesktop.wonderland.modules.testcells.client.jme;

import com.jme.light.LightNode;
import com.jme.light.PointLight;
import com.jme.renderer.ColorRGBA;
import org.jdesktop.wonderland.client.jme.ClientContextJME;
import org.jdesktop.wonderland.client.jme.Environment;

/**
 *
 * @author paulby
 */
public class DefaultEnvironment implements Environment {

    /**
     * @{@inheritDoc}
     */
    public void setGlobalLights() {
        LightNode globalLight1 = new LightNode();
        PointLight light = new PointLight();
        light.setDiffuse(new ColorRGBA(0.95f, 0.95f, 0.95f, 1.0f));
        light.setAmbient(new ColorRGBA(0.85f, 0.85f, 0.85f, 1.0f));
        light.setEnabled(true);
        globalLight1.setLight(light);
        globalLight1.setLocalTranslation(0.0f, 50.0f, 50.0f);

        LightNode globalLight2 = new LightNode();
        light = new PointLight();
        light.setDiffuse(new ColorRGBA(0.75f, 0.75f, 0.75f, 1.0f));
        light.setAmbient(new ColorRGBA(0.25f, 0.25f, 0.25f, 1.0f));
        light.setEnabled(true);
        globalLight2.setLight(light);
        globalLight2.setLocalTranslation(0.0f, -50.0f, -50.0f);
             
        ClientContextJME.getWorldManager().getRenderManager().addLight(globalLight1);
        ClientContextJME.getWorldManager().getRenderManager().addLight(globalLight2);
    }

    /**
     * @{@inheritDoc}
     */
    public void setSkybox() {
        
    }

}
