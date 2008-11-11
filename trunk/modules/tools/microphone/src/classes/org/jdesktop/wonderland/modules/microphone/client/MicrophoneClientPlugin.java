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
package org.jdesktop.wonderland.modules.microphone.client;

import java.util.logging.Logger;

import org.jdesktop.wonderland.client.ClientPlugin;

import org.jdesktop.wonderland.client.comms.ConnectionFailureException;
import org.jdesktop.wonderland.client.comms.SessionStatusListener;
import org.jdesktop.wonderland.client.comms.WonderlandSession;

import org.jdesktop.wonderland.client.login.LoginManager;
import org.jdesktop.wonderland.client.login.SessionLifecycleListener;

/**
 * Plugin to support the microphone
 * @author jprovino
 */
public class MicrophoneClientPlugin implements ClientPlugin,
	SessionLifecycleListener, SessionStatusListener {

    private static final Logger logger =
            Logger.getLogger(MicrophoneClientPlugin.class.getName());
    
    public void initialize(LoginManager manager) {
        manager.addLifecycleListener(this);
    }

    public void sessionCreated(WonderlandSession session) {
    }

    public void primarySession(WonderlandSession session) {
        session.addSessionStatusListener(this);

        logger.warning("Microphone initialized, session " + session);
    }
    
    public void sessionStatusChanged(WonderlandSession session, 
	    WonderlandSession.Status status) {

	logger.fine("session status changed " + session + " status " + status);
    }

}
