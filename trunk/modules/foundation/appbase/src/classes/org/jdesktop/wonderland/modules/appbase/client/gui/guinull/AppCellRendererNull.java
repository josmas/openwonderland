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

import com.jme.scene.Node;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.wonderland.modules.appbase.client.AppCell;
import org.jdesktop.wonderland.modules.appbase.client.AppCellRenderer;
import org.jdesktop.wonderland.modules.appbase.client.WindowView;

/**
 * A cell renderer which doesn't render anything.
 *
 * @author dj
 */
public class AppCellRendererNull extends AppCellRenderer {

    /** 
     * Create a new instance of AppCellRendererNull.
     * @param cell The cell to be rendered.
     */
    public AppCellRendererNull (AppCell cell) {
        super(cell);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected Node createSceneGraph(Entity entity) {
	// Return a dummy node
	Node ret = new Node();
        ret.setName("AppCellRendererNull node for cell " + cell.getCellID().toString());
	return ret;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void attachView (WindowView view) {
	// Do nothing
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void detachView (WindowView view) {
	// Do nothing
    }

    /**
     * {@inheritDoc}
     */
    public void logSceneGraph () {
    }
}
