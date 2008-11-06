/**
 * Project Wonderland
 *
 * $RCSfile:$
 *
 * Copyright (c) 2004-2007, Sun Microsystems, Inc., All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * $Revision:$
 * $Date:$
 * $State:$
 */
package org.jdesktop.wonderland.modules.orb.client;

import java.util.logging.Logger;

import org.jdesktop.wonderland.client.ClientPlugin;

import org.jdesktop.wonderland.client.comms.SessionStatusListener;
import org.jdesktop.wonderland.client.comms.WonderlandSession;

/**
 * Plugin to support the Orb
 * @author jprovino
 */
public class OrbClientPlugin implements ClientPlugin,
	SessionStatusListener {

    private static final Logger logger =
            Logger.getLogger(OrbClientPlugin.class.getName());
    
    public void initialize(WonderlandSession session) {
	session.addSessionStatusListener(this);

	logger.fine("Orb initialized, session " + session);
    }
    
    public void sessionStatusChanged(WonderlandSession session, 
	    WonderlandSession.Status status) {

	logger.fine("session status changed " + session + " status " + status);
    }

}
