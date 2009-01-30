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
package org.jdesktop.wonderland.modules.appbase.client.gui.guinull;

import org.jdesktop.wonderland.modules.appbase.client.AppCell;
import org.jdesktop.wonderland.modules.appbase.client.ControlArb;
import org.jdesktop.wonderland.modules.appbase.client.Window2DFrame;
import org.jdesktop.wonderland.modules.appbase.client.Window2DView;
import org.jdesktop.wonderland.modules.appbase.client.WindowView;
import org.jdesktop.wonderland.common.ExperimentalAPI;

/**
 * A null implementation of Window2DFrame which renders nothing.
 *
 * @author deronj
 */ 

@ExperimentalAPI
public class Window2DFrameNull extends Window2DFrame {

    /**
     * Create a new instance of Window2DFrameNull.
     *
     * @param view The (null) view the frame encloses.
     */
    public Window2DFrameNull (WindowView frameView) {
	super((Window2DView)frameView);
    }

    /**
     * {@inheritDoc}
     */
    public void cleanup () {}

    /**
     * Returns the cell of this view.
     */
    private AppCell getCell () {
	return getView().getWindow().getCell();
    }

    /**
     * Disconnect the frame components from the frame's window view.
     */
    void disconnect () {
    }

    /** 
     * The size of the view has changed. Make the corresponding
     * position and/or size updates for the frame components.
     *
     * @throw InstantiationException if couldn't allocate resources for the visual representation.
     */
    void update () throws InstantiationException {}

    /**
     * {@inheritDoc}
     */
    public void setTitle (String title) {}
    
    /**
     * {@inheritDoc}
     */
    public void updateControl (ControlArb controlArb) {}
}

