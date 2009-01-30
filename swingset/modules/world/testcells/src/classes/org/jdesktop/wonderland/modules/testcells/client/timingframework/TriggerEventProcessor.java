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
package org.jdesktop.wonderland.modules.testcells.client.timingframework;

import org.jdesktop.wonderland.modules.testcells.client.timingframework.util.*;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.mtgame.RenderComponent;
import com.jme.scene.Node;
import java.util.ArrayList;
import java.util.HashSet;
import org.jdesktop.wonderland.client.input.Event;
import org.jdesktop.wonderland.client.input.EventClassListener;
import org.jdesktop.wonderland.common.ExperimentalAPI;
import org.jdesktop.wonderland.modules.testcells.client.timingframework.util.WonderlandEventTrigger;

/**
 * @author paulby
 */

@ExperimentalAPI
public class TriggerEventProcessor extends EventClassListener {

    private ArrayList<WonderlandEventTrigger> triggers = new ArrayList();
    private HashSet<Class<? extends Event>> triggerEvents = new HashSet();
    private Class[] triggerEventsArray = null;

    /**
     * {@inheritDoc}
     */
    @Override
    public Class[] eventClassesToConsume () {
        synchronized(triggerEvents) {
            return triggerEventsArray;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void commitEvent (Event event) {
        for(WonderlandEventTrigger t : triggers) {
            t.commitEvent(event);
        }
    }

    /**
     * Add the trigger to the set handled by this processor
     * @param trigger
     */
    public void addTrigger(WonderlandEventTrigger trigger) {
        triggers.add(trigger);
        for(Class evt : trigger.getEventClasses())
            triggerEvents.add(evt);

        synchronized(triggerEvents) {
            triggerEventsArray = triggerEvents.toArray(new Class[triggerEvents.size()]);
        }
    }
}

