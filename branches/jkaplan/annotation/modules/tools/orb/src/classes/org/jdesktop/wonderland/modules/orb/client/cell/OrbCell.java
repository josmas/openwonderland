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
package org.jdesktop.wonderland.modules.orb.client.cell;

import com.sun.sgs.client.ClientChannel;

import java.util.logging.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;

import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.cell.Cell.RendererType;
import org.jdesktop.wonderland.client.cell.CellCache;
import org.jdesktop.wonderland.client.cell.CellManager;
import org.jdesktop.wonderland.client.cell.CellRenderer;
import org.jdesktop.wonderland.client.cell.CellStatusChangeListener;
import org.jdesktop.wonderland.client.cell.MovableComponent;

import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.CellStatus;

import org.jdesktop.wonderland.common.cell.state.CellClientState;

import org.jdesktop.wonderland.client.comms.WonderlandSession;

import org.jdesktop.wonderland.modules.orb.common.OrbCellClientState;

/**
 *
 * @author jprovino
 */
public class OrbCell extends Cell implements CellStatusChangeListener {

    private static final Logger logger =
            Logger.getLogger(OrbCell.class.getName());

    private static final float HOVERSCALE = 1.5f;
    private static final float NORMALSCALE = 1.25f;
    
    private OrbCellRenderer orbCellRenderer;

    private OrbMessageHandler orbMessageHandler;

    public OrbCell(CellID cellID, CellCache cellCache) {
        super(cellID, cellCache);

	logger.fine("CREATED NEW ORB CELL " + cellID);

	//addComponent(new MovableComponent(this));

	CellManager.getCellManager().addCellStatusChangeListener(this);
    }

    public void cellStatusChanged(Cell cell, CellStatus status) {
        logger.finer("callStatusChanged got status " + status + " for cell " + cell.getCellID());

        if (cell.getCellID() != getCellID()) {
            return;
        }

        if (status.equals(CellStatus.ACTIVE) && orbMessageHandler == null) {
	    logger.fine("Creating orb Message handler for " + cell.getCellID());
            orbMessageHandler = new OrbMessageHandler(this);
        } else if (status.equals(CellStatus.DISK) && orbMessageHandler != null) {
	    orbMessageHandler.done();
	    orbMessageHandler = null;
	}
    }

    /**
     * Called when the cell is initially created and any time there is a 
     * major configuration change. The cell will already be attached to it's parent
     * before the initial call of this method
     * 
     * @param setupData
     */
    @Override
    public void setClientState(CellClientState cellClientState) {
	super.setClientState(cellClientState);

	logger.fine("ORB is configured");
	OrbCellClientState orbCellClientState = (OrbCellClientState) cellClientState;
    }

    @Override
    protected CellRenderer createCellRenderer(RendererType rendererType) {
	WonderlandSession session = getCellCache().getSession();

	logger.fine("Create cell renderer...");

        if (rendererType == RendererType.RENDERER_JME) {
	    orbCellRenderer = new OrbCellRenderer(this);
	    return orbCellRenderer;
        }

        throw new IllegalStateException("Cell does not support " + rendererType);
    }

    public void removeMouseListener() {
	orbCellRenderer.removeMouseListener();
    }

    public void orbSelected() {
	if (orbMessageHandler == null) {
	    logger.warning("No phoneMessageHandler");
	    return;
	}

	orbMessageHandler.orbSelected();
    }

}
