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

package org.jdesktop.wonderland.modules.testcells.client.timingframework.util;

import com.sun.scenario.animation.Clip;
import com.sun.scenario.animation.util.Trigger;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.logging.Logger;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.mtgame.ProcessorCollectionComponent;
import org.jdesktop.wonderland.client.input.Event;
import org.jdesktop.wonderland.client.jme.input.MouseButtonEvent3D;
import org.jdesktop.wonderland.client.jme.input.MouseEnterExitEvent3D;
import org.jdesktop.wonderland.modules.testcells.client.timingframework.TriggerEventProcessor;

/**
 * MouseTrigger handles mouse events
 * and triggers an animation based on those events.
 * For example, to have anim start when component receives an
 * ENTER event, one might write the following:
 * <pre>
 *     MouseTrigger trigger = 
 *         MouseTrigger.addTrigger(component, anim, MouseTriggerEvent.ENTER);
 * </pre>
 * 
 * @author paulby
 */
public class Mouse3DTrigger extends WonderlandEventTrigger {
    
    
    /**
     * Creates a {@code MouseTrigger} and adds it as a 
     * listener to component.
     * 
     * @param component component that will generate {@code MouseEvent}s
     * for this trigger
     * @param clip the {@code clip} that will start when the event occurs
     * @param event the {@code MouseTriggerEvent} that will cause the
     * action to fire
     * @return the resulting trigger
     */
    public static Mouse3DTrigger addTrigger(Entity entity,
                                            Clip clip,
                                            Mouse3DTriggerEvent event)
    {
        Mouse3DTrigger trigger = new Mouse3DTrigger(clip, event);

        TriggerEventProcessor proc = (TriggerEventProcessor) entity.getComponent(TriggerEventProcessor.class);
        if (proc==null) {
            proc = new TriggerEventProcessor();
            proc.addToEntity(entity);
        }
        proc.addTrigger(trigger);

        return trigger;
    }

    /**
     * Creates a {@code MouseTrigger}, which should be added
     * to a {@code Component} that will generate the mouse events of interest.
     */
    Mouse3DTrigger(Clip clip, Mouse3DTriggerEvent event) {
        super(clip, event);
        addEventClass(event.getEventClass());
    }


    @Override
    public void processEvent(Event evt) {
        if (evt instanceof MouseEnterExitEvent3D) {
            MouseEnterExitEvent3D ee = (MouseEnterExitEvent3D) evt;
            if (ee.isEnter()) {
                fire(Mouse3DTriggerEvent.ENTER);
            } else {
                fire(Mouse3DTriggerEvent.EXIT);
            }
        } else if (evt instanceof MouseButtonEvent3D) {
            MouseButtonEvent3D b = (MouseButtonEvent3D) evt;
            switch(b.getAwtEvent().getID()) {
                case MouseEvent.MOUSE_CLICKED :
                    fire(Mouse3DTriggerEvent.CLICK);
                    break;
                case MouseEvent.MOUSE_PRESSED :
                    fire(Mouse3DTriggerEvent.PRESS);
                    break;
                case MouseEvent.MOUSE_RELEASED :
                    fire(Mouse3DTriggerEvent.RELEASE);
                    break;
                default :
                    Logger.getLogger(Mouse3DTrigger.class.getName()).severe("Unknown event id "+b.getAwtEvent().getID());
            }
        }
    }


}
