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
package org.jdesktop.wonderland.client.jme.input;

/**
 * The abstract super class for all Wonderland mouse events.
 *
 * @author deronj
 */

public abstract class MouseEvent3D extends InputEvent3D {

    /** The supported button codes. */
    public enum ButtonId {NOBUTTON, BUTTON1, BUTTON2, BUTTON3};
    
    /** The originating AWT mouse event. */
    protected MouseEvent awtEvent;

    /** The pick data for the event */
    private PickData pickData;

    /**
     * Convert MouseEvent into a MouseEvent3D and change the event id to newID
     * @param awtEvent The originating AWT mouse event
     * @param pickData The pick info for the event.
     */
    public MouseEvent3D (MouseEvent awtEvent, PickData pickData) {
	this.awtEvent = awtEvent;
	this.pickData = pickData;
    }
    
    /**
     * {@inheritDoc}
     */
    public InputEvent getAwtEvent() {
        return awtEvent;
    }
    
    /**
     * Returns the pick data of the event.
     */
    public PickData getPickData () {
	return pickData;
    }

    /**
     * Sets the pick data of the event.
     */
    void setPickData (PickData pickData) {
	this.pickData = pickData;
    }

    /**
     * Returns the entity hit by the event.
     */
    public Entity getEntity () {
	return EntityResolver.pickDataToEntity(pickData);
    }

    /**
     * Returns which, if any, of the mouse buttons has changed state.
     * @return one of the following enums: NOBUTTON, BUTTON1, BUTTON2 or BUTTON3.
     */
    public ButtonId getButton () {
        ButtonId ret = ButtonId.NOBUTTON;
        
        int button = awtEvent.getButton();
        switch (button) {
            case MouseEvent.BUTTON1:
                ret = ButtonId.BUTTON1;
                break;
            case MouseEvent.BUTTON2:
                ret = ButtonId.BUTTON2;
                break;
            case MouseEvent.BUTTON3:
                ret = ButtonId.BUTTON3;
                break;
            default:
                assert(button == MouseEvent.NOBUTTON);
        }
        
	return ret;
    }

    // TODO: add utilities for getting at things inside the pick data, such as local and world intersection point
    // and the window coordinates
}
