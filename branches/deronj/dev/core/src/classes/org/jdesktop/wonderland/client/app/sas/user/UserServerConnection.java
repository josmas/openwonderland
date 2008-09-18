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
package org.jdesktop.wonderland.client.app.sas.user;

/**
 * The client side handler for communications between a Wonderland user client and the SAS server.
 *
 * Messages sent to the server: LAUNCH.
 *
 * Messages received from the server: ADD_EXECUTION_CAPABILITY, REMOVE_EXECUTION_CAPABILITY.
 *
 * @author deronj
 */

@ExperimentalAPI
class UserServerConnection extends BaseConnection {

    private int nextSequenceNumber = 0;

    private class LaunchAttemptState {
	User
	int sequenceNumber;
	String executionCapability;
	String appName;
	Serializable launchInfo;
	boolean timedOut;
    }

    private LinkedList<LaunchAttemptState> pendingLaunches = new LinkedList<LaunchAttemptState>();

    private SasUserClient sasUserClient;

    UserServerConnection (SasUserClient sasUserClient) {
	this.sasUserClient = sasUserClient;
    }

    void launch (String executionCapability, String appName, String launchInfo, 
			 StringBuffer launchStatusDetail) {

	

	LaunchAttemptState state = new LaunchAttemptState();

	state.sequenceNumber = nextSequenceNumber;
	if (nextSequenceNumber == Integer.MAX_VALUE) {
	    nextSequenceNumber = 0;
	} else {
	    nextSequenceNumber++;
	}
	state.executionCapability = executionCapability;
	state.appName = appName;
	state.launchInfo = launchInfo;
	state.timedOut = false;

	addLaunchAttemptState(state);

	startTimer(state);

	SasServerLaunchMessage message = SasMessage.createServerLaunchMessage(sasUserClient.getID(), 
					      nextSequenceNumber, executionCapability, appName, launchInfo);
	send(message);

	// The response will come asynchronously.
    }

    private void addLaunchAttemptState (LaunchAttemptState state) {
	synchronized (pendingLaunches) {
	    pendingLaunches.add(state);
	}
    }

    private void removeLaunchAttemptState (LaunchAttemptState state) {
	synchronized (pendingLaunches) {

	    LaunchAttemptState toDelete = null;
	    for (LaunchAttemptState las : pendingLaunches) {
		if (las.sequenceNumber == state.sequenceNumber) {
		    toDelete = las;
		    break;
		}
	    }
	    if (toDelete != null) {
		pendingLaunches.remove(toDelete);
	    }
	}
    }

    private LaunchAttemptState findLaunchAttemptState (int sequenceNumber) {
	synchronized (pendingLaunches) {
	    for (LaunchAttemptState state : pendingLaunches) {
		if (state.sequenceNumber == sequenceNumber) {
		    return state;
		}
	    }
	    
	    return null;
	}
    }

    private void startTimeoutTimer (LaunchAttemptState state) {
	// TODO
    }

    /**
     * {@inheritDoc}
     */
     @Override
     public void handleMessage (Message message) {
	 if (addmessage) {
	     userClient.addExecutionCapability(message.getExecutionCapability(), message.getApps());
	 } else if (removemessage) {
	     userClient.removeExecutionCapability(message.getExecutionCapability(), message.getApps());
	 } else if (message instanceof SasUserLaunchStatusMessage) {
	     SasUserLaunchStatusMessage	msg = (SasUserLaunchStatusMessage) message;
	     LaunchAttemptState state = findLaunchAttemptState(msg.getSequenceNumber());
	     if (state == null) {
		 logger.warning("UserLaunchStatus message: Cannot find launch state for sequence number " +
				msg.getSequenceNumber());
		     
		 return;
	     }
	     if (state.timedOut) {
	     } else {
		 sasUserClient.reportLaunchStatus(msg.getLaunchStatus(), msg.getLaunchStatusDetail(), 
						  state.executionCapability, state.appName, state.launchInfo)
		     }
	 } else {
	     logger warning invalid message from server;
	 }
    
}
