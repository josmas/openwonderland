/**
 * Project Wonderland
 *
 * Copyright (c) 2004-2009, Sun Microsystems, Inc., All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * Sun designates this particular file as subject to the "Classpath" 
 * exception as provided by Sun in the License file that accompanied 
 * this code.
 */
package org.jdesktop.wonderland.modules.appbase.client;

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.Iterator;
import java.util.LinkedList;
import org.jdesktop.wonderland.common.ExperimentalAPI;

/**
 * A user input control arbiter. Implementations of this interface
 * decide how application control is to be delegated between users.
 * All ControlArbs support the notion of one or more users controlling
 * an app and the rest of the users not controlling the app. When
 * a user has control of an app user input events are sent from
 * app windows to the app. When a user does not control an app 
 * user input events are ignored by its windows.
 *
 * @author deronj
 */
@ExperimentalAPI
public abstract class ControlArb {

    /** A list of controllers in the Wonderland client session */
    protected static LinkedList<ControlArb> controlArbs = new LinkedList<ControlArb>();
    /** A list of components to notify of a state change in the control arb */
    protected LinkedList<ControlChangeListener> listeners =
            new LinkedList<ControlChangeListener>();
    /** The application controlled by this arbiter */
    protected App app;
    /** Has the user enabled app control? */
    // TODO: HACK for debug    protected boolean appControl;
    protected boolean appControl = true;

    /** 
     * The interface that components interested in being notified of a state change in the control arb must implement.
     */
    public interface ControlChangeListener {

        /**
         * The state of a control arb you are subscribed to may have changed. The state of whether this user has
         * control or the current set of controlling users may have changed.
         *
         * @param controlArb The control arb that changed.
         */
        public void updateControl(ControlArb controlArb);
    }

    /** What do the pointer and keyboard currently drive: the world or apps? */
    public enum EventMode {

        WORLD, APP
    };
    private static EventMode eventMode = EventMode.WORLD;

    /** 
     * Create a new instance of ControlArb.
     */
    public ControlArb() {
        controlArbs.add(this);
    }

    /**
     * Clean up resources held.
     */
    public void cleanup() {
        controlArbs.remove(this);
        listeners = null;
        app = null;
    }

    /**
     * Specify the app the controlArb controls.
     *
     * @param app The app.
     */
    public void setApp(App app) {
        this.app = app;
    }

    /**
     * Return the app the controlArb controls.
     */
    public App getApp() {
        return app;
    }

    /**
     * Tell the arbiter that this user is take control of the app.
     * Note that the attempt to take control may be refused. This method
     * doesn't report whether the attempt succeeded or failed--this is
     * is reported via the controller change listener.
     *
     * Note: depending on the implementation, this may cause other users with control 
     * to lose it.
     */
    public void takeControl() {
        if (!hasControl()) {
// TODO	    InputManager3D.getInputManager().setEventMode(InputManager3D.EventMode.APP);
            appControl = true;
            updateControl();
        }
    }

    /**
     * Tell the arbiter that you are releasing control of the app.
     */
    public void releaseControl() {
        if (!hasControl()) {
//TODO	    InputManager3D.getInputManager().setEventMode(InputManager3D.EventMode.WORLD);
            appControl = false;
            updateControl();
        }
    }

    /**
     * Release control of all applications in the Wonderland client session.
     */
    public static void releaseControlAll() {
        for (ControlArb controlArb : controlArbs) {
            controlArb.releaseControl();
        }
    }

    /**
     * Returns the current controlling users.
     * @return An array of user names who are currently controlling this control arb's app.
     * This array is null if there are currently no controlling users.
     */
    public String[] getControllers() {
        return null;
    }

    /**
     * Add a control change listener.
     * 
     * @param listener The control change listener.
     */
    public void addListener(ControlChangeListener listener) {
        listeners.add(listener);
    }

    /**
     * Remove a controller change listener.
     * 
     * @param listener The control change listener.
     */
    public void removeListener(ControlChangeListener listener) {
        listeners.remove(listener);
    }

    /**
     * Returns an iterator over all control change listeners.
     */
    public Iterator<ControlChangeListener> getListeners() {
        return listeners.iterator();
    }

    /**
     * Send a key event to an app window, if the user has control.
     *
     * @param window The window to which to send the event.
     * @param event The event to send.
     */
    public abstract void deliverEvent(Window2D window, KeyEvent event);

    /**
     * Send a non-wheel mouse event to an app window, if the user has control.
     *
     * @param window The window to which to send the event.
     * @param event The event to send.
     */
    public abstract void deliverEvent(Window2D window, MouseEvent event);

    /**
     * Does this control arb currently have app control?
     */
    public boolean hasControl() {
        return appControl;
    }

    public static void setEventMode(EventMode mode) {
        eventMode = mode;
        updateControlAll();
    }

    /**
     * Informs the control change listeners that the control arb state has been updated.
     */
    protected void updateControl() {
        for (ControlChangeListener listener : listeners) {
            listener.updateControl(this);
        }
    }

    /**
     * Informs the control change listeners of all control arbs that the state has been updated.
     */
    protected static void updateControlAll() {
        for (ControlArb controlArb : controlArbs) {
            controlArb.updateControl();
        }
    }
}
