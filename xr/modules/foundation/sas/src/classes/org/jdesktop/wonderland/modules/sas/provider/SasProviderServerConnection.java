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
package org.jdesktop.wonderland.client.app.sas.provider;

/**
 * Handles communications between a SAS provider client and the SAS server.
 *
 * Messages sent to the server: ADD_EXECUTION_CAPABILITY, REMOVE_EXECUTION_CAPABILITY.
 *
 * Messages received from the server: LAUNCH.
 *
 * @author deronj
 */

@ExperimentalAPI
class SasProviderServerConnection extends BaseConnection {

    private static Logger logger = Logger.getLogger(SasProviderServerConnection.class.getName());

    public class LaunchListener {
	LaunchStatus launch (SasLaunchMessage message);
    }

    private BigInteger clientID;

    private HashMap<String,LaunchListener> listeners = new HashMap<String,LaunchListener>();

    SasProviderServerConnection (BigInteger clientID) {
	this.clientID = clientID;
    }

    void registerLaunchListener (String executionCapability, LaunchListener listener) {
	synchronized (listeners) {
	    listeners.put(executionCapability, listener);
	}
    }
    
    /**
     * Tell the server that this provider is willing to provide the given execution capability.
     * @param The name of the execution capability to support.
     * @param apps A specific set of apps to support. If null, the execution capability supports all possible apps.
     * that the execution capability supports.
     */
    boolean addExecutionCapability (String executionCapability, AppCollection apps) { 
	SasMessage msg = SasMessage.createAddExecutionCapabilityMessage(executionCapability, apps);
	ResponseMessage response = sendAndWait(msg);
	if (!(response instanceof OKMessage)) {
	    logger.severe("Cannot register apps with server");
	    return false;
	}
	return true;
    }

    /**
     * Tell the server that this provider is no longer willing to provide the given execution capability
     * and/or the apps for it.
     * @param The name of the execution capability for which to remove support.
     * @param apps A specific set of apps to stop supportng. If null, the provider is saying that it 
     * will no longer support any apps for the execution capability.
     */
    boolean removeExecutionCapability (String executionCapability, AppCollection apps) {
	SasMessage msg = SasMessage.createRemoveExecutionCapabilityMessage(executionCapability, apps);
	ResponseMessage response = sendAndWait(msg);
	if (!(response instanceof OKMessage)) {
	    logger.warning("Cannot unregister apps with server");
	    return false;
	}
	return true;
    }

    /**
     * {@inheritDoc}
     */
     @Override
     void handleMessage (Message message) {

	 if (message instanceof SasLaunchMessage) {
	     SasLaunchMessage launchMessage = (SasLaunchMessage) message;
	     int sequenceNumber = launchMessage.getSequenceNumber();

	     // Get the listener for this capability
	     LaunchListener listener;
	     synchronized (listeners) {
		 listener = listeners.get(launchMessage.getExecutionCapability);
	     }
	     if (listener == null) {
		 send(SasMessage.createLaunchStatusMessage(clientID, sequenceNumber, LaunchStatus.LaunchRefused));
		 return;
	     }

	     LaunchStatus status = listener.launch(launchMessage);
	     send(SasMessage.createLaunchStatusMessage(clientID, sequenceNumber, status));

	 } else if (message instanceof SasAbortMessage) {
	     // TODO: later

	 } else {
	     logger.warning("Invalid message received from server: " + message);
	 }

     }
}