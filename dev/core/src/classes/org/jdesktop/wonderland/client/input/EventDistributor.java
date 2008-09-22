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
import org.jdesktop.wonderland.client.input.InputManager.EventMode;
import org.jdesktop.mtgame.PickInfo;
import java.util.Iterator;
import org.jdesktop.wonderland.common.InternalAPI;
import java.util.logging.Logger;

/**
 * The abstract base class for an Event Distributor singleton. The Entity Distributor is part of the input 
 * subsystem which distributes events throughout the scene graph according to the information provided by 
 * the entity event listeners. It also supports a set of global event listeners. This are independent of 
 * any events. Events are always tried to be delivered to the global event listeners. In general, the user
 * cannot make any assumptions about the order in which individual event listeners methods are called.
 *
 * @author deronj
 */

@InternalAPI
public abstract class EventDistributor implements Runnable {

    private static final Logger logger = Logger.getLogger(EventDistributor.class.getName());

    /** The current event mode */
    private static InputManager.EventMode eventMode = EventMode.WORLD;
    
    /** The base input queue entry. */
    private static class Entry {
	/** The Wonderland event. */
	Event event;
	/** The pick info for the event */
	PickInfo pickInfo;
        Entry (Event event, PickInfo pickInfo) {
	    this.event = event;
	    this.pickInfo = pickInfo;
	}
    }

    /** The state of the propagation state during the traversal. */
    public class PropagationState {
	public boolean toParent;
	public boolean toUnder;
    }

    private Thread thread;

    private LinkedBlockingQueue<Entry> inputQueue = new LinkedBlockingQueue<Entry>();

    private EventListenerCollection globalEventListeners = new EventListenerCollection();

    protected void start () {
	thread = new Thread(this, "EventDistributor");
	thread.start();
    }

    /**
     * Add a Wonderland event to the event distribution queue, with null pick info.
     * @param event The event to enqueue.
     */
    void enqueueEvent (Event event) {
	inputQueue.add(new Entry(event, null));
    }

    /**
     * Add a Wonderland event to the event distribution queue.
     * @param event The event to enqueue.
     * @param pickInfo The pick info for the event.
     */
    void enqueueEvent (Event event, PickInfo pickInfo) {
	inputQueue.add(new Entry(event, pickInfo));
    }

    /**
     * The run loop of the thread.
     */
    public void run () {
	while (true) {
	    Entry entry = null;
            try {
                entry = inputQueue.take();
            } catch (InterruptedException ex) {}
	    if (entry.event instanceof EventModeEvent) {
		processEventModeEvent(entry.event);
	    } else {
		processEvent(entry.event, entry.pickInfo);
	    }
	}
    }
    
    /** 
     * The responsibility for determining how to process individual event types is delegated to the subclass.
     * @param event The event to try to deliver to event listeners.
     * @param pickInfo The pick info associated with the event.
     */
    protected abstract void processEvent (Event event, PickInfo pickInfo);

    /** 
     * Traverse the list of global listeners to see whether the event should be delivered to any of them. 
     * Post the event to willing consumers. Note: the return values of propagateToParent() and propagateToUnder() 
     * for global listeners are ignored.
     */
    protected void tryGlobalListeners (Event event) {
	logger.fine("tryGlobalListeners event = " + event);
	synchronized (globalEventListeners) {
            Iterator<EventListener> it = globalEventListeners.iterator();
	    while (it.hasNext()) {
                EventListener listener = it.next();
		if (listener.isEnabled()) {
		    logger.fine("Calling consume for listener " + listener);
		    if (listener.consumeEvent(event, null)) {
			logger.fine("CONSUMED.");
			listener.postEvent(event);
		    }
		}
	    }
	}
    }
	
    /** 
     * See if any of the enabled event listeners for the given entity are willing to consume the given event.
     * Post the event to willing consumers. Also, returns the OR of propagateToParent for all enabled listeners 
     * and the OR of propagateToUnder for all enabled listeners in propState.
     */
    protected void tryListenersForEntity (Entity entity, Event event, PropagationState propState) {
	logger.fine("tryListenersForEntity, entity = " + entity + ", event = " + event);
	EventListenerCollection listeners = (EventListenerCollection) entity.getComponent(EventListenerCollection.class);
	if (listeners == null) { 
	    logger.fine("Entity has no listeners");
	    // propagateToParent is true, so OR makes no change to its accumulator
	    // propagateToUnder is false, so OR makes its accumulator is false
	    propState.toUnder = false;
	} else {
	    Iterator<EventListener> it = listeners.iterator();
	    while (it.hasNext()) {
		EventListener listener = it.next();
		if (listener.isEnabled()) {
		    logger.fine("Calling consume for listener " + listener);
		    if (listener.consumeEvent(event, entity)) {
			logger.fine("CONSUMED.");
			listener.postEvent(event);
		    }
		    propState.toParent |= listener.propagateToParent(event, entity);
		    propState.toUnder |= listener.propagateToUnder(event, entity);
		    logger.finer("Listener prop state, toParent = " + propState.toParent + 
				", toUnder = " + propState.toUnder);
		}
	    }
	}
	logger.fine("Cumulative prop state, toParent = " + propState.toParent + ", toUnder = " + propState.toUnder);
    }

    /** 
     * Traverse the entity parent chain (starting with the given entity) to see if the
     * event should be delivered to any of their listeners. Continue the search until no more parents are found.
     * Post the event to willing consumers. Also, returns the OR of propagateToParent for all enabled listeners 
     * and the OR of propagateToUnder for all enabled listeners in propState.
     */
    protected void tryListenersForEntityParents (Entity entity, Event event, PropagationState propState) {
	while (entity != null) {
	    tryListenersForEntity(entity, event, propState);
	    if (!propState.toParent) {
		// No more propagation to parents. We're done with this loop.
		break;
	    }
	    logger.fine("Propagate to next parent");
	    entity = /* TODO: notyet: entity.getParent()*/ null;
	}
    }
	
    /**
     * Returns the current event mode.
     */
    InputManager.EventMode getEventMode () {
	synchronized (eventMode) {
	    return eventMode;
	}
    }

    /**
     * Returns the current event mode.
     */
    void setEventMode (InputManager.EventMode eventMode) {
	enqueueEvent(new EventModeEvent(eventMode));
    }

    private void processEventModeEvent (Event event) {
	EventModeEvent eme = (EventModeEvent) event;
	EventMode newMode = eme.getEventMode();
	synchronized (eventMode) {
	    eventMode = newMode;
	}	
	logger.info("New event mode = " + eventMode);
    }

    /**
     * Add an event listener to be tried once per event. This global listener can be added only once.
     * Subsequent attempts to add it will be ignored.
     * <br><br>
     * Note: It is not a good idea to call this from inside EventListener.computeEvent function.
     * However, it is okay to call this from inside EventListener.commitEvent function if necessary.
     *
     * @param listener The global event listener to be added.
     */
    public void addGlobalEventListener (EventListener listener) {
	synchronized (globalEventListeners) {
	    if (globalEventListeners.contains(listener)) {
		return;
	    } else {
		globalEventListeners.add(listener);
	    }
	}
    }

    /**
     * Remove this global event listener.
     * <br><br>
     * Note: It is not a good idea to call this from inside EventListener.computeEvent function.
     * However, it is okay to call this from inside EventListener.commitEvent function if necessary.
     *
     * @param listener The global event listener to be removed.
     */
    public void removeGlobalEventListener (EventListener listener) {
	synchronized (globalEventListeners) {
	    globalEventListeners.remove(listener);
	}
    }
}
