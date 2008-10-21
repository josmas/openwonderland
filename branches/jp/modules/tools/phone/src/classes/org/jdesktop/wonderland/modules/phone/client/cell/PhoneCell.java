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

package org.jdesktop.wonderland.modules.phone.client.cell;

//import org.jdesktop.wonderland.avatarorb.client.cell.AvatarOrbCell;

import  org.jdesktop.wonderland.modules.phone.common.CallListing;

import org.jdesktop.wonderland.modules.phone.common.PhoneConnectionType;

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
import org.jdesktop.wonderland.client.cell.CellRenderer;
import org.jdesktop.wonderland.common.cell.CellID;

import org.jdesktop.wonderland.common.cell.config.CellConfig;

import org.jdesktop.wonderland.modules.phone.common.PhoneCellConfig;

import org.jdesktop.wonderland.client.comms.ClientConnection;
import org.jdesktop.wonderland.client.comms.WonderlandSession;

/**
 *
 * @author jkaplan
 */
public class PhoneCell extends Cell {

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

	logger.warning("CREATED NEW PHONE CELL " + cellID);

	new Connector(this, cellCache.getSession());
    }

    class Connector extends Thread {
	
	private PhoneCell phoneCell;
	private WonderlandSession session;

	public Connector(PhoneCell phoneCell, WonderlandSession session) {
	    this.phoneCell = phoneCell;
	    this.session = session;

	    start();
	}

	public void run() {
	    ClientConnection connection = null;

	    while (connection == null) {
	        try {
		    Thread.sleep(5000);
	        } catch (InterruptedException e) {
	        }

	        logger.warning("Trying to connect...");

		connection = session.getConnection(PhoneConnectionType.CONNECTION_TYPE);

		logger.warning("Session is " + session + " connection is " + connection);
	    }

	    PhoneClient phoneClient = (PhoneClient) connection;

	    phoneClient.setPhoneMessageHandler(
		new PhoneMessageHandler(phoneCell, session, connection));
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
	PhoneCellConfig config = (PhoneCellConfig) setupData;

	locked = config.getLocked();
        simulateCalls = config.getSimulateCalls();
        phoneNumber = config.getPhoneNumber();
        password = config.getPassword();
        phoneLocation = config.getPhoneLocation();
	zeroVolumeRadius = config.getZeroVolumeRadius();
	fullVolumeRadius = config.getFullVolumeRadius();
    }

    public boolean getLocked() {
	return locked;
    }

    public String getPassword() {
	return password;
    }

    @Override
    protected CellRenderer createCellRenderer(RendererType rendererType) {
	WonderlandSession session = getCellCache().getSession();

        if (rendererType == RendererType.RENDERER_JME) {
            return new PhoneCellRenderer(this);
        }

        throw new IllegalStateException("Cell does not support " + rendererType);
    }

}
