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
package org.jdesktop.wonderland.server.app.sas;

/**
 * The server-side connection handler for communications between a SAS provider client and the SAS server.
 *
 * Messages sent to the provider: LAUNCH.
 *
 * Messages received from the provider: ADD_EXECUTION_CAPABILITY, REMOVE_EXECUTION_CAPABILITY.
 *
 * @author deronj
 */

@ExperimentalAPI
class SasProviderServerClientConnectionHandler {

    private Registry registry = SasServer.getRegistry();

    void launch (WonderlandClientSender providerSender, String ExecutionCapability, String appName, 
		 String launchInfo) {
	// TODO: construct and send launch message
    }

    void messageReceived (WonderlandClientSender sender, ClientSession session, Message message) {
	if (addmessage) {
	    status = registry.addExecutionCapability(sender, message.getExecutionCapability(), message.getApps());
	    send status in response message;
	} else if (removemessage) {
	    status = registry.removeExecutionCapability(sender, message.getExecutionCapability(), message.getApps());
	    send status in response message;
	} else if (message instanceof SasLaunchStatusMessage) {
	    SasLaunchStatusMessage msg (SasLaunchStatusMessage) message;
	    if (msg.getStatus() == LaunchStatus.SUCCESS) {
		SasServer.getDistributor().launchSucceeded(msg.getProviderClientID(), msg.getSequenceNumber());
	    } else {
		// Launch refused or launch failed
		SasServer.getDistributor().launchFailed(msg.getProviderClientID(), msg.getSequenceNumber());
	    }
	} else {
	    send response: invalid message error
	}
    } 


}
