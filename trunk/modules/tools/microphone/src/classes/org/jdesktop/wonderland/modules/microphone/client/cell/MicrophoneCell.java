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

package org.jdesktop.wonderland.modules.microphone.client.cell;

import com.sun.sgs.client.ClientChannel;

import java.util.logging.Logger;

import org.jdesktop.wonderland.common.messages.Message;

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
import org.jdesktop.wonderland.client.cell.ChannelComponent;
import org.jdesktop.wonderland.client.cell.CellStatusChangeListener;

import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.CellStatus;

import org.jdesktop.wonderland.common.cell.state.CellClientState;

import org.jdesktop.wonderland.modules.microphone.common.MicrophoneCellConfig;

import org.jdesktop.wonderland.client.comms.ClientConnection;
import org.jdesktop.wonderland.client.comms.WonderlandSession;

/**
 *
 * @author jkaplan
 */
public class MicrophoneCell extends Cell implements CellStatusChangeListener {

    private static final Logger logger =
            Logger.getLogger(MicrophoneCell.class.getName());

    private MicrophoneMessageHandler microphoneMessageHandler;

    public MicrophoneCell(CellID cellID, CellCache cellCache) {
        super(cellID, cellCache);

	logger.fine("CREATED NEW CONEOFSILENCE CELL " + cellID);

	CellManager.getCellManager().addCellStatusChangeListener(this);
    }

    public void cellStatusChanged(Cell cell, CellStatus status) {
	logger.fine("got status " + status + " for cell " + cell.getCellID());

        if (cell.getCellID() != getCellID()) {
            return;
        }

	if (status.equals(CellStatus.ACTIVE) && microphoneMessageHandler == null) {
	    microphoneMessageHandler = new MicrophoneMessageHandler(this);
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
    public void setClientState(CellClientState setupData) {
	super.setClientState(setupData);

	MicrophoneCellConfig config = (MicrophoneCellConfig) setupData;
    }

    public WonderlandSession getSession() {
	return getCellCache().getSession();
    }

    @Override
    protected CellRenderer createCellRenderer(RendererType rendererType) {
        if (rendererType == RendererType.RENDERER_JME) {
            return new MicrophoneCellRenderer(this);
        }

        throw new IllegalStateException("Cell does not support " + rendererType);
    }

}
