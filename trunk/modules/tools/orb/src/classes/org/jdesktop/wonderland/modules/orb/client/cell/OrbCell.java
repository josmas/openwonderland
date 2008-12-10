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

import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.CellStatus;
import org.jdesktop.wonderland.common.cell.config.CellConfig;

import org.jdesktop.wonderland.modules.orb.common.OrbCellConfig;

import org.jdesktop.wonderland.client.comms.WonderlandSession;

/**
 *
 * @author jprovino
 */
public class OrbCell extends Cell implements CellStatusChangeListener {

    private static final Logger logger =
            Logger.getLogger(OrbCell.class.getName());

    private static final float HOVERSCALE = 1.5f;
    private static final float NORMALSCALE = 1.25f;
    
    private OrbMessageHandler orbMessageHandler;

    private String callID;

    public OrbCell(CellID cellID, CellCache cellCache) {
        super(cellID, cellCache);

	logger.fine("CREATED NEW ORB CELL " + cellID);

	CellManager.getCellManager().addCellStatusChangeListener(this);
    }

    public void cellStatusChanged(Cell cell, CellStatus status) {
        logger.fine("got status " + status + " for cell " + cell.getCellID());

        if (cell.getCellID() != getCellID()) {
            return;
        }

        if (status.equals(CellStatus.ACTIVE) && orbMessageHandler == null) {
            orbMessageHandler = new OrbMessageHandler(this);
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
    public void configure(CellConfig setupData) {
	super.configure(setupData);

	logger.fine("ORB is configured");
	OrbCellConfig config = (OrbCellConfig) setupData;
    }

    @Override
    protected CellRenderer createCellRenderer(RendererType rendererType) {
	WonderlandSession session = getCellCache().getSession();

	logger.fine("Create cell renderer...");

        if (rendererType == RendererType.RENDERER_JME) {
	    return new OrbCellRenderer(this);
        }

        throw new IllegalStateException("Cell does not support " + rendererType);
    }

    public void orbSelected() {
	if (orbMessageHandler == null) {
	    logger.warning("No phoneMessageHandler");
	    return;
	}

	orbMessageHandler.orbSelected();
    }

}
