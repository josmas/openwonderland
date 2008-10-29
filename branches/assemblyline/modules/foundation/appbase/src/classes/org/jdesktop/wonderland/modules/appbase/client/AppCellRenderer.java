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

import org.jdesktop.wonderland.client.jme.cellrenderer.BasicRenderer;

/**
 * A cell renderer for app cells which allows views to be attached to it.
 *
 * @author dj
 */

public abstract class AppCellRenderer extends BasicRenderer {

    /**
     * Create a new instance of AppCellRenderer.
     * @param cell The cell to be rendered.
     */
    public AppCellRenderer (AppCell cell) {
	super(cell);
    }

    /**
     * Attaches the given view to the scene graph of the cell renderer.
     * @param view The view to attach.
     */
    public abstract void attachView (WindowView view);

    /**
     * Detaches the given view from the scene graph of the cell renderer.
     * @param view The view to detach.
     */
    public abstract void detachView (WindowView view);
}
