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
 * The main logic for a SAS provider client.
 *
 * @author deronj
 */

@ExperimentalAPI
abstract public class ProviderClient {

    protected WonderlandServer session;
    protected ProviderServerConnection serverConnection;

    protected boolean initConnection (ServerInfo serverInfo, LoginParameters loginParams) {
	
	// TODO: manage session lifecycle

	// TODO: Establish connection - verify this
	session = new WonderlandSessionImpl(serverInfo);

        try {
	    session.login(loginParams);
        } catch (LoginFailureException ex) {
            logger.log(Level.SEVERE, "Login Failure", ex);
	    return false;
        }


	serverConnection = new ProviderServerConnection(session.getID());
        session.addConnection(serverConnection);
	// TODO: Create cell container and add to session

	return true;
    }

    protected void disconnect () {
	session.logout();
    }

    protected boolean initApps (String configFileName) {
	// TODO: read configuration file into AppList
        // TODO: define config file format
    }

}
