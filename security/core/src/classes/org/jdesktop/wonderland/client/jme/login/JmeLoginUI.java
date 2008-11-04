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

import java.io.IOException;
import javax.swing.SwingUtilities;
import org.jdesktop.wonderland.client.cell.CellCache;
import org.jdesktop.wonderland.client.comms.CellClientSession;
import org.jdesktop.wonderland.client.comms.WonderlandServerInfo;
import org.jdesktop.wonderland.client.comms.WonderlandSession;
import org.jdesktop.wonderland.client.jme.JmeCellCache;
import org.jdesktop.wonderland.client.jme.MainFrame;
import org.jdesktop.wonderland.client.jme.login.WonderlandLoginDialog.LoginPanel;
import org.jdesktop.wonderland.client.login.LoginManager;
import org.jdesktop.wonderland.client.login.LoginManager.NoAuthLoginControl;
import org.jdesktop.wonderland.client.login.LoginManager.UserPasswordLoginControl;
import org.jdesktop.wonderland.client.login.LoginManager.WebURLLoginControl;
import org.jdesktop.wonderland.client.login.LoginUI;


/**
 * Manage the connection between this client and the wonderland server
 *
 * TODO RENAME, there must be a better name for this class !  LoginManager & JMELoginManager
 *
 * @author paulby
 */
public class JmeLoginUI implements LoginUI {
    private JmeCellCache cellCache = null;
    private String serverURL;
    private MainFrame parent;

    public JmeLoginUI(String serverURL, MainFrame parent) {
        this.serverURL = serverURL;
        this.parent = parent;

        LoginManager.setLoginUI(this);
    }

    public WonderlandSession doLogin() throws IOException {
        LoginManager lm = LoginManager.getInstance(serverURL);
        if (!lm.isAuthenicated()) {
            lm.authenticate();
        }

        return lm.getSession();
    }

    public void requestLogin(final NoAuthLoginControl control) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                LoginPanel lp = new NoAuthLoginPanel(control.getServerURL(),
                                                     control);
                WonderlandLoginDialog dialog = new WonderlandLoginDialog(
                                                              parent, true, lp);
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

    public WonderlandSession createSession(WonderlandServerInfo server,
                                           ClassLoader loader)
    {

        final ClassLoader finalLoader = loader;

        // create a session
        CellClientSession session = new CellClientSession(server, loader) {
            // createCellCache is called in the constructor fo CellClientSession
            // so the cellCache will be set before we proceed
            @Override
            protected CellCache createCellCache() {
                System.out.println("CREATING CELL CACHE");
                cellCache = new JmeCellCache(this, finalLoader);  // this session
                getCellCacheConnection().addListener(cellCache);
                return cellCache;
            }
        };

        return session;
    }

}
