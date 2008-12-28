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

package org.jdesktop.wonderland.modules.coneofsilence.client.cell;

import org.jdesktop.wonderland.client.ClientPlugin;
import org.jdesktop.wonderland.client.cell.registry.CellRegistry;
import org.jdesktop.wonderland.client.login.ServerSessionManager;

/**
 * A client plugin that registers the sample cell with the registry.
 * 
 * @author Jordan Slott <jslott@dev.java.net>
 */
public class ConeOfSilenceClientPlugin implements ClientPlugin {

    public void initialize(ServerSessionManager loginInfo) {
        CellRegistry.getCellRegistry().registerCellFactory(new ConeOfSilenceCellFactory());
    }

}
