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
package org.jdesktop.wonderland.modules.testcells.client.timingframework.util;

import com.sun.scenario.animation.Clip;
import com.sun.scenario.animation.util.Trigger;
import com.sun.scenario.animation.util.TriggerEvent;
import java.util.HashSet;
import org.jdesktop.wonderland.client.input.Event;

/**
 *
 * @author paulby
 */
public abstract class WonderlandEventTrigger extends Trigger {

    private HashSet<Class<? extends Event>> eventSet = new HashSet();
    
    public WonderlandEventTrigger(Clip clip) {
        super(clip);
    }

    public WonderlandEventTrigger(Clip clip, TriggerEvent evt) {
        super(clip, evt);
    }
    
    protected void addEventClass(Class<? extends Event> eventClass) {
        eventSet.add(eventClass);
    }

    public Iterable<Class<? extends Event>> getEventClasses() {
        return eventSet;
    }

    public void commitEvent(Event evt) {
        // TODO Should this be instanceof (assignableFrom) check ?
        if (eventSet.contains(evt.getClass()))
            processEvent(evt);
    }

    public abstract void processEvent(Event evt);

}
