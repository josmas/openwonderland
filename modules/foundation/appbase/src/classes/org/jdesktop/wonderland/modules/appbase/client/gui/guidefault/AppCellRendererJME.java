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

import com.jme.scene.Node;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.wonderland.modules.appbase.client.AppCell;
import org.jdesktop.wonderland.modules.appbase.client.AppCellRenderer;
import org.jdesktop.wonderland.modules.appbase.client.WindowView;

/**
 * A cell renderer which uses JME to render app cell contents. It creates
 * a rootNode which it hands off to mtgame. It allows window views to be
 * attached. When a window view is attached the base node of the window is 
 * added as a child of this rootNode.
 *
 * @author dj
 */
public class AppCellRendererJME extends AppCellRenderer {

    /** The root node of the cell. */
    private Node rootNode;

    /** 
     * Create a new instance of AppCellRendererJME.
     * @param cell The cell to be rendered.
     */
    public AppCellRendererJME (AppCell cell) {
        super(cell);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected Node createSceneGraph (Entity entity) {

	Node ret = new Node();
        ret.setName("AppCellRendererNull node for cell " + cell.getCellID().toString());

        applyTransform(ret, cell.getLocalTransform());

	return ret;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void attachView (WindowView view) {
	ViewWorldDefault viewWorld = (ViewWorldDefault) view;
	rootNode.attachChild(viewWorld.getBaseNode());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void detachView (WindowView view) {
	ViewWorldDefault viewWorld = (ViewWorldDefault) view;
	rootNode.detachChild(viewWorld.getBaseNode());
    }
}
