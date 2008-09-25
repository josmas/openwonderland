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
package org.jdesktop.wonderland.common.app.base;

import org.jdesktop.wonderland.common.comms.ConnectionType;
import org.jdesktop.wonderland.common.InternalAPI;

/**
 * The connection type the App Base uses to communicate with the Wonderland server about conventional cells.
 *
 * @author deronj
 */

@InternalAPI
public class AppConventionalConnectionType extends ConnectionType {

    /** The client type */
    public static final ConnectionType CLIENT_TYPE = new AppConventionalConnectionType("__AppBaseConventional");
    
    private AppConventionalConnectionType (String type) {
        super(type);
    }
}
