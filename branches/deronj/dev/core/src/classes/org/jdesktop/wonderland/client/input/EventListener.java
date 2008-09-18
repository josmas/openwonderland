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
 * A listener for events which are delivered by the input system to entities. An event listener can be added to an 
 * entity via addToEntity(). Multiple listeners can be added to a single entity. A listener can be only added
 * once to an entity. Subsequent attempts to add the listener to the same entity will be ignored.
 *
 * When an entity receives an event it will invoke methods for all enabled event listeners. An event listener 
 * can be enabled or disabled via setEnable(). For each enabled listener the following methods will be called 
 * in the following order:
 *
 * 1. <code>consumeEvent</code> is called. The listener should return true if it wishes to receive the given event.
 *
 * 2. <code>propogateToParent</code> is called. The listener should return true if the parent entity should be
 *    given a chance to receive the given event.
 *
 * 3. <code>propogateToUnder</code> is called. The listener should return true if the entity immediately 
 *    underneath the current entity (in eye space) should be given a chance to receive the event.
 *
 * 4. <code>computeEvent</code> is called if the previous call to consumeEvent returned true. This
 *    method should determine how to change the world based on the event.
 *
 * 5. <code>commitEvent<code> is called if computeEvent was previously called. This method should
 *    apply to the world thoese changes which were determined by computeEvent.
 *
 * Note: <code>commitEvent</code> is called in the render loop thread and therefore should be
 * kept short.
 *
 * In general, the programmer should not make any assumptions about the order in which methods of different 
 * event listeners methods are called for a single event. Nor should the programmer make any assumptions about
 * the order or number of times methods are called for a single event listener for a single event. One 
 * guarantee which is provided is that for a given event an enabled listener's computeEvent will be called once and, 
 * sometime later, its commitEvent routine will be called. Also, another guarantee is that all registered,
 * enabled event listeners (both global and per-entity) will be tried for a given event before any event listeners
 * are tried for subsequent events.
 *
 * If an entity which has no attached listeners receives an event that entity will be treated as
 * if it has an enabled listener whose consumeEvent method returns true, whose propagateToParent 
 * method returns true and whose propagateToUnder method returns false.
 *
 * <code>computeEvent</code> should propogate information to <code>commitEvent</code> by storing
 * data in instance data members of the event listener. The system makes the guaranteed that 
 * a call to <code>commitEvent</code> is always preceded by a call to <code>computeEvent</code>
 * for each newly received event.
 *
 * @author deronj
 */

@ExperimentalAPI
public interface EventListener  {

    /**
     * Returns whether this event listener is currently enabled. 
     */
    public boolean isEnabled ();

    /**
     * Enable or disable this event listener.
     *
     * Note: Disabling the listener when it is enabled deletes any pending posted events which 
     * have not yet been delivered to the listeners.
     *
     * @param enable Whether the event listener should be enabled.
     */
    public void setEnable (boolean enable);

    /**
     * Returns whether this event listener currently wishes to receive the given event.
     * Here is where the decision to receive different types of events occurs.
     * The input system calls this method only if the listener is enabled.
     * Computations in this method should be kept reasonably short as they occur in 
     * AWT Event Dispatch thread.
     *
     * Example Usage: User interface buttons with rounded edges can use a simple quad for its geometry
     * and use a transparent texture to achieve the rounded appearance. In order to make sure that
     * the portions of the quad which are transparent are not input sensitive we could define 
     * consumeEvent to lookup the hit texel in the texture and not consume the event if the hit
     * texel is transparent.
     *
     * @param event The event in question.
     * @param entity The entity which is a candidate to receive the event. (This is null if the 
     * event listener is global).
     */
    public boolean consumeEvent (Event event, Entity entity);

    /**
     * Returns whether the event should also be propogated to its parent for possible delivery.
     * Computations in this method should be kept reasonably short as they occur in
     * AWT Event Dispatch thread. This method is only called for entity-attached event listeners.
     *
     * @param event The event in question.
     * @param entity The entity which is a candidate to receive the event. 
     */
    public boolean propagateToParent (Event event, Entity entity);

    /**
     * Returns whether the event should also be propogated to the next underlying object (in eye space) for 
     * possible delivery. Computations in this method should be kept reasonably short as they occur in
     * AWT Event Dispatch thread. This method is only called for entity-attached event listeners.
     *
     * @param event The event in question.
     * @param entity The entity which is a candidate to receive the event. 
     */
    public boolean propagateToUnder (Event event, Entity entity);

    /**
     * The implementation of this method should determine how to change the world based on the given event.
     *
     * Note: It is guaranteed that when <code>computeEvent</code> is called for a particular event 
     * that <code>commitEvent</code> will be called sometime after.
     *
     * @param event The event in question.
     * @param entity The entity which is receiving the event.
     */
    public void computeEvent (Event event, Entity entity);

    /**
     * Called after <code>computeEvent</code> has been called for this event listener.
     * The implementation of this method apply to the world those changes which were determined
     * in <code>computeEvent</code>.
     *
     * Note: It is guaranteed that a call to <code>commitEvent</code> for an event is always 
     * preceded by a call to <code>commitEvent</code> for that event.
     *
     * @param event The event which was computed.
     * @param entity The entity which is receiving the event.
     */
    public void commitEvent (Event event, Entity entity);

    /**
     * Add this event listener to the given entity. An given event listener instance may be only
     * be added once to an entity. Once an event listener instance has been added to an entity 
     * subsequent attempts to add the entity will be ignored. However, a given event listener
     * instance may be added to multiple entities.
     *
     * Note: It is not a good idea to call this from inside EventListener.computeEvent function.
     * However, it is okay to call this from inside EventListener.commitEvent function if necessary.
     *
     * @param The entity to which to attach this event listener.
     */
    public void addToEntity (Entity entity);

    /**
     * Remove this event listener from the given entity.
     *
     * Note: It is not a good idea to call this from inside EventListener.computeEvent function.
     * However, it is okay to call this from inside EventListener.commitEvent function if necessary.
     *
     * @param The entity to which to attach this event listener.
     */
    public void removeFromEntity (Entity entity) {

	EventListenerCollection collection = entity.getComponent(EventListenerCollection.class);
	if (collection == null) {
	    return;
	}

	collection.remove(this);
	
	if (collection.size() <= 0) {
	    entity.removeComponent(EventListenerCollection.class);
	}
    }
}
