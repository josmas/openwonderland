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
 * The server side handler for communications between a Wonderland user client and the SAS server.
 *
 * Messages sent to the user client: ADD_EXECUTION_CAPABILITY, REMOVE_EXECUTION_CAPABILITY.
 *
 * Messages received from the user client: LAUNCH.
 *
 * @author deronj
 */

@ExperimentalAPI
class UserServerClientConnectionHandler {

    /**
     * Tell the user client that there is a provider who is willing to provide the given execution capability.
     * @param The name of the execution capability to support.
     * @param apps A specific set of apps to support. If null, the execution capability supports all possible apps.
     * that the execution capability supports.
     */
    void addExecutionCapability (String executionCapability, AppCollection apps) {
	// TODO: send(new AddExecutionCapabilityMessage(executionCapability, apps))
    }

    /**
     * Tell the user client that the given execution capability (or certain apps for it) are no longer supported.
     * @param The name of the execution capability for which to remove support.
     * @param apps A specific set of apps to stop supportng. If null, the SAS server is saying that it 
     * will no longer support any apps for the execution capability.
     */
    void removeExecutionCapability (String executionCapability, AppCollection apps) {
	// TODO: send(new AddExecutionCapabilityMessage(executionCapability, apps))
    }

    void messageReceived (WonderlandClientSender sender, ClientSession session, Message message) {
	if (message instanceof SasUserLaunchMessage) {
	    SasUserLaunchMessage msg = (SasUserLaunchMessage) message;
	    Distributor.getDistributor().tryLaunch(msg.getUserClientID, sender, msg.getSequenceNumber(),
						   msg.getExecutionCapability(), msg.getAppName(), 
						   msg.getLaunchInfo(), null);
	} else {
	    logger.warning("Invalid SAS message received from user client: " + message);
	}
    }
}
