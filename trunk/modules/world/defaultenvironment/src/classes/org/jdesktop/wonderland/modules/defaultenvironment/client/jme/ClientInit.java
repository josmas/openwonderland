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
package org.jdesktop.wonderland.modules.defaultenvironment.client.jme;

import org.jdesktop.wonderland.client.ClientPlugin;
import org.jdesktop.wonderland.client.jme.ClientContextJME;
import org.jdesktop.wonderland.client.login.LoginManager;
import org.jdesktop.wonderland.client.login.PrimaryServerListener;
import org.jdesktop.wonderland.client.login.ServerSessionManager;

/**
 *
 * @author paulby
 */
public class ClientInit implements ClientPlugin, PrimaryServerListener {
    private ServerSessionManager sessionManager;

    public void initialize(ServerSessionManager sessionManager) {
        this.sessionManager = sessionManager;

        LoginManager.addPrimaryServerListener(this);
        if (LoginManager.getPrimary() != null &&
                LoginManager.getPrimary().equals(sessionManager))
        {
            enable();
        }
    }

    public void enable() {
        ClientContextJME.getEnvironmentManager().addEnvironment(sessionManager, "Default",
                                                                new DefaultEnvironment(sessionManager));
        ClientContextJME.getEnvironmentManager().setCurrentEnvironment(sessionManager, "Default");
    }

    public void disable() {
        ClientContextJME.getEnvironmentManager().removeEnvironment(sessionManager, "Default");
    }

    public void primaryServer(ServerSessionManager server) {
        if (sessionManager.equals(server)) {
            enable();
        } else {
            disable();
        }
    }
}
