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

import java.awt.event.KeyEvent;
import java.awt.event.InputEvent;

/**
 * An event which indicates that a keystroke occurred in a component.
 *
 * REMINDER: we may introduce KeyTypedEvent3D and KeyPressedEvent3D
 * that are subclass of this class and implement getKeyChar() and 
 * getKeyCode() respectively.
 *
 * @author deronj
 */

@ExperimentalAPI
class KeyEvent3D extends InputEvent3D {

    static {
	/** Allocate this event type's ID. */
	EVENT_ID = Event.allocateEventID();
    }

    /** The originating AWT key event */
    private KeyEvent awtEvent;
    
    /** 
     * Create a new instance of KeyEvent3D.
     * @param awtEvent The originating AWT key event.
     */
    KeyEvent3D (KeyEvent awtEvent) {
        this.awtEvent = awtEvent;
    }
    
    /**
     * {@inheritDoc}
     */
    public InputEvent getAwtEvent() {
        return awtEvent;
    }
    
    /**
     * Returns true if this event is a key type.
     */
    public boolean isTyped () {
        return (awtEvent.getID() == KeyEvent.KEY_TYPED);
    }
    
    /**
     * Returns true if this event is a key press.
     */
    public boolean isPressed () {
        return (awtEvent.getID() == KeyEvent.KEY_PRESSED);
    }
    
    /**
     * Returns true if this event is a key release.
     */
    public boolean isReleased() {
        return (awtEvent.getID() == KeyEvent.KEY_RELEASED);
    }
    
    /**
     * Returns the character associated with the key in this event.
     * For example, the KEY_TYPED event for shift + "a" returns
     * the value for "A".
     *
     * KEY_PRESSED and KEY_RELEASED events are not intended for 
     * reporting of character input. Therefore, the values returned
     * by this method are guaranteed to be meaningful only for
     * KEY_TYPED events.
     *
     * @return the Unicode character defined for this key event.
     * If no valid Unicode character exists for this key event,
     * CHAR_UNDEFINED is returned.
     */
    public char getKeyChar () {
        return awtEvent.getKeyChar();
    }
    
    /**
     * Returns the integer keyCode associated with the key in this event.
     * The keyCode is the same as that of the originating AWT event.
     *
     * @return the integer code for an actual key on the keyboard.
     * (For KEY_TYPED events, the keyCode is VK_UNDEFINED.)
     */
    public int getKeyCode () {
        return awtEvent.getKeyCode();
    }
}
