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
package org.jdesktop.wonderland.client.app.base;

import java.util.Iterator;
import java.util.LinkedList;
import org.jdesktop.wonderland.common.ExperimentalAPI;

/**
 * The generic root class of all WindowFrame2D objects.
 *
 * @author deronj
 */

@ExperimentalAPI
public class Window2DFrame 
    extends WindowFrame
    implements ControlArb.ControlChangeListener 
{
    /* TODO: planned: Frame2DHUD, Frame2DDock */

    /** List of listeners to notify when the frame is closed */
    protected LinkedList<CloseListener> closeListeners = new LinkedList();

    /** The view this frame wraps */
    protected Window2DView view;

    /** The control arbiter of the window */
    protected ControlArb controlArb;

    /** The window title displayed in the frame */
    protected String title;

    /**
     * Components who wish to be notified when the user has pressed the
     * close button should implement this interface and register themselves
     * with addCloseListener.
     */
    public interface CloseListener {

	/**
	 * Called when the user clicks on the frame's close button.
	 */
	public void close ();
    }

    /** 
     * Create a new instance of Window2DFrame.
     * @param view The window the frame wraps.
     */
    public Window2DFrame (Window2DView view) {
	this.view = view;
	controlArb = view.getWindow().getApp().getControlArb();
	if (controlArb != null) {
	    controlArb.addListener(this);
	}
    }

    /**
     * Clean up resources.
     */
    public void cleanup () {
	if (closeListeners != null) {
	    closeListeners.clear();
	    closeListeners = null;
	}
	if (controlArb != null) {
	    controlArb.removeListener(this);
	    controlArb = null;
	}
	view = null;
    }

    /**
     * Return the view the frame wraps.
     */
    public Window2DView getView () {
	return view;
    }

    /**
     * Add a close listener.
     *
     * @param listener The listener to add.
     */
    public void addCloseListener (CloseListener listener) {
	closeListeners.add(listener);
    }

    /**
     * Remove a close listener.
     *
     * @param listener The listener to remove.
     */
    public void removeCloseListener (CloseListener listener) {
	closeListeners.remove(listener);
    }

    /**
     * Returns an iterator over all close listeners.
     */
    public Iterator<CloseListener> getCloseListeners () {
	return closeListeners.iterator();
    }

    /**
     * Specify the title to be displayed in the frame header.
     *
     * @param title The title to display.
     */
    public void setTitle (String title) {
	this.title = title;
    }

    /**
     * Returns the title of the frame.
     */
    public String getTitle () {
	return title;
    } 

    /**
     * The control state of the app has changed. Make the corresponding change in the frame.
     *
     * @param controlArb The app's control arb.
     */
    public void updateControl (ControlArb controlArb) {
	// TODO: change highlight
	// TODO: Change controlling user
    }
}