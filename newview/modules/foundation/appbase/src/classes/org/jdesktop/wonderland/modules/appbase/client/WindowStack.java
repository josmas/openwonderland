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

import java.util.LinkedList;
import java.util.NoSuchElementException;
import org.jdesktop.wonderland.common.ExperimentalAPI;

/**
 * The stack of visible windows. The top window is first in the list.
 * The bottom window is the last in the list. The stack position index
 * of the top window is N-1, where N is the number of windows. 
 * The stack position of the bottom window is 0.
 *
 * @author deronj
 */
@ExperimentalAPI
class WindowStack {

    /** The list of windows in the stack. Windows in the list appear in top to bottom order */
    protected LinkedList<Window2D> stack = new LinkedList<Window2D>();

    /**
     * Deallocate resources.
     */
    public synchronized void cleanup() {
        if (stack != null) {
            stack.clear();
            stack = null;
        }
    }

    /**
     * Return the position of the given window in the stack. 0 is the bottom window.
     *
     * @param window The window whose stack position is to be returned.
     */
    public synchronized int getStackPosition(Window2D window) {
        int index = stack.indexOf(window);
        if (index < 0) {
            return index;
        }
        return stack.size() - 1 - index;
    }

    /**
     * Add a new window to the top of the stack. If the window is already in the stack,
     * moves it to the top.
     *
     * @param window The window to be added.
     */
    public synchronized void add(Window2D window) {
        if (stack.indexOf(window) > 0) {
            stack.remove(window);
        }
        stack.addFirst(window);
        //printStack();
    }

    /**
     * Add the given window to the stack so that it is above the given sibling window.
     *
     * @param window The window to be added.
     * @param sibWin The sibling window.
     */
    public synchronized void addSiblingAbove(Window2D window, Window2D sibWin) {
        if (getStackPosition(window) >= 0) {
            stack.remove(window);
        }

        int indexOfSibling = stack.indexOf(sibWin);
        if (indexOfSibling < 0) {
            stack.addFirst(window);
        } else {
            stack.add(indexOfSibling, window);
        }
    }

    /**
     * Remove the given window from the stack.
     *
     * @param window The window to be removed.
     */
    public synchronized void remove(Window2D window) {
        stack.remove(window);
    }

    /**
     * Move the given window to the front of the stack.
     *
     * @param window The window to be moved.
     */
    public synchronized void toFront(Window2D window) {
        stack.remove(window);
        stack.addFirst(window);
    }

    /** 
     * Return the top window of the stack.
     */
    public synchronized Window2D getTop() {
        try {
            return stack.getFirst();
        } catch (NoSuchElementException ex) {
            return null;
        }
    }

    /** 
     * Return the bottom window of the stack.
     */
    public synchronized Window2D getBottom() {
        try {
            return stack.getLast();
        } catch (NoSuchElementException ex) {
            return null;
        }
    }

    /**
     * Rearrange the window stack so that the windows are in the given order.
     *
     * @param order An array of Window2D. At the completion of this method, the window order[0] should be the
     * bottommost window on the stack and the window order[order.length-1] should be the topmost window on the
     * stack.
     */
    public synchronized void restack(Window2D[] order) {
        stack.clear();
        for (Window2D win : order) {
            if (win != null) {
                stack.addFirst(win);
            }
        }
    }
}
