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

import com.sun.scenario.animation.util.TriggerEvent;
import org.jdesktop.wonderland.client.input.Event;
import org.jdesktop.wonderland.client.jme.input.MouseButtonEvent3D;
import org.jdesktop.wonderland.client.jme.input.MouseEnterExitEvent3D;

/**
 * Mouse Enter/Exit/Press/Release/Click events
 *
 * @author paulby
 */
public class Mouse3DTriggerEvent extends TriggerEvent {
    
    private Class<? extends Event> event;
    
    /**
     * Event fired when mouse enters
     */
    public static final Mouse3DTriggerEvent ENTER =
            new Mouse3DTriggerEvent("Entered", MouseEnterExitEvent3D.class);
    /**
     * Event fired when mouse exits
     */
    public static final Mouse3DTriggerEvent EXIT =
            new Mouse3DTriggerEvent("Exit", MouseEnterExitEvent3D.class);
    /**
     * Event fired when mouse button is pressed
     */
    public static final Mouse3DTriggerEvent PRESS =
            new Mouse3DTriggerEvent("Press", MouseButtonEvent3D.class);
    /**
     * Event fired when mouse button is released
     */
    public static final Mouse3DTriggerEvent RELEASE =
            new Mouse3DTriggerEvent("Release", MouseButtonEvent3D.class);
    /**
     * Event fired when mouse is clicked
     */
    public static final Mouse3DTriggerEvent CLICK =
            new Mouse3DTriggerEvent("Click", MouseButtonEvent3D.class);

    /**
     * Private constructor; this helps ensure type-safe use of 
     * pre-define TriggerEvent objects.
     */
    private Mouse3DTriggerEvent(String name, Class<? extends Event> event) {
        super(name);
        this.event = event;
    }
    
    Class<? extends Event> getEventClass() {
        return event;
    }

    /**
     * This method finds the opposite of the current event.
     * <pre>
     *   ENTER   -> EXIT
     *   EXIT    -> ENTER
     *   PRESS   -> RELEASE
     *   RELEASE -> PRESS
     * </pre>
     * Note that {@code CLICK} has no obvious opposite so
     * it simply returns {@code CLICK} (this method should probably
     * not be called for that case).
     */
    @Override
    public TriggerEvent getOppositeEvent() {
        if (this == Mouse3DTriggerEvent.ENTER) {
            return Mouse3DTriggerEvent.EXIT;
        } else if (this == Mouse3DTriggerEvent.EXIT) {
            return Mouse3DTriggerEvent.ENTER;
        } else if (this == Mouse3DTriggerEvent.PRESS) {
            return Mouse3DTriggerEvent.RELEASE;
        } else if (this == Mouse3DTriggerEvent.RELEASE) {
            return Mouse3DTriggerEvent.PRESS;
        }
        // Possible to reach here for REPEAT action (but probably should not
        // have been called with this event)
        return this;
    }
}
