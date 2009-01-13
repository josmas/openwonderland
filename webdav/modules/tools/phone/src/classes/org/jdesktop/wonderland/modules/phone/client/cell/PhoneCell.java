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
package org.jdesktop.wonderland.modules.phone.client.cell;

import  org.jdesktop.wonderland.modules.phone.common.CallListing;

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

import org.jdesktop.wonderland.modules.phone.common.PhoneCellConfig;

import org.jdesktop.wonderland.client.comms.ClientConnection;
import org.jdesktop.wonderland.client.comms.WonderlandSession;

/**
 *
 * @author jkaplan
 */
public class PhoneCell extends Cell implements CellStatusChangeListener {

    private static final Logger logger =
            Logger.getLogger(PhoneCell.class.getName());

    private PhoneForm phoneForm;
    
    private static final float HOVERSCALE = 1.5f;
    private static final float NORMALSCALE = 1.25f;
    
    private CallListing mostRecentCallListing;
         
    //private boolean projectorState;
    
    //private ProjectorStateUpdater projectorStateUpdater;
        
    private boolean locked;
    private boolean simulateCalls;
    private String phoneNumber;
    private String password;
    private String phoneLocation;
    private double zeroVolumeRadius;
    private double fullVolumeRadius;

    private PhoneMessageHandler phoneMessageHandler;

    public PhoneCell(CellID cellID, CellCache cellCache) {
        super(cellID, cellCache);

	logger.fine("CREATED NEW PHONE CELL " + cellID);

	CellManager.getCellManager().addCellStatusChangeListener(this);
    }

    public void cellStatusChanged(Cell cell, CellStatus status) {
	logger.fine("got status " + status + " for cell " + cell.getCellID());

        if (cell.getCellID() != getCellID()) {
            return;
        }

	if (status.equals(CellStatus.ACTIVE) && phoneMessageHandler == null) {
	    phoneMessageHandler = new PhoneMessageHandler(this);
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

	PhoneCellConfig config = (PhoneCellConfig) setupData;

	locked = config.getLocked();
        simulateCalls = config.getSimulateCalls();
        phoneNumber = config.getPhoneNumber();
        password = config.getPassword();
        phoneLocation = config.getPhoneLocation();
	zeroVolumeRadius = config.getZeroVolumeRadius();
	fullVolumeRadius = config.getFullVolumeRadius();
    }

    public String getPhoneNumber() {
	return phoneNumber;
    }

    public boolean getLocked() {
	return locked;
    }

    public String getPassword() {
	return password;
    }

    public WonderlandSession getSession() {
	return getCellCache().getSession();
    }

    public void phoneSelected() {
	if (phoneMessageHandler == null) {
	    logger.warning("No phoneMessageHandler");
	    return;
	}

	phoneMessageHandler.phoneSelected();
    }

    @Override
    protected CellRenderer createCellRenderer(RendererType rendererType) {
        if (rendererType == RendererType.RENDERER_JME) {
            return new PhoneCellRenderer(this);
        }

        throw new IllegalStateException("Cell does not support " + rendererType);
    }

}
