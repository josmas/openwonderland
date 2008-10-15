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
package org.jdesktop.wonderland.modules.testcells.client.cell;

import org.jdesktop.mtgame.Entity;
import org.jdesktop.mtgame.RenderComponent;
import org.jdesktop.wonderland.client.input.Event;
import org.jdesktop.wonderland.client.jme.input.MouseButtonEvent3D;
import com.jme.scene.Node;
import com.sun.scenario.animation.Clip;
import com.sun.scenario.animation.Interpolators;
import org.jdesktop.wonderland.client.input.EventClassListener;
import org.jdesktop.wonderland.common.ExperimentalAPI;

/**
 * @author paulby
 */

@ExperimentalAPI
public class SpinAnimObjectEventListener extends EventClassListener {

     private Clip clip2;
        
    /**
     * Consume only mouse button events.
     */
    @Override
    public Class[] eventClassesToConsume () {
	return new Class[] { MouseButtonEvent3D.class };
    }

    // Note: we don't override computeEvent because we don't do any computation in this listener.

    /**
     * {@inheritDoc}
     */
    @Override
    public void commitEvent (Event event) {
	if (!((MouseButtonEvent3D)event).isPressed()) {
	    return;
	}
	Entity entity = event.getEntity();
	RotationAnimationProcessor spinner = (RotationAnimationProcessor) entity.getComponent(RotationAnimationProcessor.class);
	if (spinner == null) {
            Node node = getSceneRoot(entity);
            if (node==null)
                return;

	    spinner = new RotationAnimationProcessor(node, 0f, (float)Math.PI*2f);
            clip2 = Clip.create(1000, spinner);
            clip2.setInterpolator(Interpolators.getEasingInstance(0.4f, 0.4f));
            entity.addComponent(RotationAnimationProcessor.class, spinner);
        }

        if (!clip2.isRunning())
            clip2.start();
    }

    static Node getSceneRoot (Entity entity) {
        RenderComponent renderComp = (RenderComponent) entity.getComponent(RenderComponent.class);
        if (renderComp == null) {
            return null;
        }
        Node node = renderComp.getSceneRoot();
        return node;
    }
}

