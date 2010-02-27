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
package org.jdesktop.wonderland.modules.testcells.client.jme;

import org.jdesktop.wonderland.client.ClientPlugin;
import org.jdesktop.wonderland.client.jme.ClientContextJME;
import org.jdesktop.wonderland.client.login.LoginManager;

/**
 *
 * @author paulby
 */
public class ClientInit implements ClientPlugin {

    public void initialize(LoginManager loginManager) {
        System.out.println("------------> HERE <---------------------");
        ClientContextJME.getEnvironmentManager().addEnvironment("Default", new DefaultEnvironment());
        ClientContextJME.getEnvironmentManager().setCurrentEnvironment("Default");
    }

}
