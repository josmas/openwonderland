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

import java.util.concurrent.LinkedBlockingQueue;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.mtgame.ProcessorArmingCollection;
import org.jdesktop.mtgame.PostEventCondition;
import org.jdesktop.mtgame.ProcessorComponent;
import org.jdesktop.wonderland.client.jme.ClientContextJME;
import org.jdesktop.wonderland.common.ExperimentalAPI;
import org.jdesktop.wonderland.common.InternalAPI;

/**
 * The base implementation of a Wonderland event listener. Almost all custom event listeners should
 * extend this class.
 *
 * @author deronj
 */

@ExperimentalAPI
public class EventListenerBaseImpl extends ProcessorComponent implements EventListener  {

    /** The mtgame event used to wake up the this collection for computation on the Event */
    // TODO: need a way to ensure this is unique throughout the client
    private static long MTGAME_EVENT_ID = 0;

    /** The input event queue for this collection. The listeners will be called for these. */
    private LinkedBlockingQueue<Event> inputQueue = new LinkedBlockingQueue<Event>();

    /** Whether the listener is enabled. */
    protected boolean enabled = true;

    /** The number of entities to which the listener is attached */
    private int numEntitiesAttached;

    /** The last event for which computeEvent was called */
    private Event lastComputedEvent;

    /**
     * {@inheritDoc}
     * Note: A listener is enabled by default.
     */
    public boolean isEnabled () {
	return enabled;
    }

    /**
     * {@inheritDoc}
     */
    public void setEnable (boolean enable) {
	this.enabled = enabled;

	if (enabled && numEntitiesAttached > 0) {
	    // Make sure listener is armed if it is enabled and attached.
	    arm();
	} else if (!enabled || numEntitiesAttached <= 0) {
	    // Make sure listener is disarmed if it is disabled or detached.
	    disarm();
	}

	// Discard pending input events if listener is being disabled.
	if (!enable) {
	    inputQueue.clear();
	}
    }

    /**
     * {@inheritDoc}
     * <br><br>
     * Note on subclassing: unless a subclass overrides this method, true is always returned.
     */
    public boolean consumeEvent (Event event, Entity entity) {
	return true;
    }

    /**
     * {@inheritDoc}
     * <br><br>
     * Note on subclassing: unless a subclass overrides this method, true is always returned.
     */
    public boolean propagateToParent (Event event, Entity entity) {
	return true;
    }

    /**
     * {@inheritDoc}
     * <br><br>
     * Note on subclassing: unless a subclass overrides this method, true is always returned.
     */
    public boolean propagateToUnder (Event event, Entity entity) {
	return false;
    }

    /**
     * {@inheritDoc}
     */
    public void computeEvent (Event event, Entity entity) {}

    /**
     * {@inheritDoc}
     */
    public void commitEvent (Event event, Entity entity) {}

    /**
     * {@inheritDoc}
     */
    public void addToEntity (Entity entity) {

	// Make sure that the entity has an event listener collection to use as an attach point.
	EventListenerCollection collection = (EventListenerCollection) entity.getComponent(EventListenerCollection.class);
	if (collection == null) {
	    collection = new EventListenerCollection();
	    entity.addComponent(EventListenerCollection.class, collection);
	} else {

	    // See if listener is already attached to this entity
	    if (collection.contains(this)) {
		return;
	    }

	    collection.add(this);
	}	

	numEntitiesAttached++;

	// Arm on first attach (if listener is enabled)
	if (numEntitiesAttached == 1 && enabled) {
	    arm();
	}
    }

    /**
     * {@inheritDoc}
     */
    public void removeFromEntity (Entity entity) {

	EventListenerCollection collection = (EventListenerCollection) entity.getComponent(EventListenerCollection.class);
	if (collection == null) {
	    return;
	}

	// Remove listener from collection, seeing if listener was attached to this entity
	if (!collection.remove(this)) {
	    // Listener was not in this collection
	}
	
	if (collection.size() <= 0) {
	    entity.removeComponent(EventListenerCollection.class);
	}

	numEntitiesAttached--;

	// Disarm on last detach
	if (numEntitiesAttached <= 0) {
	    disarm();
	}
    }

    /**
     * INTERNAL ONLY
     * <br><br>
     * Deliver the given event to this collection. This is only ever called by the EventDeliverer.
     */
    @InternalAPI
    public void postEvent (Event event) {
	if (!enabled) return;
	inputQueue.add(event);
	ClientContextJME.getWorldManager().postEvent(MTGAME_EVENT_ID);
    }

    /**
     * {@inheritDoc}
     */
    public void initialize() {}
    
    /**
     * INTERNAL ONLY
     * <br><br>
     * {@inheritDoc}
     * <br><br>
     * Called when there is new event in the input queue.
     */
    @InternalAPI
    public void compute (ProcessorArmingCollection collection) {
	Event event = null;
        try {
            event = inputQueue.take();
        } catch (Exception ex) {}
	if (event == null) return;
	computeEvent(event, getEntity());
	lastComputedEvent = event;
    }

    /**
     * INTERNAL ONLY
     * <br><br>
     * {@inheritDoc}
     * <br><br>
     * Called in the render loop to allow this collection to commit the 
     * <code>compute()</code> calculations to alter the scene graph.
     */
    @InternalAPI
    public void commit (ProcessorArmingCollection collection) {
	commitEvent(lastComputedEvent, getEntity());
    }

    /** Arm the processor */
    private void arm () {
	setArmingCondition(new PostEventCondition(this, new long[] { MTGAME_EVENT_ID}));
    }

    /** Disarm the processor */
    private void disarm () {
	setArmingCondition(null);
    }
}