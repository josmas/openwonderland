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
 * The part of the input subsystem which distributes events throughout the scene graph 
 * according to the information provided by the entity event listeners.
 *
 * @author deronj
 */

@InternalAPI
public class EntityDistributor3D implements Runnable {

    private PropagationState propState = new PropagationState();

    private static EventDistributor getEventDistributor () {
	if (eventDistributor == null) {
	    eventDistributor = new EventDistributor3D();
	    eventDistributor.start();
	}
	return eventDistributor;
    }

    protected abstract processEvent (Event event, PickResults pickResults) {
	if (entry.event instanceof MouseEnterExitEvent3D) {
	    processEnterExitEvent(entry.event, entry.pickResults);
	} else if (entry.event instanceof MouseEvent3D) {
	    processMouseEvent(entry.event, entry.pickResults);
	} if (entry.event instanceof KeyEvent3D) {
	    processKeyEvent(entry.event);
	} else {
	    logger.warning("Invalid event type encountered, event = " + entry.event);
	}
    }

    protected void processMouseEvent (Event event, PickResults pickResults) {

	// First try the global event listeners
	event.setPickData(null);
	tryGlobalListeners(event);

	// Start out the entity search assuming no propagation to unders
	propState.toUnder = false;

	// Walk through successive depth levels, as long as propagateToUnder is true,
	// searching up the parent chain in each level
	PickData pickData = pickResults.getPickData(0);
	int idx = 0;
	while (true) {
	    
	    // Start this loop interation out assuming no propagation to parents
	    propState.toParent = false; 

	    // See whether the picked entity wants the event.
	    event.setPickData(pickData);
	    Entity entity = EventResolver.pickDataToEntity(pickData);
	    tryListenersForEntity(entity, event, propState);

	    // See whether any of the picked entity's parents want the event
	    if (propState.toParent) {
		tryListenersForEntityParents(/* TODO: notyet: entity.getParent()*/ null, event, propState);
	    }

	    if (!propState.toUnder) {
		// No more propagation to unders. We're done.
		break;
	    }

	    idx++;
	    if (idx >= pickResults.getNumber()) {
		// No more picked objects underneath. We're done.
		break;
	    }
	    
	    pickData = restPickResults.getPickData(idx);
	}
    }

    private void processEnterExitEvent (Event event, PickResults pickResults) {
	// TODO
    }

    private void processKeyEvent (Event event) {
	// TODO
    }
}