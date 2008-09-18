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
 * The base implementation of a Wonderland event listener.
 *
 * @author deronj
 */

@ExperimentalAPI
public class EventListenerBaseImpl extends Processor implements EventListener  {

    /** The mtgame event used to wake up the this collection for computation on the Event */
    // TODO: need a way to ensure this is unique throughout the client
    private static long MTGAME_EVENT_ID = 0;

    /** The input event queue for this collection. The listeners will be called for these. */
    private LinkedBlockingQueue<Event> inputQueue = new LinkedBlockingQueue<Event>();

    /** Whether the listener is enabled. */
    protected boolean enabled = true;

    /** The number of entities to which the listener is attached */
    private numEntitiesAttached;

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
	this.enable = enable;

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
     *
     * Note on subclassing: unless a subclass overrides this method, true is always returned.
     */
    public boolean consumeEvent (Event event) {
	return true;
    }

    /**
     * {@inheritDoc}
     *
     * Note on subclassing: unless a subclass overrides this method, true is always returned.
     */
    public boolean propagateToParent (Event event) {
	return true;
    }

    /**
     * {@inheritDoc}
     *
     * Note on subclassing: unless a subclass overrides this method, true is always returned.
     */
    public boolean propagateToUnder (Event event) {
	return false;
    }

    /**
     * {@inheritDoc}
     */
    public void computeEvent (Event event) {};

    /**
     * {@inheritDoc}
     */
    public void commitEvent (Event event, Object computeData) {};

    /**
     * {@inheritDoc}
     */
    public void addToEntity (Entity entity) {

	// Make sure that the entity has an event listener collection to use as an attach point.
	EventListenerCollection collection = entity.getComponent(EventListenerCollection.class);
	if (collection == null) {
	    collection = new EventListenerCollection();
	    entity.addComponent(EventListenerCollection.class);
	} else {

	    // See if listener is already attached to this entity
	    if (collection.get(this) != null) {
		return;
	    }

	    collection.addListener(this);
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

	if (!attached)
	EventListenerCollection collection = entity.getComponent(EventListenerCollection.class);
	if (collection == null) {
	    return;
	}

	// Remove listener from collection, seeing if listener was attached to this entity
	if (collection.remove(this) == null) {
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
     * Deliver the given event to this collection. This is only ever called by the EventDeliverer.
     */
    @InternalAPI
    void postEvent (Event event) {
	if (!enable) return;
	inputQueue.add(event);
	JmeClientMain.getWorldManager().postEvent(MTGAME_EVENT_ID);
    }

    /**
     * {@inheritDoc}
     * Called when there is new event in the input queue.
     */
    @InternalAPI
    public void compute (ProcessorArmingCollection collection) {
	Event event = inputQueue.take();
	if (event == null) return;
	computeEvent(event);
	lastComputedEvent = event;
    }

    /**
     * {@inheritDoc}
     * Called in the render loop to allow this collection to commit the 
     * compute() calculations to alter the scene graph.
     */
    @InternalAPI
    public void commit (ProcessorArmingCollection collection) {
	commitEvent(lastComputedEvent);
    }

    /** Arm the processor */
    private void arm () {
	setArmingCondition(new ProcessorArmingCondition(this, new long[] { MTGAME_EVENT_ID}));
    }

    /** Disarm the processor */
    private void disarm () {
	setArmingCondition(null);
    }
}
