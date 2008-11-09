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
package org.jdesktop.wonderland.modules.audiomanager.common.messages;

import org.jdesktop.wonderland.common.ExperimentalAPI;

import org.jdesktop.wonderland.common.messages.Message;

import org.jdesktop.wonderland.common.cell.CellID;

/**
 * Message to transfer a call to a different number.
 *
 * @author jprovino
 */
@ExperimentalAPI
public class TransferCallMessage extends Message {

    private String softphoneCallID;
    private String phoneNumber;

    public TransferCallMessage(String softphoneCallID, String phoneNumber) {
	this.softphoneCallID = softphoneCallID;
	this.phoneNumber = phoneNumber;
    }

    public String getSoftphoneCallID() {
        return softphoneCallID;
    }

    public String getPhoneNumber() {
	return phoneNumber;
    }

}
