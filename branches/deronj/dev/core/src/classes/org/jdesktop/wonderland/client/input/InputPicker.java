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
 * The abstract base class for an InputPicker singleton. The InputPicker is the part of the 
 * input subsystem which determines the pick data the entity which an input event "hits." There are two sets of
 * routines. One set is used when Embedded Swing is being used in the Wonderland client (pickMouseEventSwing 
 * and pickKeyEventSwing). The other set is used when Embedded Swing is not being used in the Wonderland client
 * (pickMouseEventNonSwing and pickKeyEventNonSwing).
 *
 * Note: the pick operation happens on the AWT event dispatch thread all cases.
 *
 * @author deronj
 */

// TODO: if possible separate out the JME-dependent code into jme.input.InputPicker3D

@ExperimentalAPI
abstract class InputPicker {

    private static final Logger logger = Logger.getLogger(InputPicker.class.getName());

    /**
     * The return type of the pick methods in this class.
     */
    public class PickEventReturn {

	/** The entity. */
	public Entity entity;

	/** One level of pick information for what is immediately under the event in eye space **/
	public PickData pickData;

	/** Constructs a new instance of PickEventReturn */
	public PickEventReturn (Entity entity, PickData pickData) {
	    this.entity = entity;
	    this.pickData = pickData;
	}
    }    

    /** The input picker singleton */
    private static InputPicker inputPicker;

    /** The type of grab transition */
    private enum GrabChangeType { GRAB_ACTIVATE, GRAB_DEACTIVATE, GRAB_NO_CHANGE };

    /** Whether a grab is currently active */
    private boolean grabIsActive = false;

    /** The JME collision system (used for picking) */
    // TODO: how do we also handle the other types of collision systems? 
    JMECollisionSystem collisionSys;

    /** The destination pick results that the picker computes */
    private PickResults destPickResults;

    /** The destination pick results for the previous picked event */
    private PickResults destPickResultsPrev;

    /** The pick ray. This is a vector along the +Z axis in eye space. */
    private static Ray pickRay = new Ray(new Vector3f(0f, 0f, 0f), new Vector3f(0f, 0f, 1f));

    // 3 pixels
    private static final int BUTTON_CLICK_POSITION_THRESHOLD = 3;

    // Coordinate of last button press event
    private int buttonLastX, buttonLastY;

    /**
     * Returns the input picker singleton.
     */
    abstract static InputPicker getInputPicker ();

    /**
     * Create a new instance of InputPicker.
     */
    private InputPicker () {
       CollisionManager cm = worldManager.getCollisionManager();
       collisionSys = cm.loadSystem(JMECollisionSystem.class);
    }

    /**
     * Picker for mouse events for the Embedded Swing case.
     * To be called by Embedded Swing toolkit createCoordinateHandler.
     *
     * Returns non-null if window is a WindowSwing. If it is a WindowSwing then
     * return the appropriate hit entity and the corresponding pick results.
     *
     * @param awtEvent The event whose entity and pickresults need to be picked.
     * @return An object of class PickEventReturn, which contains the return
     * values entity and pickData.
     */
    public PickEventReturn pickMouseEventSwing (MouseEvent awtEvent) {

	// Determine the destination pick results by performing a pick, considering grabs. etc.
        destPickResults = determineDestPickResults(awEvent);
	if (destPickResults == null || destPickResults.getNumber() <= 0) {
	    // Pick miss. Ignore the event.
	    return null;
	}

	int eventID = awtEvent.getID();
	if (eventID == MouseEvent.MOUSE_MOVED ||
	    eventID == MouseEvent.MOUSE_DRAGGED) {
	    generateEnterExitEvents(awtEvent, destPickResults);
	}

	boolean propagateToUnder = true;
	PickData pickData = destPickResults.getPickData(0);
	Event event = createWonderlandEvent(awtEvent);
	int idx = 0;
	while (pickData != null && idx < destPickResults.getNumber() && propagateToUnder) {

            // Search through the entity space to find the first input sensitive 
	    // Window Swing entity for the pickinfo that will consume the event.
	    // Note: WindowSwing entities are always leaf nodes so we don't need to search the parent chains.
	    Entity entity = pickDataToEntity(pickData);

	    boolean consumeEvent;
	    EventListenerCollection listeners = entity.getComponent(EventListenerCollection.class);
	    if (listeners == null) { 
		consumeEvent = false;
		propagateToUnder = false;
	    } else {
		event.setPickData(pickData);
		for (EventListener listener : listeners) {
		    consumeEvent |= listener.consumeEvent(event, entity);
		    propagateToUnder |= listener.propagateToUnder(event, entity);
		}
	    }

	    if (consumeEvent && isWindowSwingEntity(entity)) {
		// WindowSwing pick semantics: Stop at the first WindowSwing which will consume the event. 
		// Note that because of single-threaded nature of the Embedded Swing interface we cannot do 
		// any further propagation of the event to parents or unders.
		return new PickEventReturn(entity, pickData);
	    }

	    if (propagateToUnder) {
		// Search next depth level for entities willing to consume this event
		idx++;
		if (idx < destPickResults.getNumber()) {
		    pickData = destPickResults.getPickData(idx);
		} else {
		    pickData = null;
		}
		continue;
	    }
	}

	// We haven't found an input sensitive WindowSwing, so turn this job over to the
	// EventDistributor in order to continue the search for consuming entities.
	// This gets the work off the AWT Event Dispatch thread as soon as possible.

	EventDistributor.enqueueEvent(event, destPickResults);

	return null;
    }

    /**
     * Picker for key events for the Embedded Swing case.
     */
    public PickEventReturn pickKeyEventSwing (KeyEvent event) {
	// TODO
    }

    /**
     * Mouse Event picker for non-Embedded Swing case.
     * Finds the first consuming entity and then turns the work over to the event deliverer.
     * This method does not return a result but instead enqueues an entry for the event in
     * the input queue of the event deliverer.
     */
    public void pickMouseEventNonSwing (MouseEvent awtEvent) {
	
	// Determine the destination pick results by performing a pick, considering grabs. etc.
        destPickResults = determineDestPickResults(awtEvent);
	if (destPickResults == null || destPickResults.getNumber() <= 0) {
	    // Pick miss. Ignore the event.
	    return;
	}

	int eventID = awtEvent.getID();
	if (eventID == MouseEvent.MOUSE_MOVED ||
	    eventID == MouseEvent.MOUSE_DRAGGED) {
	    generateEnterExitEvents(awtEvent, destPickResults);
	}

	PickData pickData = destPickResults.getPickData(0);
	Event event = createWonderlandEvent(awtEvent);
	event.setPickData(pickData);

	// Do the rest of the work in the EventDistributor
	EventDistributor.enqueueEvent(event, destPickResults);
    }

    /**
     * Key event picker for non-Embedded Swing case.
     */
    public void pickKeyEventNonSwing (KeyEvent event) {
	// TODO
    }

    /**
     * Performs a pick on the scene graph and determine the actual destination pick results 
     * taking into account button click threshold and mouse button grabbing.
     * Returns the destination pick results in the global member destPickResults.
     *
     * @param e The mouse event.
     * @return The destination pick results.
     */
    protected PickResults determineDestPickResults (MouseEvent e) {
        boolean deactivateGrab = false;

	// Implement the click threshold. Allow click event to be passed along only
	if (e.getID() == MouseEvent.MOUSE_PRESSED) {
	    buttonLastX = e.getX();
	    buttonLastY = e.getY();
	} else if (e.getID() == MouseEvent.MOUSE_CLICKED) {
	    if (!buttonWithinClickThreshold(e.getX(), e.getY())) {
		// Discard the event by returing a miss
		return null;
	    }
	}

	// Handle button clicked events specially. The mouse clicked event
	// comes after the grab has terminated. So we do this in order to 
	// force the clicked event to go to the same destination as the
	// pressed event
	if (e.getID() == MouseEvent.MOUSE_CLICKED) {
	    return lastButtonReleasedPickResults;
	}

	// First perform the pick (the pick datas in the results are ordered
	// from least to greatest eye distance.
	PickResults hitPickResults = new PickResults();
	collisionSys.pickAll(pickRay, hitPickResults);

	// If no grab is active and we didn't hit anything return a miss */
	if (!grabIsActive && pickResults.getNumber() <= 0) {
	    if (e.getID() == MouseEvent.MOUSE_RELEASED) {
		lastButtonReleasedPickResults = null;
	    }
	    return null;
	}

	// Calculate how the grab state should change. If the a grab should activate, activate it.
	int eventID = e.getID();
	if (eventID == MouseEvent.MOUSE_PRESSED ||
	    eventID == MouseEvent.MOUSE_RELEASED) {

	    grabChange = evaluateButtonGrabStateChange(eventID, e);
	    if (grabChange == GRAB_ACTIVATE) {
		grabIsActive = true;
		grabPickResults = hitPickResults;
	    }
	}

	// If a grab is active, the event destination pick results will be the grabbed pick results
	PickResults pickResults;
	if (grabisActive) {
	    pickResults = grabPickResults;
	} else {
	    pickResults = hitPickResults;
	}

	// It is now safe to disable the grab
	if (grabChange == GRAB_DEACTIVATE) {
	    grabIsActive = false;
	    grabPickResults = null;
	}

	if (e.getID() == MouseEvent.MOUSE_RELEASED) {
	    lastButtonReleasedPickResults = pickResults;
	}

	return pickResults;
    }

    private GrabChangeType evaluateButtonGrabStateChange (int eventID, MouseEvent e) {
	int modifiers = convertAwtEventModifiersToPassiveGrabModifiers(e.getModifiers());

	if (eventID == MouseEvent.MOUSE_PRESSED) {
	    // Button press
	    return GRAB_ACTIVATE;
	} else if (eventID == MouseEvent.MOUSE_RELEASED &&
	           (modifiers & PG_ALL_BUTTONS_MASK) == 0) {
	    // Button release: Similar to X11: terminate grab only when all buttons are released	
	    return GRAB_DEACTIVATE;
	}

	return GRAB_NO_CHANGE;
    }

    // Returns true if the button release is close enough to the button press
    // so as to consitute a click event.
    // Note: These are Java-on-Windows behavior. There is no click event position
    // threshold on Java-on-Linux. But Hideya wants the Windows behavior.

    private final boolean buttonWithinClickThreshold (int x, int y) {
	return Math.abs(x - buttonLastX) <= BUTTON_CLICK_POSITION_THRESHOLD &&
	       Math.abs(y - buttonLastY) <= BUTTON_CLICK_POSITION_THRESHOLD;
    }

    /* TODO: not needed?
    private PickResults clonePickResults (PickResults pickResults) {
	PickResults clone = new PickResults();
	clone.setCheckDistance(true);
	for (int i = pickResults.getNumber() - 1; i >= 0; i-- ) {
	    PickData pickData = pickResults.getPickData(i);
	    clone.addPickData(pickData);
	}
	return clone;
    }
    */

    private int convertAwtEventModifiersToPassiveGrabModifiers (int modifiers) {
	int pgModifiers = 0;

        if ((modifiers & InputEvent.SHIFT_DOWN_MASK) != 0) {
	    pgModifiers |= PG_SHIFT_DOWN_MASK;
	}

        if ((modifiers & InputEvent.CTRL_DOWN_MASK) != 0) {
	    pgModifiers |= PG_CTRL_DOWN_MASK;
	}

	if ((modifiers & InputEvent.META_DOWN_MASK) != 0) {
	    pgModifiers |= PG_META_DOWN_MASK;
	}

	if ((modifiers & InputEvent.ALT_DOWN_MASK) != 0) {
	    pgModifiers |= PG_ALT_DOWN_MASK;
	}

	if ((modifiers & InputEvent.ALT_GRAPH_DOWN_MASK) != 0) {
	    pgModifiers |= PG_ALT_GRAPH_DOWN_MASK;
	}

	return pgModifiers;
    }

    /**
     * Generate object enter/exit events based on the determined destination pick results.
     *
     * @param event The mouse event.
     * @param destPickResults The destination pick results.
     */
    private void generateEnterExitEvents (MouseEvent event, PickResults destPickResults) {
	/*

	if event is mouse exit, send exit events to all prev pickresults
	if event is mouse enter do ??

	// TODO: Compare new pickresults with previous pickresults
	// Determine list of added and deleted pick results
	// Determine the entities for the added and deleted pick results
	// Generate enter events for added pickresults and exit events for deleted pickresults
	// Enqueue enter/exit events to the EventDeliverer to be propogated through the entity and its parents, checking consumeEvent and propogateToParent
	*/

	destPickResultsPrev = destPickResults;
    }

    private Entity pickDataToEntity (PickData pickData) {
	// TODO
    }

    private static boolean isWindowSwingEntity (Entity entity) {
	/* TODO
	   AppWindowComponent appWindowComp = entity.getComponent(AppWindowComponent.class);
	   AppWindow window = appWindowComp.getWindow();
	   return window instanceof WindowSwing;
	 */
	return false;
    }

    /**
     * Converts a 2D AWT event into a Wonderland event.
     */
    protected abstract Event createWonderlandEvent (AWTEvent awtEvent);
}

