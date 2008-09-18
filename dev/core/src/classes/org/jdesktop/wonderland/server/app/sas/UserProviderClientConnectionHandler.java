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
 * The server-side connection handler for communications from a SAS user client to a SAS provider client.
 * It simply forwards messages from the user client to the provider client.
 *
 * Messages received from the user client and sent to provider client: START.
 *
 * @author deronj
 */

@ExperimentalAPI
class UserProviderClientConnectionHandler extends ClientConnectionHandler {

    /** The sender to the provier client. */
    private WonderlandClientSender providerClientSender;

    ProviderUserClientConnectionHandler (WonderlandClientSender providerClientSender) {
	this.providerClientSender = providerClientSender;
    }

    public void handleMessage (Message message) {
	// Forward the message
	providerClientSender.send(message);
    }
}
