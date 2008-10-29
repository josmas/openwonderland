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
package org.jdesktop.wonderland.client.jme;

import org.jdesktop.wonderland.client.LoginHelper;
import org.jdesktop.wonderland.client.cell.CellCache;
import org.jdesktop.wonderland.client.comms.CellClientSession;
import org.jdesktop.wonderland.client.comms.WonderlandServerInfo;
import org.jdesktop.wonderland.client.comms.WonderlandSession;


/**
 * Manage the connection between this client and the wonderland server
 * 
 * TODO RENAME, there must be a better name for this class !  LoginManager & JMELoginManager
 * 
 * @author paulby
 */
public class ClientManager extends LoginHelper {
    private JmeCellCache cellCache = null;
    
    public ClientManager(String serverName, int serverPort, String userName) {
        super(serverName, serverPort, userName);
    }
    
    public WonderlandSession createSession(WonderlandServerInfo server, ClassLoader loader) {

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
