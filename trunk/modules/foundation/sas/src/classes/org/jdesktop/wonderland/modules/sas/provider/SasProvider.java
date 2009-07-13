/**
 * Project Wonderland
 *
 * Copyright (c) 2004-2009, Sun Microsystems, Inc., All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * Sun designates this particular file as subject to the "Classpath"
 * exception as provided by Sun in the License file that accompanied
 * this code.
 */
package org.jdesktop.wonderland.modules.sas.provider;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.wonderland.client.comms.SessionStatusListener;
import org.jdesktop.wonderland.client.comms.WonderlandServerInfo;
import org.jdesktop.wonderland.client.comms.WonderlandSession;
import org.jdesktop.wonderland.client.login.ProgrammaticLogin;
import org.jdesktop.wonderland.client.login.ServerSessionManager;
import org.jdesktop.wonderland.common.ExperimentalAPI;

/**
 * The SAS provider client.
 *
 * @author deronj
 */
@ExperimentalAPI
public class SasProvider {
    private static final Logger logger =
            Logger.getLogger(SasProvider.class.getName());

    /** The execution site dependent listener for messages from the SAS server to provider. */
    private SasProviderConnectionListener listener;

    /**
     * Create a new instance of SasProvider, given login information.
     */
    public SasProvider (String userName, String fullName, String password, String serverUrl,
                        SasProviderConnectionListener listener) {

        this.listener = listener;

        // create a new programmatic login object
        SasLogin login = new SasLogin(serverUrl);

        // if the password isn't null, write it to a temporary file to use
        // during login
        File pwfile = null;
        if (password != null) {
            try {
                pwfile = File.createTempFile("pwfile", "out");
                PrintWriter out = new PrintWriter(new FileWriter(pwfile));
                out.println(password);
                out.close();
            } catch (IOException ioe) {
                // didn't work
                logger.log(Level.WARNING, "Error writing password", ioe);
                pwfile = null;
            }
        }

        SasProviderSession curSession;
        
        try {
            // log in to the session
            curSession = login.login(userName, pwfile);
        } finally {
            // make sure to delete the password file after login
            if (pwfile != null) {
                pwfile.delete();
            }
        }
        

        // make sure we logged in successfully
        if (curSession == null) {
            throw new RuntimeException("Unable to create session.");
        }

        curSession.addSessionStatusListener(new SessionStatusListener() {
                public void sessionStatusChanged(WonderlandSession session, WonderlandSession.Status status) {
                    if (status==WonderlandSession.Status.DISCONNECTED) {
                        // TODO: Do nothing for now
                    }
                }
            });
    }

    /**
     *  Provides the login information from the constructor to the login manager.
     */
    private class SasLogin extends ProgrammaticLogin<SasProviderSession> {
        public SasLogin(String serverURL) {
            super (serverURL);
        }

        @Override
        protected SasProviderSession createSession(ServerSessionManager sessionManager,
                                                   WonderlandServerInfo server,
                                                   ClassLoader loader)
        {
            return new SasProviderSession(sessionManager, server, loader, listener);
        }
    }
}
