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
package org.jdesktop.wonderland.modules.appbase.client.gui.guidefault;

import com.jme.math.Vector3f;
import com.jme.renderer.ColorRGBA;
import com.jme.scene.Node;
import org.jdesktop.wonderland.modules.appbase.client.ControlArb;
import org.jdesktop.wonderland.modules.appbase.client.WindowView;
import org.jdesktop.wonderland.common.ExperimentalAPI;

/**
 * The generic superclass of window frame components.
 *
 * @author deronj
 */ 

@ExperimentalAPI
public abstract class FrameComponent extends Node /* TODO: extends EventNode */ {

    /** The color to display when the app has control. */
    protected static final ColorRGBA HAS_CONTROL_COLOR = new ColorRGBA(0f, 0.9f, 0f, 1f);

    /** The color to display when the app has control. */
    protected static final ColorRGBA NO_CONTROL_COLOR = new ColorRGBA(0.9f, 0f, 0f, 1f);

    /** The view of the window the frame encloses. */
    protected ViewWorldDefault view;

    /** The control arb of the app. */
    protected ControlArb controlArb;

    /** The event handler of this component. */
    // TODO: protected Gui2D gui;
    protected Object gui;

    /** 
     * Create a new instance of <code>FrameComponent</code>.
     *
     * @param view The view the frame encloses.
     * @param gui The event handler.
     */
    public FrameComponent (String name, WindowView view, /*TODO Gui2D*/ Object gui) {
	super(name);
	this.view = (ViewWorldDefault) view;
	this.gui = gui;
	controlArb = view.getWindow().getApp().getControlArb();
    }

    /**
     * Clean up resources.
     */
    public void cleanup () {
	view = null;
	if (gui != null) {
	    // TODO: gui.cleanup();
	    gui = null;
	}
    }

    /**
     * The size of the view has changed. Make the corresponding
     * position and/or size updates for this frame component.
     *
     * @throw InstantiationException if couldn't allocate resources for the visual representation.
     */
    public void update () throws InstantiationException {
	updateColor();
    }

    /**
     * The control state of the app has changed. Make the corresponding change in the frame.
     *
     * @param controlArb The app's control arb.
     */
    public void updateControl (ControlArb controlArb) {
	updateColor();
    }

    /**
     * Update the component color based on whether the user has control of the app.
     */
    protected void updateColor () {
        if (controlArb == null || controlArb.hasControl()) {
	    setColor(HAS_CONTROL_COLOR);
        } else {
	    setColor(NO_CONTROL_COLOR);
	}               
    }

    /**
     * Set the background color of the component.
     *
     * @param color The new background color.
     */
    public abstract void setColor (ColorRGBA color);

    /**
     * Get the background color of the component.
     */
    public abstract ColorRGBA getColor ();

    /**
     * Remove a child component from this component.
     *
     * @param comp The child to remove.
     */
    public void removeChild (FrameComponent comp) {
	detachChild(comp);
    }

    /**
     * Set the translation of this component.
     *
     * @param trans The translation vector.
     */
    public void setTranslation (Vector3f trans) {
	setLocalTranslation(trans);
    }
}