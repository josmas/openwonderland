/**
 * Project Wonderland
 *
 * $RCSfile: LogControl.java,v $
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
 * $Revision: 1.3 $
 * $Date: 2007/10/23 18:27:41 $
 * $State: Exp $
 */
package org.jdesktop.wonderland.common.cell;

import org.jdesktop.wonderland.common.comms.ConnectionType;

/**
 * The ConnectionType of the CellCacheClient
 * @author jkaplan
 */
public class CellCacheConnectionType extends ConnectionType {
    /** the client type for the cell client */
    public static final ConnectionType CLIENT_TYPE =
            new CellCacheConnectionType("__CellCacheClient");
    
    private CellCacheConnectionType(String type) {
        super (type);
    }
}
