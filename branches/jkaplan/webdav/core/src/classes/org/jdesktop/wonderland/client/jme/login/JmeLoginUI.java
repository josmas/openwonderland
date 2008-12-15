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
package org.jdesktop.wonderland.client.jme.login;

import javax.swing.SwingUtilities;
import org.jdesktop.wonderland.client.comms.LoginFailureException;
import org.jdesktop.wonderland.client.comms.WonderlandServerInfo;
import org.jdesktop.wonderland.client.jme.JmeClientSession;
import org.jdesktop.wonderland.client.jme.MainFrame;
import org.jdesktop.wonderland.client.jme.login.WonderlandLoginDialog.LoginPanel;
import org.jdesktop.wonderland.client.login.ServerSessionManager.NoAuthLoginControl;
import org.jdesktop.wonderland.client.login.ServerSessionManager.UserPasswordLoginControl;
import org.jdesktop.wonderland.client.login.ServerSessionManager.WebURLLoginControl;
import org.jdesktop.wonderland.client.login.LoginUI;
import org.jdesktop.wonderland.client.login.SessionCreator;


/**
 * Manage the connection between this client and the wonderland server
 *
 * TODO RENAME, there must be a better name for this class !  LoginManager & JMELoginManager
 *
 * @author paulby
 */
public class JmeLoginUI implements LoginUI, SessionCreator<JmeClientSession> {
    private MainFrame parent;

    public JmeLoginUI(MainFrame parent) {
        this.parent = parent;
    }

    public void requestLogin(final NoAuthLoginControl control) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                // see if we have properties for automatic login
                String username = System.getProperty("auth.username");
                String fullname = System.getProperty("auth.fullname");
                if (username != null && fullname != null) {
                    try {
                        control.authenticate(username, fullname);
                        return;
                    } catch (LoginFailureException lfe) {
                        // error trying to login in.  Fall back to 
                        // showing a dialog
                    }
                }

                LoginPanel lp = new NoAuthLoginPanel(control.getServerURL(),
                                                     control);
                WonderlandLoginDialog dialog = new WonderlandLoginDialog(
                                                   parent.getFrame(), true, lp);
                dialog.setLocationRelativeTo(parent.getFrame());
                dialog.setVisible(true);
            }
        });
    }

    public void requestLogin(UserPasswordLoginControl control) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void requestLogin(WebURLLoginControl control) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public JmeClientSession createSession(WonderlandServerInfo server,
                                          ClassLoader loader)
    {
        return new JmeClientSession(server, loader);
    }
}
