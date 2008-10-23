/**
 * Project Looking Glass
 * 
 * $RCSfile: PhoneCellGLO.java,v $
 * 
 * Copyright (c) 2004-2007, Sun Microsystems, Inc., All Rights Reserved
 * 
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 * 
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 * 
 * $Revision: 1.43 $
 * $Date: 2008/06/16 18:08:29 $
 * $State: Exp $ 
 */
package org.jdesktop.wonderland.modules.phone.server.cell;

import com.sun.sgs.app.ManagedReference;

import org.jdesktop.wonderland.modules.phone.common.CallListing;
import org.jdesktop.wonderland.modules.phone.common.PhoneCellSetup;

import com.sun.mpk20.voicelib.app.AudioGroup;
import com.sun.mpk20.voicelib.app.AudioGroupPlayerInfo;
import com.sun.mpk20.voicelib.app.AudioGroupSetup;
import com.sun.mpk20.voicelib.app.Call;
import com.sun.mpk20.voicelib.app.CallSetup;
import com.sun.mpk20.voicelib.app.DefaultSpatializer;
import com.sun.mpk20.voicelib.app.DefaultSpatializer;
import com.sun.mpk20.voicelib.app.FullVolumeSpatializer;
import com.sun.mpk20.voicelib.app.Player;
import com.sun.mpk20.voicelib.app.PlayerSetup;
import com.sun.mpk20.voicelib.app.VoiceManager;
import com.sun.mpk20.voicelib.app.ZeroVolumeSpatializer;

import com.sun.sgs.app.AppContext;
import com.sun.sgs.app.ClientSession;

import com.sun.voip.CallParticipant;
import com.sun.voip.client.connector.CallStatus;

import java.io.IOException;
import java.lang.String;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;

import org.jdesktop.wonderland.common.cell.config.CellConfig;

import org.jdesktop.wonderland.common.cell.setup.BasicCellSetup;

import org.jdesktop.wonderland.common.messages.Message;

import org.jdesktop.wonderland.common.ExperimentalAPI;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.CellTransform;
import org.jdesktop.wonderland.common.cell.CellTransform;
import org.jdesktop.wonderland.common.cell.ClientCapabilities;
import org.jdesktop.wonderland.common.cell.config.CellConfig;

import org.jdesktop.wonderland.common.cell.setup.BasicCellSetup;

import org.jdesktop.wonderland.modules.phone.common.PhoneCellSetup;
import org.jdesktop.wonderland.modules.phone.common.PhoneCellConfig;

import org.jdesktop.wonderland.server.cell.CellMO;

import org.jdesktop.wonderland.server.setup.BasicCellSetupHelper;
import org.jdesktop.wonderland.server.setup.BeanSetupMO;

import com.jme.bounding.BoundingBox;
import com.jme.bounding.BoundingVolume;

import com.jme.math.Vector3f;

/**
 * A server cell that provides conference phone functionality
 * @author jprovino
 */
public class PhoneCellMO extends CellMO implements BeanSetupMO {

    private static final Logger logger =
        Logger.getLogger(PhoneCellMO.class.getName());
     
    private String modelFileName;    
    
    private final static double PRIVATE_DAMPING_COEFFICIENT = 0.5;
    
    private boolean locked;
    private boolean simulateCalls;
    private String phoneNumber;
    private String password;
    private String phoneLocation;
    private double zeroVolumeRadius;
    private double fullVolumeRadius;

    private boolean keepUnlocked = true;

    private int callNumber = 0;

    public PhoneCellMO() {
    }
    
    public PhoneCellMO(Vector3f center, float size) {
        super(new BoundingBox(new Vector3f(), size, size, size), 
	    new CellTransform(null, center));
    }

    @Override
    protected String getClientCellClassName(ClientSession clientSession, 
	    ClientCapabilities capabilities) {

        return "org.jdesktop.wonderland.modules.phone.client.cell.PhoneCell";
    }

    @Override
    public CellConfig getCellConfig(ClientSession clientSession, 
	    ClientCapabilities capabilities) {

        PhoneCellConfig config = new PhoneCellConfig();

	config.setLocked(locked);
	config.setSimulateCalls(simulateCalls);
	config.setPhoneNumber(phoneNumber);
	config.setPassword(password);
	config.setPhoneLocation(phoneLocation);
	config.setZeroVolumeRadius(zeroVolumeRadius);
	config.setFullVolumeRadius(fullVolumeRadius);

	return config;
    }

    @Override
    public void setupCell(BasicCellSetup setup) {
        super.setupCell(setup);

	PhoneCellSetup pcs = (PhoneCellSetup) setup;

	locked = pcs.getLocked();
	simulateCalls = pcs.getSimulateCalls();
	phoneNumber = pcs.getPhoneNumber();
	password = pcs.getPassword();
	phoneLocation = pcs.getPhoneLocation();
	zeroVolumeRadius = pcs.getZeroVolumeRadius();
    }

    @Override
    public void reconfigureCell(BasicCellSetup setup) {
        super.reconfigureCell(setup);
        setupCell(setup);
    }

     /**
     * Return a new BasicCellSetup Java bean class that represents the current
     * state of the cell.
     *
     * @return a JavaBean representing the current state
     */
    public BasicCellSetup getCellMOSetup() {
        /* Create a new BasicCellState and populate its members */
        PhoneCellSetup setup = new PhoneCellSetup();

        /* Set the bounds of the cell */
        BoundingVolume bounds = this.getLocalBounds();

        if (bounds != null) {
            setup.setBounds(BasicCellSetupHelper.getSetupBounds(bounds));
        }

        /* Set the origin, scale, and rotation of the cell */
        CellTransform transform = this.getLocalTransform(null);
        if (transform != null) {
            setup.setOrigin(BasicCellSetupHelper.getSetupOrigin(transform));
            setup.setRotation(BasicCellSetupHelper.getSetupRotation(transform));
            setup.setScaling(BasicCellSetupHelper.getSetupScaling(transform));
        }

        String pw = null;

        if (password != null && password.length() > 0) {
            pw = "";
        }

        //BasicCellSetup<PhoneCellSetup> setup =
        //        (BasicCellSetup<PhoneCellSetup>) setupData;

        //PhoneCellSetup cellSetup = setup.getCellSetup();

        //phoneNumber = cellSetup.getPhoneNumber();

        //password = cellSetup.getPassword();

        //if (password != null && password.length() > 0) {
            locked = true;
        //}

        //simulateCalls = cellSetup.getSimulateCalls();

        //phoneLocation = cellSetup.getPhoneLocation();

        //zeroVolumeRadius = cellSetup.getZeroVolumeRadius();

        //fullVolumeRadius = cellSetup.getFullVolumeRadius();

        //HARRISTODO: Rework Demo Mode to be phone specific (instead of a global static)
        //Currently you can't mix and match modes for multiple phones.

	return setup;
    }

    public boolean getLocked() {
	return locked;
    }

    public void setLocked(boolean locked) {
	this.locked = locked;
    }

    public boolean getSimulateCalls() {
	return simulateCalls;
    }

    public void setSimulateCalls(boolean simulateCalls) {
	this.simulateCalls = simulateCalls;
    }

    public String getPhoneNumber() {
	return phoneNumber;
    }

    public String getPassword() {
	return password;
    }

    public String getPhoneLocation() {
	return phoneLocation;
    }

    public double getZeroVolumeRadius() {
	return zeroVolumeRadius;
    }

    public double getFullVolumeRadius() {
	return fullVolumeRadius;
    }

    public boolean getKeepUnlocked() {
	return keepUnlocked;
    }

    public void setKeepUnlocked(boolean keepUnlocked) {
	this.keepUnlocked = keepUnlocked;
    }
	
    protected void addParentCell(ManagedReference parent) {
	IncomingCallHandler incomingCallHandler = IncomingCallHandler.getInstance();

        //vector3f translation = new Vector3f();                
        //getOriginWorld().get(translation);                

	//incomingCallHandler.addPhone(getCellID(), translation, phoneNumber,
	//    phoneLocation, zeroVolumeRadius, fullVolumeRadius);
    }

}
