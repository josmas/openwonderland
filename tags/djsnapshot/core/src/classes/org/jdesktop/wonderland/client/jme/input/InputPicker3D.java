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
package org.jdesktop.wonderland.client.jme.input;

import java.awt.AWTEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.FocusEvent;
import org.jdesktop.wonderland.client.input.Event;
import org.jdesktop.wonderland.client.input.InputPicker;
import org.jdesktop.wonderland.common.InternalAPI;

/**
 * A specific implementation of <code>InputPicker</code> which uses 3D events.
 *
 * @author deronj
 */

@InternalAPI
public class InputPicker3D extends InputPicker {

    /** The input picker singleton */
    private static InputPicker inputPicker;

    /**
     * Returns the entity resolver singleton.
     */
    static InputPicker getInputPicker() {
        if (inputPicker == null) {
            inputPicker = new InputPicker3D();
        }
        return inputPicker;
    }

    /**
     * {@inheritDoc}
     */
    @InternalAPI
    public Event createWonderlandEvent(AWTEvent awtEvent) {
        Event event = null;

        if (awtEvent instanceof KeyEvent) {
            event = new KeyEvent3D((KeyEvent) awtEvent);
        } else if (awtEvent instanceof FocusEvent) {
            event = new FocusEvent3D((FocusEvent) awtEvent);
        } else if (awtEvent instanceof MouseWheelEvent) {
            event = new MouseWheelEvent3D((MouseWheelEvent) awtEvent);
        } else if (awtEvent instanceof MouseEvent) {
            switch (awtEvent.getID()) {
                case MouseEvent.MOUSE_CLICKED:
                case MouseEvent.MOUSE_RELEASED:
                case MouseEvent.MOUSE_PRESSED:
                    event = new MouseButtonEvent3D((MouseEvent) awtEvent);
                    break;
                case MouseEvent.MOUSE_ENTERED:
                case MouseEvent.MOUSE_EXITED:
                    event = new MouseEnterExitEvent3D((MouseEvent) awtEvent);
                    break;
                case MouseEvent.MOUSE_MOVED:
                    event = new MouseMovedEvent3D((MouseEvent) awtEvent);
                    break;
                case MouseEvent.MOUSE_DRAGGED:
                    event = new MouseDraggedEvent3D((MouseEvent) awtEvent);
                    break;
                default:
                    logger.warning("Invalid AWT event type");
            }
        }

        return event;
    }
}
