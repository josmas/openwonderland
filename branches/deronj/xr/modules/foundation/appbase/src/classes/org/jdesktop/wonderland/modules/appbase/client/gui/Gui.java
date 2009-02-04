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
package org.jdesktop.wonderland.modules.appbase.client.gui;

import org.jdesktop.wonderland.common.ExperimentalAPI;
import org.jdesktop.wonderland.modules.appbase.client.ControlArb;
import org.jdesktop.wonderland.modules.appbase.client.Window;

/**
 * Generic app event handler for 2D and 3D window views.
 *
 * @author deronj
 */
@ExperimentalAPI
public abstract class Gui {

    /** This Gui's view */
    protected WindowView view;
    /** The window displayed by this Gui's view */
    protected Window window;
    /** The control arb of the associated application */
    protected ControlArb controlArb;

    /**
     * Create a new instance of Gui.
     *
     * @param view The view associated with the component that uses this Gui.
     */
    public Gui(WindowView view) {
        this.view = view;
        window = (Window) view.getWindow();
        controlArb = window.getApp().getControlArb();
    }

    /**
     * Clean up resources.
     */
    public void cleanup() {
        window = null;
        view = null;
        controlArb = null;
    }

    /**
     * Return the GUI's control arb
     */
    protected ControlArb getControlArb() {
        return controlArb;
    }
}
