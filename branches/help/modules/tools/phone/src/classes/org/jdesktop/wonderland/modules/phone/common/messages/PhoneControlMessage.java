/**
 * Project Looking Glass
 * 
 * $RCSfile: PhoneControlMessage.java,v $
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
 * @author jprovino
 */
public class PhoneControlMessage extends Message {   
    
    private CellID clientCellID;
    private CellID externalCallCellID;
    private CallListing listing;    

    public PhoneControlMessage(CellID clientCellID, CellID externalCallCellID, 
	    CallListing listing) {

	this.clientCellID = clientCellID;
	this.externalCallCellID = externalCallCellID;
        this.listing = listing;
    }
    
    public CellID getClientCellID() {
	return clientCellID;
    }

    public CellID getExternalCallCellID() {
	return externalCallCellID;
    }

    public CallListing getCallListing() {
	return listing;
    }

}
