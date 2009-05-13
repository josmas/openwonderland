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
package org.jdesktop.wonderland.modules.audiomanager.common.messages;

/*
 *
 * @author: jprovino
 */
public class CallEndedResponseMessage extends VoiceChatMessage {
    
    private String externalCallID;
    private String reasonCallEnded;

    public CallEndedResponseMessage(String group, String externalCallID, String reasonCallEnded) {
	super(group);

	this.externalCallID = externalCallID;
	this.reasonCallEnded = reasonCallEnded;
    }

    public String getExternalCallID() {
	return externalCallID;
    }

    public String getReasonCallEnded() {
	return reasonCallEnded;
    }

}
