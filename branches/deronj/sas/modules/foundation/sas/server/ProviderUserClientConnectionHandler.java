// TODO: is the cohandler really needed?
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
 * The server-side connection handler for communications from a SAS provider client to a SAS user client.
 * It simply forwards messages from the provider client to the user client.
 *
 * Messages received from the provider client and sent to user client: OUTPUT, EXIT_VALUE.
 *
 * @author deronj
 */

@ExperimentalAPI
class ProviderUserClientConnectionHandler extends ClientConnectionHandler {

    /** The sender to the user client. */
    private WonderlandClientSender userClientSender;

    /** This handler's cohandler (the handler which sends from the user client to the provider client. */
    private UserProviderClientConnectionHandler cohandler;

    /**
     * Create a new instance of ProviderUserClientConnectionHandler.
     * @param userClientSender The sender to the user client.
     */
    ProviderUserClientConnectionHandler (WonderlandClientSender userClientSender) {
	this.userClientSender = userClientSender;
    }

    void setCohandler (UserProviderClientConnectionHandler cohandler) {
	this.cohandler = cohandler;
    }

    public void handleMessage (Message message) {
	// Forward the message
	userClientSender.send(message);
    }
}
