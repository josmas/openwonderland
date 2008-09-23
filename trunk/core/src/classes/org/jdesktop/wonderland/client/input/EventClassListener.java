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

import com.jme.entity.Entity;
import org.jdesktop.wonderland.common.InternalAPI;
import org.jdesktop.wonderland.common.ExperimentalAPI;

/**
 * A simplified event listener which provides the input system with an array of event classes.
 * By doing this the listener is telling the input system that it wishes to receive only events of these classes.
 */

@ExperimentalAPI
public class EventClassListener extends EventListenerBaseImpl {

    /**
     * Note on subclassing: the subclass should override this method.
     * @return An array of the event classes the listener wishes to consume.
     */
    public Class[] eventClassesToConsume () {
	return null;
    }

    /**
     * INTERNAL ONLY.
     */
    @InternalAPI
    public boolean consumeEvent (Event event, Entity entity) {
        Class<Event>[] eventClasses = eventClassesToConsume();
	if (eventClasses == null) return false;
	for (Class eventClass : eventClasses) {
            if (!Event.class.isAssignableFrom(eventClass)) {
                throw new IllegalArgumentException(
		   "Method eventClassesToConsume must return classes which extend the main Wonderland event class");
            }
            if (event.getClass().isAssignableFrom(eventClass)) {
	        return true;
            }
        }
        return false;
    }

}