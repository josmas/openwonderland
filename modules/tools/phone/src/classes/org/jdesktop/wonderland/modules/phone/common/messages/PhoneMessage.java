/**
 * Project Looking Glass
 * 
 * $RCSfile: PhoneMessage.java,v $
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
 * $Revision: 1.8 $
 * $Date: 2008/06/12 18:48:17 $
 * $State: Exp $ 
 */

package org.jdesktop.wonderland.modules.phone.common.messages;

import org.jdesktop.wonderland.modules.phone.common.CallListing;

import org.jdesktop.wonderland.modules.phone.common.messages.PhoneCellMessage.PhoneAction;

import org.jdesktop.wonderland.common.messages.Message;


/*
 *Author: JHarris
 */

public class PhoneMessage extends Message {
    
   // private String listingID; 
    private PhoneAction action = null;        
    private boolean wasSuccessful;
    
    //For use in updating multi-client callListings
    private String contactName;
    private String contactNumber;
    private String privateClientName;
    private String reasonCallEnded = "";
    
    private String password;
    private boolean locked;
    private boolean demoMode;
    private String callID;

    //Use with P2P messaging
    public PhoneMessage(PhoneAction action) {   
        this.action = action;
        wasSuccessful = true;
    }
    
    //Use with PhoneAction.UPDATE_LISTING
    public PhoneMessage(PhoneAction action, CallListing listing, boolean wasSuccessful) {
        
     //   this.listingID = listing.getListingID();
        this.action = action;
        this.wasSuccessful = wasSuccessful;
        
        contactName = listing.getContactName();
        contactNumber = listing.getContactNumber();
        privateClientName = listing.getPrivateClientName();
	demoMode = listing.demoMode();
	callID = listing.getCallID();
    }

    public PhoneMessage(String password) {
	this.action = PhoneAction.LOCK_OR_UNLOCK;
	this.password = password;
    }

    public PhoneMessage(boolean locked, boolean wasSuccessful) {
	this.action = PhoneAction.LOCK_OR_UNLOCK;
	this.locked = locked;
	this.wasSuccessful = wasSuccessful;
    }

/*
    public String getListingID() {
        return listingID;
    }
  */  
    public PhoneAction getAction() {
        return action;
    }
    
    public boolean wasSuccessful() {
        return wasSuccessful;
    }
    
    public String getContactName() {
        return contactName;
    }
    
    public String getContactNumber() {
        return contactNumber;        
    }
    
    public String getPrivateClientName() {
        return privateClientName;                
    }
      
    public void setReasonCallEnded(String reasonCallEnded) {
	this.reasonCallEnded = reasonCallEnded;
    }

    public String getReasonCallEnded() {
	return reasonCallEnded;
    }

    public CallListing getCallListing() {
        
        CallListing callListing = new CallListing(contactName, contactNumber, privateClientName,
	    demoMode);
        
	callListing.setCallID(callID);

        return callListing;
    }

    public String getPassword() {
	return password;
    }

    public boolean getLocked() {
	return locked;
    }

    public String getCallID() {
	return callID;
    }

    public void setCallID(String callID) {
	this.callID = callID;
    }

}
