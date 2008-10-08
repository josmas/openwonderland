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
package org.jdesktop.wonderland.modules.appbase.client;

import org.jdesktop.wonderland.common.ExperimentalAPI;

/**
 * An interface used by view world objects provided by the gui factory.
 *
 * @author deronj
 */

public interface Window2DViewWorld {

    /**
     * Sets the dimensions of the position of the view. Don't forget to 
     * call update(CHANGED_SIZE) afterward.
     *
     * @param width The new width of the window.
     * @param height The new height of the window.
     */
    public void setSize (float width, float height);

    /**
     * Returns the width of the window.
     */
    public float getWidth ();

    /**
     * Returns the height of the window.
     */
    public float getHeight ();

    /**
     * Specify whether the view is top-level. Don't forget to also
     * call update(CHANGED_TOP_LEVEL) afterward.  
     *
     * @param topLevel True if the window should be a topLevel window.
     */
    public void setTopLevel (boolean topLevel);

    /**
     * Returns whether the window of the view is top-level.
     */
    public boolean getTopLevel ();

    /**
     * Sets the visibility of the view (independent of the window).
     * Don't forget to call update(CHANGED_VISIBILITY) afterward.
     *
     * @param visible True if the window should be made visible.
     */
    public void setVisible (boolean visible);

    /**
     * Returns the visibility of the view (independent of the window)
     */
    public boolean getVisible ();
}
