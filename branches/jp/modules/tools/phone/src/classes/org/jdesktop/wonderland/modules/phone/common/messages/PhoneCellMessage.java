/**
 * Project Looking Glass
 * 
 * $RCSfile: PhoneCellMessage.java,v $
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
 * $Revision: 1.9 $
 * $Date: 2008/06/12 18:48:16 $
 * $State: Exp $ 
 */
package org.jdesktop.wonderland.modules.phone.common.messages;

import org.jdesktop.wonderland.modules.phone.common.CallListing;

import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.messages.Message;

/**
 *
 * @author jkaplan
 */
public class PhoneCellMessage extends Message {   
    
    public enum PhoneAction { 
        START_HOVER, 
        STOP_HOVER, 
        SELECT, 
        PLACE_CALL,    
	PLAY_TREATMENT,
        JOIN_CALL,
        END_CALL,
        NULL,
        LOCK_OR_UNLOCK,
        CALL_INVITED,
        CALL_ESTABLISHED,
        CALL_ENDED
    };
    
    private PhoneAction action;   

    private CellID clientCellID;
    private CellID phoneCellID;
    
 //   private String listingID;    
    private String contactName;
    private String contactNumber;
    private String privateClientName;
   
    private String treatment;
    private boolean echo;

    private String password;
    private boolean keepUnlocked;

    private boolean demoMode;
    private String callID;

    public PhoneCellMessage(CellID clientCellID, CellID phoneCellID, 
	    CallListing listing, String treatment, boolean echo) {

	this(PhoneAction.PLAY_TREATMENT, clientCellID, phoneCellID, listing);
	this.treatment = treatment;
	this.echo = echo;
    }

    public PhoneCellMessage(PhoneAction action, CellID clientCellID, 
	    CellID phoneCellID, CallListing listing) {

        this.action = action;  

	this.clientCellID = clientCellID;
	this.phoneCellID = phoneCellID;
        
        contactName = listing.getContactName();
        contactNumber = listing.getContactNumber();
        privateClientName = listing.getPrivateClientName();
	demoMode = listing.demoMode();

	callID = listing.getCallID();
    }
    
    public PhoneCellMessage(CellID cellID, String password, boolean keepUnlocked) {
	action = PhoneAction.LOCK_OR_UNLOCK;
	this.password = password;
	this.keepUnlocked = keepUnlocked;
    }

    public CallListing getCallListing() {
	CallListing callListing = new CallListing(contactName, contactNumber, 
	    privateClientName, demoMode);

	callListing.setCallID(callID);

	return callListing;
    }

    public PhoneAction getAction() {
        return action;
    }  

    public CellID getClientCellID() {
	return clientCellID;
    }

    public CellID getPhoneCellID() {
	return phoneCellID;
    }

    public String getContactName() {
        return contactName;
    }
    
    public String getContactNumber() {
        return contactNumber;
    }
       
    public String getPrivateClientName() {
        return this.privateClientName;
    }
    
    public String getTreatment() {
	return treatment;
    }

    public boolean echo() {
	return echo;
    }

    public String getPassword() {
	return password;
    }

    public boolean keepUnlocked() {
	return keepUnlocked;
    }

    public String getCallID() {
	return callID;
    }

    public void setCallID(String callID) {
	this.callID = callID;
    }

}
