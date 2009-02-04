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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.logging.Logger;
import com.jme.bounding.BoundingBox;
import com.jme.bounding.BoundingVolume;
import javax.swing.JOptionPane;
import org.jdesktop.wonderland.common.ExperimentalAPI;
import org.jdesktop.wonderland.modules.appbase.client.gui.Displayer;

/**
 * The generic application superclass. All apps in Wonderland have this
 * root class. 
 * <br><br>
 * Before an app can become visible in the world it must be associated with a Displayer which
 * provides for the display of the app, such as an app cell in the world or the HUD.  
 * The displayer is configured using <code>setDisplayer</code>. <code>setDisplayer</code> 
 * may be called only once for the app. Once called, the app remains permanently associated 
 * with that displayer until the app or the displayer is destroyed.
 *
 * When you are done with this app you should call <code>cleanup</code> to clean up its resources.
 * (This is optional).
 *
 * @author deronj
 */
@ExperimentalAPI
public class App {

    /** The logger for app.base */
    static final Logger logger = Logger.getLogger("wl.app.base");
    /** The list of all windows created by this app */
    protected LinkedList<Window> windows = new LinkedList<Window>();
    /** The type of this app */
    protected AppType appType;
    /** The control arbiter for this app. null means that all users can control the app at the same time */
    protected ControlArb controlArb;
    /** The displayer which renders the app. May be null if the app is not displayed in this client. */
    protected Displayer displayer;

    /**
     * Create a new instance of App.
     *
     * @param appType The type of app to create.
     * @param controlArb The control arbiter to use. null means that all users can control the app 
     * at the same time.
     */
    public App(AppType appType, ControlArb controlArb) {
        this.appType = appType;
        this.controlArb = controlArb;
        appType.appAdd(this);
    }

    /**
     * Clean up resources held. Specifically, the control arbiter is cleaned up.
     */
    public void cleanup() {
        if (appType != null) {
            appType.appRemove(this);
            appType = null;
        }

        if (controlArb != null) {
            controlArb.cleanup();
            controlArb = null;
        }

        displayer = null;

        if (windows != null) {
            for (Window window : windows) {
                window.cleanup();
            }
            windows.clear();
            windows = null;
        }
    }

    /**
     * Returns the type of the app.
     */
    public AppType getAppType() {
        return appType;
    }

    /**
     * Returns the Control Arbiter for this app.
     * If this is null the app supports fine-grained control swapping.
     * That is, the app accepts user events from different users equally
     * on a first-come first-served basis.
     */
    public ControlArb getControlArb() {
        return controlArb;
    }

    /**
     * Returns an iterator over all the windows of this app.
     */
    public Iterator<Window> getWindowIterator() {
        return windows.iterator();
    }

    /**
     * Add a new window to this app's list of windows.
     *
     * @param window The window to add.
     */
    synchronized void windowAdd(Window window) {
        windows.add(window);

    /* TODO: control
    Just call window.initControl(controlArb) if controlArb not null?

    if (controlArb != null && controlArb.hasControl()) {
    String controllingUser = controlArb.getControllingUser();
    window.setControllingUser(controllingUser);
    controlArb.addListener(window);
    }
    window.highlightControl();
     */
    }

    /**
     * Remove a window from this app's list of windows.
     *
     * @param window The window to remove.
     */
    synchronized void windowRemove(Window window) {
        windows.remove(window);

    /* TODO: control
    window.setControllingUser(null);
    if (controlArb != null) {
    controlArb.removeListener(window);
    }

    // No need to highlight control because window should be invisible.
     */
    }

    /**
     * Calculate and return the current bounds for this window. This is the 
     * union of the bounds of all of the app windows.
     */
    synchronized BoundingVolume getBounds() {
        BoundingVolume union = new BoundingBox();
        for (Window window : windows) {
            union.merge(window.getBounds());
        }
        return union;
    }

    /**
     * Returns the displayer of this app.
     */
    public Displayer getDisplayer() {
        return displayer;
    }

    /**
     * Used to associate this app with the given displayer.  May only be called one time.
     *
     * @param displayer The world displayer containing the app.
     * @throws IllegalArgumentException If the displayer already is associated
     * with an app.
     * @throws IllegalStateException If the app is already associated 
     * with a displayer.
     */
    public void setDisplayer(Displayer displayer)
            throws IllegalArgumentException, IllegalStateException {
        if (displayer == null) {
            throw new NullPointerException();
        }
        if (this.displayer != null) {
            throw new IllegalStateException("App already has a displayer.");
        }

        this.displayer = displayer;
    }

    protected static void reportLaunchError(String message) {
        JOptionPane.showMessageDialog(null, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Print debug information for all app windows.
     */
    public void printWindowInfosAll() {
        for (Window window : windows) {
            window.printWindowInfo();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append("appType=" + appType.getName());
        buf.append(",bounds=[" + getBounds() + "]");
        return buf.toString();
    }
}
