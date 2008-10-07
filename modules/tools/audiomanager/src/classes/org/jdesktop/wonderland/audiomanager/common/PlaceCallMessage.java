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
package org.jdesktop.wonderland.common.messages;

import org.jdesktop.wonderland.common.ExperimentalAPI;

/**
 * The initial message that a client must send to the Wonderland server
 * in order to specify a communications protocol to use.
 * @author jprovino
 */
@ExperimentalAPI
public class PlaceCallMessage extends Message {
    private String sipURL;	      // URL of softphone to call
    private boolean confirmAnswered;  // user has to press 1

    public PlaceCallMessage() {
    }

    public void setSipURL(String sipURL) {
	this.sipURL = sipURL;
    }

    public String getSipURL() {
	return sipURL;
    }

    public void setConfirmAnswered(boolean confirmAnswered) {	
	this.confirmAnswered = confirmAnswered;
    }

    public boolean getConfirmAnswered() {
	return confirmAnswered;
    }

}
