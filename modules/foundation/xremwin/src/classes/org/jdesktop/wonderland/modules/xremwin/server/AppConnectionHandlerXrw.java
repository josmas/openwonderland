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
package org.jdesktop.wonderland.modules.xremwin.server;

import org.jdesktop.wonderland.common.InternalAPI;
import org.jdesktop.wonderland.modules.appbase.common.AppConventionalCellCreateMessage;
import org.jdesktop.wonderland.modules.appbase.server.AppConventionalCellMO;
import org.jdesktop.wonderland.modules.appbase.server.AppConventionalConnectionHandler;

/**
 * Handler for Xremwin the app conventional connection.
 *
 * @author deronj
 */

@InternalAPI
class AppConnectionHandlerXrw extends AppConventionalConnectionHandler {

    /**
     * {@inheritDoc}
     */
    public AppConventionalCellMO createCell (AppConventionalCellCreateMessage msg) {
	return new AppCellMOXrw(msg);
    }
}
