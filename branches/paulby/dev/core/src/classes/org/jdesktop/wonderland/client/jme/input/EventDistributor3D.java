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

import org.jdesktop.wonderland.client.input.Event;
import org.jdesktop.wonderland.client.input.EventDistributor;
import org.jdesktop.mtgame.PickInfo;
import org.jdesktop.mtgame.PickDetails;
import java.util.logging.Logger;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.wonderland.client.input.FocusChangeEvent;
import org.jdesktop.wonderland.client.input.InputManager;
import org.jdesktop.wonderland.client.input.InputPicker;
import org.jdesktop.wonderland.common.InternalAPI;

/**
 * The part of the input subsystem which distributes events throughout the scene graph 
 * according to the information provided by the entity event listeners.
 *
 * @author deronj
 */

@InternalAPI
public class EventDistributor3D extends EventDistributor implements Runnable {

    private static final Logger logger = Logger.getLogger(EventDistributor3D.class.getName());

    private EventDistributor.PropagationState propState = new PropagationState();

    /** The pick info for the last mouse event */
    private PickInfo mousePickInfoPrev;

    /** The singleton event distributor */
    private static EventDistributor eventDistributor;
    
    /** Return the event distributor singleton */
    static EventDistributor getEventDistributor () {
	if (eventDistributor == null) {
	    eventDistributor = new EventDistributor3D();
	    ((EventDistributor3D)eventDistributor).start();
	}
	return eventDistributor;
    }

    protected void processEvent (Event event, PickInfo pickInfo) {
	if (event instanceof MouseEnterExitEvent3D) {
	    processEnterExitEvent(event, pickInfo);
	} else if (event instanceof MouseEvent3D) {
	    processMouseKeyboardEvent(event, pickInfo);
	} else if (event instanceof KeyEvent3D) {
	    if (mousePickInfoPrev != null) {
		processMouseKeyboardEvent(event, mousePickInfoPrev);
	    }
	} else if (event instanceof FocusChangeEvent) {
	    processFocusChangeEvent(((FocusChangeEvent)event).getChanges());
	} else {
	    logger.warning("Invalid event type encountered, event = " + event);
	}
    }

    protected void processMouseKeyboardEvent (Event event, PickInfo pickInfo) {
	logger.info("Distributor: received event = " + event + ", pickInfo = " + pickInfo);

	// Track the last mouse pick info for focus-follows-mouse keyboard focus policy
	if (event instanceof MouseEvent3D) {
	    mousePickInfoPrev = pickInfo;
	    MouseEvent3D mouseEvent = (MouseEvent3D) event;
	    if (mouseEvent.getAwtEvent() instanceof InputManager.NondeliverableMouseEvent) {
		return;
	    }
	}
	
	// First try the global event listeners
	// TODO: clone
	if (event instanceof MouseEvent3D) {
	    ((MouseEvent3D)event).setPickDetails((PickDetails)null);
	}
	tryGlobalListeners(event);

	// Start out the entity search assuming no propagation to unders
	propState.toUnder = false;

	// Walk through successive depth levels, as long as propagateToUnder is true,
	// searching up the parent chain in each level
	PickDetails pickDetails = pickInfo.get(0);
	logger.fine("pickDetails = " + pickDetails);
	int idx = 0;
	while (true) {
	    
	    // Start this loop interation out assuming no propagation to parents
	    propState.toParent = false; 

	    // See whether the picked entity wants the event.
	    if (event instanceof MouseEvent3D) {
		((MouseEvent3D)event).setPickDetails(pickDetails);
		
	    }
	    Entity entity = InputPicker.pickDetailsToEntity(pickDetails);
	    tryListenersForEntity(entity, event, propState);

	    // See whether any of the picked entity's parents want the event
	    if (propState.toParent) {
		logger.fine("Propogating to parents");
		tryListenersForEntityParents(entity.getParent(), event, propState);
	    }

	    if (!propState.toUnder) {
		// No more propagation to unders. We're done.
		break;
	    }

	    logger.fine("Propagate to next under");

	    idx++;
	    if (idx >= pickInfo.size()) {
		// No more picked objects underneath. We're done.
		break;
	    }
	    
	    pickDetails = pickInfo.get(idx);
	    logger.fine("pickDetails = " + pickDetails);
	}
    }

    private void processEnterExitEvent (Event event, PickInfo pickInfo) {
	// TODO
    }

}