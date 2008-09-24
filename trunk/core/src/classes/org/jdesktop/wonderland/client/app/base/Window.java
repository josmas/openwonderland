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

import com.jme.bounding.BoundingVolume;
import org.jdesktop.wonderland.common.ExperimentalAPI;

/**
 * The generic app window superclass.
 *
 * @author deronj
 */ 

@ExperimentalAPI
public abstract class Window {

    /** The app to which this window belongs */
    protected App app;

    /** The object which is used to create user-visible objects for this window */
    protected GuiFactory guiFactory;

    /** Is the window visible? (that is, is it showing?) */
    protected boolean visible;

    /**
     * Create a Window2D instance. Initially the window is not visible.
     *
     * @param app The application to which this window belongs
     */
    public Window (App app) {
	this.app = app;
	guiFactory = app.getAppType().getGuiFactory();
	app.windowAdd(this);
    }

    /**
     * Clean up resources held.
     */
    public void cleanup () {
	if (app != null) {
	    app.windowRemove(this);
	    app = null;
	}

	guiFactory = null;
	visible = false;
    }

    /**
     * Return the app to which this this window belongs.
     */
    public App getApp () {
	return app;
    }

    /**
     * Change the visibility of the window 
     *
     * @param visible True if the window should be visible.
     */
    public void setVisible (boolean visible) {
	this.visible = visible;
    }

    /** 
     * Is the window visible?  
     */
    public boolean isVisible () { 
	return visible; 
    }

    /**
     * Returns the cell of the window's app.
     */
    public AppCell getCell () {
	return app.getCell();
    }
 
    /**
     * Set the bounds for this window.
     *
     * @param bounds The new bounds of the window
     */
    synchronized void setBounds (BoundingVolume bounds) {
	// TODO
    }

    /**
     * Return the current bounds for this window. 
     */
    synchronized BoundingVolume getBounds () {
	// TODO
	return null;
    }


    /**
     * Print debug information for this window.
     */
    public void printWindowInfo () {
	// TODO
    }
}

