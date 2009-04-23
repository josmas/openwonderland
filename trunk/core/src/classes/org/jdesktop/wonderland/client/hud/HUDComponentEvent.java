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

    // the types of HUDComponent events
    public enum ComponentEventType {

        CREATED,
        APPEARED,
        APPEARED_WORLD,
        DISAPPEARED,
        DISAPPEARED_WORLD,
        CHANGED_MODE,
        MOVED,
        MOVED_WORLD,
        RESIZED,
        MINIMIZED,
        MAXIMIZED,
        ICONIFIED,
        ENABLED,
        DISABLED
    };

    public HUDComponentEvent(HUDComponent component) {
        this.component = component;
    }

    public HUDComponentEvent(HUDComponent component, ComponentEventType eventType) {
        this.component = component;
        this.eventType = eventType;
    }

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
