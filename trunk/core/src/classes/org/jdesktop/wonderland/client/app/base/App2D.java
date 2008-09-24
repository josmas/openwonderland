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

import com.jme.math.Vector2f;
import org.jdesktop.wonderland.common.ExperimentalAPI;

/**
 * The generic 2D application superclass. All 2D apps in Wonderland have this
 * root class.
 * <br><br>
 * 2D apps provide a window stack in which to arrange their visible windows.
 * This stack is a list. The top window in the stack is first in the list.
 * The bottom window is the last in the list. 
 * <br><br>
 * The stack position index of the top window is N-1, where N is the number of windows. 
 * The stack position of the bottom window is 0.
 *
 * @author deronj
 */ 

@ExperimentalAPI
public abstract class App2D extends App {

    /** The window stack for this app */
    protected WindowStack stack = new WindowStack();

    /** The world size of pixels */
    protected Vector2f pixelScale;

    /**
     * Create a new instance of App2D.
     *
     * @param appType The type of 2D app to create.
     * @param controlArb The control arbiter to use. null means that all users can control at the same time.
     * @param pixelScale The size of the window pixels in world coordinates.
     */
    public App2D (AppType appType, ControlArb controlArb, Vector2f pixelScale) {
	super(appType, controlArb);
	this.pixelScale = pixelScale;
    }

    /**
     * Deallocate resources.
     */
    public void cleanup () {
	super.cleanup();
	if (stack != null) {
	    stack.cleanup();
	    stack = null;              
	}
	pixelScale = null;
    }

    /** 
     * Returns the pixel scale 
     */
    public Vector2f getPixelScale () {
	return pixelScale;
    }

    /**
     * Add a window to this app. It is added on top of the app's window stack.
     *
     * @param window The window to add.
     */
    public void windowAdd (Window2D window) {
	super.windowAdd(window);
	stack.add(window);
    }

    /**
     * Add the given window to the stack so that it is above the given sibling.
     *
     * @param window The window to add.
     * @param sibling The window that is immediately above the added window.
     */
    public void windowAddSiblingAbove (Window2D window, Window2D sibling) {
	stack.addSiblingAbove(window, sibling);
    }

    /**
     * Remove the given window from the window stack.
     *
     * @param window The window to remove.
     */
    public void windowRemove (Window2D window) {
	super.windowRemove(window);
	stack.remove(window);
    }

    /**
     * Move the given window to the front of the window stack.
     *
     * @param window The window to move.
     */
    public void windowToFront (Window2D window) {
	stack.toFront(window);
    }

    /** 
     * Return the top window of the window stack.
     */
    public Window2D windowGetTop () {
	return stack.getTop();
    }

    /** 
     * Return the bottom window of the window stack.
     */
    public Window2D windowGetBottom () {
	return stack.getBottom();
    }

    /**
     * Rearrange the window stack so that the windows are in the given order.
     *
     * @param order An array which indicates the order in which the windows
     * are to appear in the stack. The window at order[index] should have
     * stack position N-index, where N is the number of windows in the stack.
     */
    public void restackWindows (Window2D[] order) {
	stack.restack(order);
    }
}
