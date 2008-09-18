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
package org.jdesktop.wonderland.client.input;

/**
 * The attach point for event listeners on an entity.
 *
 * @author deronj
 */

@InternalAPI
class EventListenerCollection extends LinkedList<EventListener> {

    /**
     * Add an event listener, if the listener isn't already added.
     * @param listener The listener to add.
     */
    void synchronized addListener (EventListener listener) {
	if (get(listener) != null) return;
	add(listener);
    }

    /**
     * Remove an event listener.
     * @param listener The listener to remove.
     */
    void synchronized removeListener (EventListener listener) {
	remove(listener);
    }
}
