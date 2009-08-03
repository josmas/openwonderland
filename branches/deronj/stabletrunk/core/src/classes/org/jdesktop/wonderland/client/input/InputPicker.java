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

import java.awt.AWTEvent;
import java.awt.Canvas;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.logging.Logger;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.mtgame.CollisionManager;
import org.jdesktop.mtgame.JMECollisionSystem;
import org.jdesktop.mtgame.PickInfo;
import org.jdesktop.mtgame.PickDetails;
import com.jme.math.Ray;
import com.jme.math.Vector2f;
import com.jme.math.Vector3f;
import com.jme.renderer.Camera;
import java.awt.Button;
import java.util.Iterator;
import java.util.LinkedList;
import org.jdesktop.mtgame.CameraComponent;
import org.jdesktop.mtgame.CollisionComponent;
import org.jdesktop.mtgame.EntityComponent;
import org.jdesktop.wonderland.client.jme.ClientContextJME;
import org.jdesktop.wonderland.client.jme.input.KeyEvent3D;
import org.jdesktop.wonderland.client.jme.input.MouseEvent3D;
import org.jdesktop.wonderland.common.InternalAPI;
import java.util.concurrent.LinkedBlockingQueue;
import org.jdesktop.wonderland.client.jme.input.MouseEnterExitEvent3D;

/**
 * The abstract base class for an InputPicker singleton. The InputPicker is the part of the 
 * input subsystem which determines the pick details and the entity which an input event "hits." There are two 
 * mouse event processing methods. One method is used from WindowSwing code when a WindowSwing exists.
 * This method performs a pick, determines whether it hits a WindowSwing and if so returns the pickInfo to the
 * WindowSwing code. If a WindowSwing is not hit the pickInfo is stored for later use.
 *
 * The other mouse event processing method is used for events which don't hit a WindowSwing. These events
 * come from the Canvas (via the InputManager). The processing for these events, called "3D events," attempts
 * to avoid performing a pick by using pickInfo provided by WindowSwing, if available. (See previous paragraph).
 *
 * Note: the pick operation happens on the AWT event dispatch thread in all cases.
 *
 * @author deronj
 */

// TODO: if possible separate out the JME-dependent code into jme.input.InputPicker3D

@InternalAPI
public abstract class InputPicker {

    protected static final Logger logger = Logger.getLogger(InputPicker.class.getName());

    // Bit masks used during the grab calculation
    private static final int PG_SHIFT_DOWN_MASK     = 1<<0;
    private static final int PG_CTRL_DOWN_MASK      = 1<<1;
    private static final int PG_META_DOWN_MASK      = 1<<2;
    private static final int PG_ALT_DOWN_MASK       = 1<<3;
    private static final int PG_ALT_GRAPH_DOWN_MASK = 1<<4;
    private static final int PG_ALL_BUTTONS_MASK =
        PG_SHIFT_DOWN_MASK |
        PG_CTRL_DOWN_MASK  |
        PG_META_DOWN_MASK  |
        PG_ALT_DOWN_MASK   |
        PG_ALT_GRAPH_DOWN_MASK;

    /** The type of grab transition */
    private enum GrabChangeType { GRAB_ACTIVATE, GRAB_DEACTIVATE, GRAB_NO_CHANGE };

    /** Whether a grab is currently active */
    private boolean grabIsActive = false;

    /** The JME collision system (used for picking) */
    // TODO: how do we also handle the other types of collision systems? 
    JMECollisionSystem collisionSys;

    /** The destination pick info that the picker computes */
    private PickInfo destPickInfo;

    /** The destination pick info for the previous picked event */
    private PickInfo destPickInfoPrev;

    // 3 pixels
    private static final int BUTTON_CLICK_POSITION_THRESHOLD = 3;

    // Coordinate of last button press event
    private int buttonLastX, buttonLastY;

    // The pick info of the last time a button was released.
    private PickInfo lastButtonReleasedPickInfo;
    
    // The pick info of the mouse button press event which started a grab
    private PickInfo grabPickInfo;
    
    /** The camera component to use for picking. */
    private CameraComponent cameraComp;

    /** The canvas to use for picking. */
    private Canvas canvas;

    /** The event distributor associated with this picker */
    protected EventDistributor eventDistributor;

    /** A temporary used during picking */
    private Vector2f eventPointScreen = new Vector2f();

    /** Another temporary used during picking */
    private Vector3f eventPointWorld = new Vector3f();

    /** Another temporary used during picking */
    private Vector3f directionWorld = new Vector3f();

    /** Another temporary used during picking */
    private Ray pickRay = new Ray();

    // TODO: can eventually get rid of this and just put the pickinfo into the Swing pickInfo queue
    private static class PickInfoQueueEntry {
	private PickInfo pickInfo;
	private MouseEvent mouseEvent;
	private PickInfoQueueEntry (PickInfo pickInfo, MouseEvent mouseEvent) {
	    this.pickInfo = pickInfo;
	    // TODO: For verification only. Evetually remove.
	    this.mouseEvent = mouseEvent;
	}
    }

    /** 
     * When a WindowSwing exists events go to WindowSwingEmbeddedPeer.createCoordinateHandler before
     * for picking before coming to the InputPicker. createCoordinateHandler stores the pick infos
     * for non-WindowSwing events in this queue so we don't need to repick in the picker.
     */
    private LinkedBlockingQueue<PickInfoQueueEntry> swingPickInfos = 
	new LinkedBlockingQueue<PickInfoQueueEntry>();


    /**
     * As we example the entities along the pick ray we need to keep
     * track of the entities which are visible (that is not obscured
     * by an entity whose listeners doesn't propagate to under). We also
     * need to keep track the pick details which will be ultimately sent
     * in the enter/exit events.
     */
    private static class EntityAndPickDetails {
	private Entity entity;
	private PickDetails pickDetails;
	private EntityAndPickDetails (Entity entity, PickDetails pickDetails) {
	    this.entity = entity;
	    this.pickDetails = pickDetails;
	}
    }

    /** The entities that the pointer is inside for the current event. */
    LinkedList<EntityAndPickDetails> insideEntities = new LinkedList<EntityAndPickDetails>();

    /** The entities that the pointer was inside for the last event. */
    LinkedList<EntityAndPickDetails> insideEntitiesPrev = new LinkedList<EntityAndPickDetails>();

    /** The entities that no longer have the pointer inside. */
    LinkedList<EntityAndPickDetails> noLongerInsideEntities = new LinkedList<EntityAndPickDetails>();

    /** The entities that newly have the pointer inside. */
    LinkedList<EntityAndPickDetails> newlyInsideEntities = new LinkedList<EntityAndPickDetails>();

    /** A dummy AWT component used in enter/exit events. */
    private static Button dummyButton = new Button();

    /**
     * Create a new instance of InputPicker.
     */
    protected InputPicker () {
	CollisionManager cm = ClientContextJME.getWorldManager().getCollisionManager();
	collisionSys = (JMECollisionSystem) cm.loadCollisionSystem(JMECollisionSystem.class);
    }

    /**
     * Specify the associated event distributor.
     * @param eventDistributor
     */
    public void setEventDistributor (EventDistributor eventDistributor) {
	this.eventDistributor = eventDistributor;
    }

    /**
     * Picker for mouse events for the Embedded Swing case.
     * To be called by Embedded Swing toolkit createCoordinateHandler.
     *
     * Returns non-null if window is a WindowSwing. If it is a WindowSwing then
     * return the appropriate hit entity and the corresponding pick info.
     *
     * @param awtEvent The event whose entity and pickInfo need to be picked.
     * @return An object of class PickEventReturn, which contains the return
     * values entity and pickDetails.
     */
    public InputManager.PickEventReturn pickMouseEventSwing (MouseEvent awtMouseEvent) {
	logger.fine("Picker Swing: received awt event = " + awtMouseEvent);

	// Determine the destination pick info by performing a pick, considering grabs. etc.
        destPickInfo = determineDestPickInfo(awtMouseEvent);

	// Generate enter/exit events associated with this mouse event
	int eventID = awtMouseEvent.getID();
	if (eventID == MouseEvent.MOUSE_MOVED ||
	    eventID == MouseEvent.MOUSE_DRAGGED ||
	    eventID == MouseEvent.MOUSE_ENTERED ||
	    eventID == MouseEvent.MOUSE_EXITED) {
	    generateEnterExitEvents(awtMouseEvent, destPickInfo);
	}

	// Check for pick miss
	if (destPickInfo == null || destPickInfo.size() <= 0) {
	    // Pick miss. Send it to the event distributor without pick info.
	    logger.finest("Picker: pick miss");
	    logger.finest("Enqueue null pick info for 3D event");
	    logger.finest("awtMouseEvent = " + awtMouseEvent);
	    swingPickInfos.add(new PickInfoQueueEntry(null, awtMouseEvent));
	    return null;
	}
	logger.fine("Picker: pick hit: destPickInfo = " + destPickInfo);

	// Search through the entity space to find the first input sensitive 
	// Window Swing entity for the pickInfo that will consume the event.
	// Note: WindowSwing entities are always leaf nodes so we don't need to search the parent chains.
	boolean propagatesToUnder = true;
	PickDetails pickDetails = destPickInfo.get(0);
	logger.fine("Picker: pickDetails = " + pickDetails);
	MouseEvent3D event = (MouseEvent3D) createWonderlandEvent(awtMouseEvent);
	int idx = 0;
	while (pickDetails != null && idx < destPickInfo.size() && propagatesToUnder) {
	    Entity entity = pickDetailsToEntity(pickDetails);
	    logger.fine("Picker: entity = " + entity);
	    boolean consumesEvent = false;
            propagatesToUnder = false;
	    EventListenerCollection listeners = (EventListenerCollection) 
		entity.getComponent(EventListenerCollection.class);
	    if (listeners == null) { 
		consumesEvent = false;
		propagatesToUnder = false;
	    } else {
		event.setPickDetails(pickDetails);
                Iterator<EventListener> it = listeners.iterator();
		while (it.hasNext()) {
                    EventListener listener = it.next();
		    if (listener.isEnabled()) {
			Event distribEvent = EventDistributor.createEventForEntity(event, entity);
			logger.finest("Invoke event listener consumesEvent");
			logger.finest("Listener = " + listener);
			logger.finest("Event = " + distribEvent);
			consumesEvent |= listener.consumesEvent(distribEvent);
			propagatesToUnder |= listener.propagatesToUnder(distribEvent);
			logger.finest("consumesEvent = " + consumesEvent);
		    }
		}
	    }

	    logger.finest("isWindowSwingEntity(entity) = " + isWindowSwingEntity(entity));
	    if (consumesEvent && isWindowSwingEntity(entity)) {
		// WindowSwing pick semantics: Stop at the first WindowSwing which has any listener which
		// will consume the event. Note that because of single-threaded nature of the Embedded Swing 
		// interface we cannot do any further propagation of the event to parents or unders.
		logger.fine("Hit windowswing");
		return new InputManager.PickEventReturn(entity, pickDetails);
	    }

	    if (propagatesToUnder) {
		// Search next depth level for entities willing to consume this event
		idx++;
		if (idx < destPickInfo.size()) {
		    pickDetails = destPickInfo.get(idx);
		} else {
		    pickDetails = null;
		}
	    }
	}

	// We haven't found an input sensitive WindowSwing so provide the pickInfo we have calculated
	// to pickMouseEvent3D.

	logger.finest("Enqueue pick info for 3D event");
	logger.finest("awtMouseEvent = " + awtMouseEvent);
	logger.finest("destPickInfo = " + destPickInfo);
	swingPickInfos.add(new PickInfoQueueEntry(destPickInfo, awtMouseEvent));

	return null;
    }

    /**
     * Returns true if the given entity belongs to a WindowSwing.
     */
    private static boolean isWindowSwingEntity (Entity entity) {
	// We know whether it is a WindowSwing entity by looking for the attached WindowSwingEntityComponent
	EntityComponent comp = entity.getComponent(InputManager.WindowSwingMarker.class);
	return comp != null;
    }

    /**
     * Mouse Event picker for non-Swing (3D) events.
     * Finds the first consuming entity and then turns the work over to the event deliverer.
     * This method does not return a result but instead enqueues an entry for the event in
     * the input queue of the event deliverer.
     */
    void pickMouseEvent3D (MouseEvent awtEvent) {
	logger.fine("Received awt event = " + awtEvent);
	MouseEvent3D event;

	// Determine the destination pick info by reading from the pickInfo Queue, performing a pick, 
	// considering grabs. etc.
        destPickInfo = determineDestPickInfo(awtEvent);

	// Generate enter/exit events associated with this mouse event
	int eventID = awtEvent.getID();
	if (eventID == MouseEvent.MOUSE_MOVED ||
	    eventID == MouseEvent.MOUSE_DRAGGED ||
	    eventID == MouseEvent.MOUSE_ENTERED ||
	    eventID == MouseEvent.MOUSE_EXITED) {
	    generateEnterExitEvents(awtEvent, destPickInfo);
	}

	// Check for pick miss
	if (destPickInfo == null || destPickInfo.size() <= 0) {
	    // Pick miss. Send it along without pick info.
	    logger.finest("Picker: pick miss");
	    event = (MouseEvent3D) createWonderlandEvent(awtEvent);
	    eventDistributor.enqueueEvent(event, null);
	    return;
	}
	logger.fine("Picker: pick hit: destPickInfo = " + destPickInfo);

	// Do the rest of the work in the EventDistributor
	event = (MouseEvent3D) createWonderlandEvent(awtEvent);
	eventDistributor.enqueueEvent(event, destPickInfo);
    }

    /**
     * Process key events. No picking is actually performed. Key events are delivered starting at the
     * entity that has the keyboard focus.
     */
    void pickKeyEvent (KeyEvent awtEvent) {
	logger.fine("Picker: received awt event = " + awtEvent);
	KeyEvent3D keyEvent = (KeyEvent3D) createWonderlandEvent(awtEvent);
	eventDistributor.enqueueEvent(keyEvent);
    }

    /**
     * Performs a pick on the scene graph and determine the actual destination pick info 
     * taking into account button click threshold and mouse button grabbing.
     * Returns the destination pick info in the global member destPickInfo.
     *
     * @param e The mouse event.
     * @return The destination pick info.
     */
    protected PickInfo determineDestPickInfo (MouseEvent e) {
        boolean deactivateGrab = false;

	// See if the WindowSwing has already determined a pickInfo for this event.
	// TODO: right now, button release events are never sent to createCoordinateHandler so they
	// never have pre-calculated pickInfos. I don't yet know if this is an Embedded Swing bug or
	// whether it is a feature.
	PickInfo swingPickInfo = null;
	if (swingPickInfos.peek() != null) {
	    try {
		PickInfoQueueEntry entry = swingPickInfos.take();

		// TODO: for now, verify that this is the right pickInfo for this event
		// Only check certain fields. Other fields (such as absolute X and Y) are 
		// expected to be different
		if (e.getID() == entry.mouseEvent.getID() &&
		    e.getX() == entry.mouseEvent.getX() &&
		    e.getY() == entry.mouseEvent.getY()) {
		    swingPickInfo = entry.pickInfo;
		} else {
		    logger.warning("Swing pickInfo event doesn't match 3D event. Repicking.");
		    logger.warning("3D event = " + e);
		    logger.warning("pickInfo event = " + entry.mouseEvent);
		}
	    } catch (InterruptedException ex) {}
	}

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
	    return lastButtonReleasedPickInfo;
	}

	// First perform the pick (the pick details in the info are ordered
	// from least to greatest eye distance.
        PickInfo hitPickInfo ;
	if (swingPickInfo == null) {
	    hitPickInfo = pickEventScreenPos(e.getX(), e.getY());
	} else {
	    hitPickInfo = swingPickInfo;
	}
	if (hitPickInfo == null || hitPickInfo.size() <= 0) {
	    return null;
	}

	/* For Debug
	int n = hitPickInfo.size();
	System.err.println("n = " + n);
	for (int i = 0; i < n; i++) {
	    PickDetails pd = hitPickInfo.get(i);
	    System.err.println("pd[" + i + "] = " + pd);
	    Entity pickEntity = pickDetailsToEntity(pd);
	    System.err.println("entity[" + i + "] = " + pickEntity);
	}
	*/

	// If no grab is active and we didn't hit anything return a miss */
	if (!grabIsActive && (hitPickInfo == null || hitPickInfo.size() <= 0)) {
	    if (e.getID() == MouseEvent.MOUSE_RELEASED) {
		lastButtonReleasedPickInfo = null;
	    }
	    return null;
	}

	// Calculate how the grab state should change. If the a grab should activate, activate it.
	GrabChangeType grabChange = GrabChangeType.GRAB_NO_CHANGE;
        int eventID = e.getID();
	if (eventID == MouseEvent.MOUSE_PRESSED ||
	    eventID == MouseEvent.MOUSE_RELEASED) {

	    grabChange = evaluateButtonGrabStateChange(eventID, e);
	    if (grabChange == GrabChangeType.GRAB_ACTIVATE) {
		grabIsActive = true;
		grabPickInfo = hitPickInfo;
	    }
	}

	// If a grab is active, the event destination pick info will be the grabbed pick info
	PickInfo pickInfo;
	if (grabIsActive) {
	    // TODO: HACK: is this the right way to fix this?
	    if (eventID == MouseEvent.MOUSE_DRAGGED) {
		pickInfo = hitPickInfo;
	    } else {
		pickInfo = grabPickInfo;
	    }
	} else {
	    pickInfo = hitPickInfo;
	}

	// It is now safe to disable the grab
	if (grabChange == GrabChangeType.GRAB_DEACTIVATE) {
	    grabIsActive = false;
	    grabPickInfo = null;
	}

	if (e.getID() == MouseEvent.MOUSE_RELEASED) {
	    lastButtonReleasedPickInfo = pickInfo;
	}

	return pickInfo;
    }

    /** 
     * Specify the canvas to be used for picking.
     *
     * @param canvas The AWT canvas to use for picking operations.
     */
    public void setCanvas (Canvas canvas) {
	this.canvas = canvas;
    }

    /** 
     * Returns the canvas that is used for picking.
     */
    public Canvas getCanvas () {
	return canvas;
    }

    /** 
     * Specify the camera component to be used for picking.
     *
     * @param cameraComp The mtgame camera component to use for picking operations.
     */
    void setCameraComponent (CameraComponent cameraComp) {
	this.cameraComp = cameraComp;
    }

    /** 
     * Returns the camera component that is used for picking.
     */
    CameraComponent getCameraComponent () {
	return cameraComp;
    }

    /** 
     * Calculates the ray to use for picking, based on the given screen coordinates.
     */
    private Ray calcPickRayWorld (int x, int y) {

	// Get the world space coordinates of the eye position
	Camera camera = cameraComp.getCamera();
	Vector3f eyePosWorld = camera.getLocation();

	// Convert the event from AWT coords to JME float screen space.
	// Need to invert y because (0f, 0f) is at the button left corner.
	eventPointScreen.setX((float)x);
	eventPointScreen.setY((float)(canvas.getHeight()-1-y));

	// Get the world space coordinates of the screen space point from the event
	// (The glass plate of the screen is considered to be at at z = 0 in world space
	camera.getWorldCoordinates(eventPointScreen, 0f, eventPointWorld);

	// Compute the diff and create the ray
	eventPointWorld.subtract(eyePosWorld, directionWorld);
	return new Ray(eyePosWorld, directionWorld);
    }

    /**
     * Actually perform the pick.
     */
    private PickInfo pickEventScreenPos (int x, int y) {
	if (cameraComp == null) return null;

	logger.fine(" at " + x + ", " + y);
	Ray pickRayWorld = calcPickRayWorld(x, y);

	// Note: pickAll is needed to in order to pick through transparent objects
        return collisionSys.pickAllWorldRay(pickRayWorld, true, false/*TODO:interp*/);
    }

    private GrabChangeType evaluateButtonGrabStateChange (int eventID, MouseEvent e) {
	int modifiers = convertAwtEventModifiersToPassiveGrabModifiers(e.getModifiers());

	if (eventID == MouseEvent.MOUSE_PRESSED) {
	    // Button press
	    return GrabChangeType.GRAB_ACTIVATE;
	} else if (eventID == MouseEvent.MOUSE_RELEASED &&
	           (modifiers & PG_ALL_BUTTONS_MASK) == 0) {
	    // Button release: Similar to X11: terminate grab only when all buttons are released	
	    return GrabChangeType.GRAB_DEACTIVATE;
	}

	return GrabChangeType.GRAB_NO_CHANGE;
    }

    // Returns true if the button release is close enough to the button press
    // so as to consitute a click event.
    // Note: These are Java-on-Windows behavior. There is no click event position
    // threshold on Java-on-Linux. But Hideya wants the Windows behavior.

    private final boolean buttonWithinClickThreshold (int x, int y) {
	return Math.abs(x - buttonLastX) <= BUTTON_CLICK_POSITION_THRESHOLD &&
	       Math.abs(y - buttonLastY) <= BUTTON_CLICK_POSITION_THRESHOLD;
    }

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

    public static Entity pickDetailsToEntity (PickDetails pickDetails) {
        if (pickDetails==null)
            return null;
	CollisionComponent cc = pickDetails.getCollisionComponent();
	if (cc == null) return null;
	return cc.getEntity();
    }

    /**
     * Converts a 2D AWT event into a Wonderland event.
     */
    protected abstract Event createWonderlandEvent (AWTEvent awtEvent);

    /**
     * Generate the appropriate enter/exit events. 
     *
     * NOTE: the pointer can be inside an entity, but if this entity is obscured by
     * an entity with event listeners that don't propagate to under then the obscured
     * entity is effectively exitted. This is an example of an "event shadow."
     */
    private void generateEnterExitEvents (MouseEvent awtEvent, PickInfo pickInfo) {

	MouseEnterExitEvent3D enterEventProto = 
	    createEnterExitEventFromAwtEvent(awtEvent, MouseEvent.MOUSE_ENTERED);
	MouseEnterExitEvent3D exitEventProto = 
	    createEnterExitEventFromAwtEvent(awtEvent, MouseEvent.MOUSE_EXITED);

	// Calculate which entities have the pointer inside
	calcInsideEntities(awtEvent, enterEventProto, pickInfo);

	//System.err.println("awtEvent = " + awtEvent);
	//System.err.println("insideEntities = " + insideEntities);

	// Calculate entities which had the pointer inside on the last event
	// but which no longer have the pointer inside
	noLongerInsideEntities.clear();
	for (EntityAndPickDetails entry : insideEntitiesPrev) {
	    boolean found = false;
	    for (EntityAndPickDetails entryInside : insideEntities) {
		if (entry.entity.equals(entryInside.entity)) {
		    found = true;
		    break;
		}
	    }
	    if (!found) {
		noLongerInsideEntities.add(entry);
	    }
	}

	//System.err.println("noLongerInsideEntities = " + noLongerInsideEntities);

	// Calculate entities which did not have the pointer inside on the last
	// event but now have the pointer inside.
	newlyInsideEntities.clear();
	for (EntityAndPickDetails entry : insideEntities) {
	    boolean found = false;
	    for (EntityAndPickDetails entryInsidePrev : insideEntitiesPrev) {
		if (entry.entity.equals(entryInsidePrev.entity)) {
		    found = true;
		    break;
		}
	    }
	    if (!found) {
		newlyInsideEntities.add(entry);
	    }
	}

	//System.err.println("newlyInsideEntities = " + newlyInsideEntities);

	// Send the exit events to the no longer inside entities.
	sendExitEvents(exitEventProto, pickInfo);

	// Send the enter events to the newly inside entities.
	sendEnterEvents(enterEventProto, pickInfo);

	// Remember the inside entities for the event
	insideEntitiesPrev.clear();
	insideEntitiesPrev.addAll(insideEntities);
    }


    /**
     * Calculate the current set of entities the pointer is inside.
     */
    private void calcInsideEntities (MouseEvent awtEvent, MouseEnterExitEvent3D enterEventProto,
				     PickInfo pickInfo) {
	
	// Calculate the current set of entities the pointer is inside.
	insideEntities.clear();
	if (awtEvent.getID() == MouseEvent.MOUSE_EXITED || pickInfo == null) {
	    // Note: Canvas exit event is treated just like a pick miss (pickInfo is null)
	    return;
	}

	// Gather up entities which intersect the pick ray until we encounter an
	// entity which doesn't propagate to under.
	boolean propagatesToUnder = true;
	PickDetails pickDetails = pickInfo.get(0);
	int idx = 0;
	while (pickDetails != null && idx < destPickInfo.size() && propagatesToUnder) {
	    Entity entity = pickDetailsToEntity(pickDetails);
	    insideEntities.add(new EntityAndPickDetails(entity, pickDetails));
	    
	    propagatesToUnder = false;
	    EventListenerCollection listeners = (EventListenerCollection) 
		entity.getComponent(EventListenerCollection.class);
	    
	    if (listeners == null) { 
		propagatesToUnder = false;
	    } else {
		Iterator<EventListener> it = listeners.iterator();
		while (it.hasNext()) {
		    EventListener listener = it.next();
		    if (listener.isEnabled()) {
			MouseEnterExitEvent3D distribEvent = (MouseEnterExitEvent3D) 
			    EventDistributor.createEventForEntity(enterEventProto, entity);
			distribEvent.setPickDetails(pickDetails);
			distribEvent.setPickInfo(pickInfo);
			propagatesToUnder |= listener.propagatesToUnder(distribEvent);
		    }
		}
		if (propagatesToUnder) {
		    idx++;
		    if (idx < destPickInfo.size()) {
			pickDetails = destPickInfo.get(idx);
		    } else {
			pickDetails = null;
		    }
		}
	    }	    
	}
    }

    /**
     * Send the exit events to the no longer inside entities.
     */
    private void sendExitEvents (MouseEnterExitEvent3D exitEventProto, PickInfo pickInfo) {
	for (EntityAndPickDetails entry : noLongerInsideEntities) {
	    MouseEnterExitEvent3D exitEvent = (MouseEnterExitEvent3D) 
		EventDistributor.createEventForEntity(exitEventProto, entry.entity);
	    exitEvent.setPickDetails(entry.pickDetails);
	    exitEvent.setPickInfo(pickInfo);
	    //System.err.println("Try sending exitEvent = " + exitEvent);
	    //System.err.println("Try entity = " + entry.entity);
	    tryListenersForEntity(entry.entity, exitEvent);
	}
    }

    /**
     * Send the enter events to the newly inside entities.
     */
    private void sendEnterEvents (MouseEnterExitEvent3D enterEventProto, PickInfo pickInfo) {
	for (EntityAndPickDetails entry : newlyInsideEntities) {
	    MouseEnterExitEvent3D enterEvent = (MouseEnterExitEvent3D) 
		EventDistributor.createEventForEntity(enterEventProto, entry.entity);
	    enterEvent.setPickDetails(entry.pickDetails);
	    enterEvent.setPickInfo(pickInfo);
	    tryListenersForEntity(entry.entity, enterEvent);
	}
    }

    /**
     * Try to send the given event to the listeners for the given entity.
     */
    private void tryListenersForEntity (Entity entity, Event event) {
	EventListenerCollection listeners = 
	    (EventListenerCollection) entity.getComponent(EventListenerCollection.class);
	if (listeners != null && listeners.size() > 0) { 
	    Iterator<EventListener> it = listeners.iterator();
	    while (it.hasNext()) {
		EventListener listener = it.next();
		if (listener.isEnabled()) {
		    logger.fine("Calling consume for listener " + listener);
		    if (listener.consumesEvent(event)) {
			logger.fine("CONSUMED by entity " + entity);
			listener.postEvent(event);
		    }
		}
	    }
	}
    }

    private MouseEnterExitEvent3D createEnterExitEventFromAwtEvent (MouseEvent awtEvent, int id) {
	int x = awtEvent.getX();
	int y = awtEvent.getY();
	long when = awtEvent.getWhen();
	int modifiers = awtEvent.getModifiers();
	int button = awtEvent.getButton();
	MouseEvent me = new MouseEvent(dummyButton, id, when, modifiers, x, y, 0, false, button);
	return (MouseEnterExitEvent3D) createWonderlandEvent(me);
    }
}


