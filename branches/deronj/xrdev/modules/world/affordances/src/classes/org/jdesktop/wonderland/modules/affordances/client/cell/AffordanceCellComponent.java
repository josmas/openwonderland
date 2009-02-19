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
package org.jdesktop.wonderland.modules.affordances.client.cell;

import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.cell.CellComponent;
import org.jdesktop.wonderland.client.input.Event;
import org.jdesktop.wonderland.client.input.EventClassListener;
import org.jdesktop.wonderland.client.input.InputManager;
import org.jdesktop.wonderland.modules.affordances.client.event.AffordanceRemoveEvent;

/**
 * A client-side cell component base class for affordance components.
 * 
 * @author Jordan Slott <jslott@dev.java.net>
 */
public abstract class AffordanceCellComponent extends CellComponent {
    private AffordanceCloseListener listener = null;
    private float size = 1.5f;

    public AffordanceCellComponent(Cell cell) {
        super(cell);
        InputManager.inputManager().addGlobalEventListener(listener = new AffordanceCloseListener());
    }

    /**
     * Sets the size of the affordance. The size is a floating point value where
     * 1.0 designates the same size of the cell.
     *
     * @param size The new size of the affordance
     */
    public void setSize(float size) {
        this.size = size;
    }

    /**
     * Returns the current size of the afforance.
     *
     * @return The size of the affordance
     */
    public float getSize() {
        return this.size;
    }

    /**
     * Remove the affordance component from the cell
     */
    public void remove() {
        InputManager.inputManager().removeGlobalEventListener(listener);
    }

    /**
     * Inner class that listens for an event signalling that the afforance
     * should remove itself
     */
    class AffordanceCloseListener extends EventClassListener {

        @Override
        public Class[] eventClassesToConsume() {
            return new Class[] { AffordanceRemoveEvent.class };
        }


        @Override
        public void commitEvent(Event event) {
            // Just tell the affordance to remove itself
            remove();
            InputManager.inputManager().removeGlobalEventListener(this);
        }
    }
}
