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
 * The provider side handler for communications between a SAS provider client and a SAS user client.
 *
 * Messages sent by the provider client to the user client: OUTPUT, EXIT_VALUE.
 *
 * Messages received from the user client: START.
 *
 * This connection doesn't send any messages to the user client until it first receives a START message
 * from the user client. Prior to that receiving this message this connection buffers up any output and
 * exit value messages. When the start command is received all buffered messages are sent to the user client.
 *
 * @author deronj
 */

@ExperimentalAPI
class ProviderUserConnection extends BaseConnection {

    BigInteger userClientID;

    private boolean started = false;

    private LinkedList<SasMessage> messageBuffer = new LinkedList<SasMessage>();

    ProviderUserConnection (BigInteger userClientID) {
	this.userClientID = userClientID;
    }

    void output (String str) {
	Message message = SasMessage.createOutputMessage(userClientID, str);
	if (started) {
	    send(message);
	} else {
	    messageBuffer.add(message);
	}
    }

    void exitValue (int exitValue) {
	Message message = SasMessage.createExitValueMessage(userClientID, exitValue);
	if (started) {
	    send(message);
	} else {
	    messageBuffer.add(message);
	}
    }

    public void handleMessage (Message message) {
	if (message instanceof SasStartMessage) {
	    if (!started) {
		for (Message msg : messageBuffer) {
		    send(msg);
		}
	    }
	    started = true;
	} else {
	    logger warning invalid message receiver from Sas user client;
	}
    }
}
