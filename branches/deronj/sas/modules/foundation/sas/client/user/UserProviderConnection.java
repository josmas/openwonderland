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
 * The user side handler for communications between a SAS user client and a SAS provider client.
 *
 * Messages sent by the user client to the provider client: START.
 *
 * Messages received from the provider client: OUTPUT, EXIT_VALUE.
 *
 * When this connection is initialized it sends a START message to the provider to notify it that the 
 * client is ready to receive messages from it.
 *
 * @author deronj
 */

@ExperimentalAPI
class UserProviderConnection extends BaseConnection {

    /** The session ID of the provider client. */
    BigInteger providerClientID;

    /** The process reporter to which to report the received messages. */
    ProcessReporter reporter;

    /**
     * Create a new instance of UserProviderConnection.
     * @param providerClientID The session ID of the provider client.
     * @param reporter The process reporter to which to report the received messages.
     */
    UserProviderConnection (BigInteger providerClientID, ProcessReporter reporter) {
	this.providerClientID = providerClientID;
	this.reporter = reporter;

	// Send the start message to the provider. The provider will now start sending
	// us any available output or exit value messages from the SAS app.
	send(SasMessage.createStartMessage(providerClientID));
    }

    /**
     * {@inheritDoc}
     */
    void handleMessage (Message message) {
	if (message instanceof SasOutputMessage) {
	    reporter.output(((SasOutputMessage)message).getString());
	} else if (message instanceof SasExitValueMessage) {
	    reporter.exitValue(((SasExitValueMessage)message).getValue());
	} else {
	    logger.warning("Invalid message received from SAS provider " + providerClientID);
	}
    }
}
