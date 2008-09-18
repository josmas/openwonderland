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
 * The abstract base class for an Event Distributor singleton. The Entity Distributor is part of the input 
 * subsystem which distributes events throughout the scene graph according to the information provided by 
 * the entity event listeners. It also supports a set of global event listeners. This are independent of 
 * any events. Events are always tried to be delivered to the global event listeners. In general, the user
 * cannot make any assumptions about the order in which individual event listeners methods are called.
 *
 * @author deronj
 */

@ExperimentalAPI
class EntityDistributor implements Runnable {

    /** The current event mode */
    private static eventMode = EventMode.WORLD;
    
    /** The base input queue entry. */
    private abstract static class Entry {
	/** The Wonderland event. */
	Event event;
	/** The pick results for the event */
	PickResults pickResults;
        Entry (Event event, PickResults pickResults) {
	    this.event = event;
	    this.pickResults = pickResults
	}
    }

    private Thread thread;

    private LinkedBlockingQueue<Entry> inputQueue = new LinkedBlockingQueue<Entry>();

    private static EventDistributor eventDistributor;

    private EventListenerCollection globalEventListeners = new EventListenerCollection();

    abstract static EventDistributor getEventDistributor ();

    private void start () {
	thread = new Thread(this, "EventDistributor");
	thread.start();
    }

    /**
     * Add a Wonderland event to the event distribution queue, with null pick results.
     * @param event The event to enqueue.
     */
    void enqueueEvent (Event event) {
	inputQueue.add(new Entry(event, null));
    }

    /**
     * Add a Wonderland event to the event distribution queue.
     * @param event The event to enqueue.
     * @param pickResults The pick results for the event.
     */
    void enqueueEvent (Event event, PickResults pickResults) {
	inputQueue.add(new Entry(event, pickResults));
    }

    /**
     * The run loop of the thread.
     */
    public void run () {
	while (true) {
	    Entry entry = inputQueue.take();
	    if (entry.event instanceof EventModeEvent) {
		processEventModeEvent(entry.event);
	    } else {
		processEvent(entry.event, entry.pickResults);
	    }
	}
    }
    
    /** 
     * The responsibility for determining how to process individual event types is delegated to the subclass.
     * @param event The event to try to deliver to event listeners.
     * @param pickResults The pick results associated with the event.
     */
    protected abstract processEvent (Event event, PickResults pickResults);

    /** The state of the propagation state during the traversal. */
    protected class PropagationState {
	private boolean toParent;
	private boolean toUnder;
    }

    /** 
     * See if any of the enabled event listeners for the given entity are willing to consume the given event.
     * Post the event to willing consumers. Also, returns the OR of propagateToParent for all enabled listeners 
     * and the OR of propagateToUnder for all enabled listeners in propState.
     */
    private void tryListenersForEntity (Entity entity, Event event, PropagationState propState) {
	EventListenerCollection listeners = entity.getComponent(EventListenerCollection.class);
	if (listeners == null) { 
	    // propagateToParent is true, so OR makes no change to its accumulator
	    // propagateToUnder is false, so OR makes its accumulator is false
	    propState.toUnder = false;
	} else {
	    for (EventListener listener : listeners) {
		if (listener.enabled()) {
		    if (listener.consumeEvent(event, entity)) {
			listener.postEvent(event);
		    }
		    propState.toParent |= listener.propagateToParent(event, entity);
		    propState.toUnder |= listener.propagateToUnder(event, entity);
		}
	    }
	}
    }

    /** 
     * Traverse the entity parent chain (starting with the given entity) to see if the
     * event should be delivered to any of their listeners. Continue the search until no more parents are found.
     * Post the event to willing consumers. Also, returns the OR of propagateToParent for all enabled listeners 
     * and the OR of propagateToUnder for all enabled listeners in propState.
     */
    private void tryListenersForEntityParents (Entity entity, Event event, PropagationState propState) {
	while (entity != null) {
	    tryListenersForEntity(entity, event, propState);
	    if (!propState.toParent) {
		// No more propagation to parents. We're done with this loop.
		break;
	    }
	    entity = /* TODO: notyet: entity.getParent()*/ null;
	}
    }
	
    /** 
     * Traverse the list of global listeners to see whether the event should be delivered to any of them. 
     * Post the event to willing consumers. Note: the return values of propagateToParent() and propagateToUnder() 
     * for global listeners are ignored.
     */
    private void tryGlobalListeners (Event event) {
	synchronized (globalEventListeners) {
	    for (EventListener listener : globalEventListeners) {
		if (listener.enabled()) {
		    if (listener.consumeEvent(event, null)) {
			listener.postEvent(event);
		    }
		}
	    }
	}
    }
	

    /**
     * Returns the current event mode.
     */
    static EventMode getEventMode () {
	synchronized (eventMode) {
	    return eventMode;
	}
    }

    /**
     * Returns the current event mode.
     */
    static void setEventMode (EventMode eventMode) {
	enqueueEvent(new EventModeEvent(eventMode));
    }

    private void processEventModeEvent (Event event) {
	EventModeEvent eme = (EventModeEvent) event;
	EventMode newMode = eme.getEventMode();
	synchronized (eventMode) {
	    eventMode = newMode;
	}	
    }

    /**
     * Add an event listener to be tried once per event. This global listener can be added only once.
     * Subsequent attempts to add it will be ignored.
     *
     * Note: It is not a good idea to call this from inside EventListener.computeEvent function.
     * However, it is okay to call this from inside EventListener.commitEvent function if necessary.
     *
     * @param Listener The global event listener to be added.
     */
    public void addGlobalEventListener (EventListener listener) {
	synchronized (globalEventListeners) {
	    if (globalEventListeners.get(listener) != null) {
		return null;
	    } else {
		globalEventListeners.add(listener);
	    }
	}
    }

    /**
     * Remove this global event listener.
     *
     * Note: It is not a good idea to call this from inside EventListener.computeEvent function.
     * However, it is okay to call this from inside EventListener.commitEvent function if necessary.
     *
     * @param The entity to which to attach this event listener.
     */
    public void removeGlobalEventListener (EventListener listener) {
	synchronized (globalEventListeners) {
	    globalEventListeners.remove(listener);
	}
    }
}
