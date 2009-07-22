    /*
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
package org.jdesktop.wonderland.client.hud;

import java.util.Date;

/**
 * Describes an event that applies to a HUDComponent.
 *
 * @author nsimpson
 */
public class HUDComponentEvent {

    private HUDComponent component;
    private ComponentEventType eventType;
    private Date eventTime;

    /**
     * Types of HUD component events
     */
    public enum ComponentEventType {

        /**
         * A HUD component has been created
         */
        CREATED,
        /**
         * A HUD component is visible on the HUD
         */
        APPEARED,
        /**
         * A HUD component is visible in world
         */
        APPEARED_WORLD,
        /**
         * A HUD component is no longer visible on the HUD
         */
        DISAPPEARED,
        /**
         * A HUD component is no longer visible in world
         */
        DISAPPEARED_WORLD,
        /**
         * A HUD component has changed display modes
         */
        CHANGED_MODE,
        /**
         * A HUD component has moved on the HUD
         */
        MOVED,
        /**
         * A HUD component has moved in world
         */
        MOVED_WORLD,
        /**
         * A HUD component has resized
         */
        RESIZED,
        /**
         * A HUD component is minimized
         */
        MINIMIZED,
        /**
         * A HUD component is maximized
         */
        MAXIMIZED,
        /**
         * A HUD component is iconified
         */
        ICONIFIED,
        /**
         * A HUD component is enabled
         */
        ENABLED,
        /**
         * A HUD component is disabled
         */
        DISABLED,
        /**
         * A HUD component has been closed
         */
        CLOSED
    };

    /**
     * Create a new instance of a HUD Component Event
     * @param component the HUD component associated with this event
     */
    public HUDComponentEvent(HUDComponent component) {
        this.component = component;
    }

    /**
     * Create a new instance of a HUD Component Event
     * @param component the HUD component associated with this event
     * @param eventType the event type
     */
    public HUDComponentEvent(HUDComponent component, ComponentEventType eventType) {
        this.component = component;
        this.eventType = eventType;
    }

    /**
     * Create a new instance of a HUD Component Event
     * @param component the HUD component associated with this event
     * @param eventType the event type
     * @param eventTime the time of the event
     */
    public HUDComponentEvent(HUDComponent component, ComponentEventType eventType,
            Date eventTime) {
        this.component = component;
        this.eventType = eventType;
        this.eventTime = eventTime;
    }

    /**
     * Sets the component that triggered the event
     * @param component the component that triggered the event
     */
    public void setComponent(HUDComponent component) {
        this.component = component;
    }

    /**
     * Gets the component that triggered the event
     * @return the component that triggered the event
     */
    public HUDComponent getComponent() {
        return component;
    }

    /**
     * Sets the event type
     * @param eventType the type of the event
     */
    public void setEventType(ComponentEventType eventType) {
        this.eventType = eventType;
    }

    /**
     * Gets the type of the event
     * @return the event type
     */
    public ComponentEventType getEventType() {
        return eventType;
    }

    /**
     * Sets the time that the event occurred
     * @param eventTime the time of the event
     */
    public void setEventTime(Date eventTime) {
        this.eventTime = eventTime;
    }

    /**
     * Gets the time the event occurred
     * @return the time of the event
     */
    public Date getEventTime() {
        return eventTime;
    }

    @Override
    public String toString() {
        return "component: " + component + ", event type: " + eventType +
                ", event time: " + eventTime;
    }
}
