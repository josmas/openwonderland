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

package org.jdesktop.wonderland.modules.sample.client;

import org.jdesktop.wonderland.client.ClientPlugin;
import org.jdesktop.wonderland.client.cell.registry.CellRegistry;
import org.jdesktop.wonderland.client.login.ServerSessionManager;

/**
 *
 * @author jordanslott
 */
public class SampleClientPlugin implements ClientPlugin {

    public void initialize(ServerSessionManager loginInfo) {
        CellRegistry.getCellRegistry().registerCellFactory(new SampleCellFactory());
    }

}
