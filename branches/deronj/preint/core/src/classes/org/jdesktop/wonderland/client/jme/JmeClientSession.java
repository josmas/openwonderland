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

import org.jdesktop.wonderland.client.comms.CellClientSession;
import org.jdesktop.wonderland.client.comms.WonderlandServerInfo;

/**
 * An extension of CellClientSession to work with JME.  Primarily, this
 * uses a JmeCellCache instead of a regular cell cache
 * @author jkaplan
 */
public class JmeClientSession extends CellClientSession {
    private JmeCellCache jmeCellCache;
    
    public JmeClientSession(WonderlandServerInfo serverInfo,
            ClassLoader loader) {
        super(serverInfo, loader);
    }

    @Override
    public JmeCellCache getCellCache() {
        return jmeCellCache;
    }

    // createCellCache is called in the constructor fo CellClientSession
    // so the cellCache will be set before we proceed
    @Override
    protected JmeCellCache createCellCache() {
        System.out.println("CREATING CELL CACHE");
        jmeCellCache = new JmeCellCache(this, getClassLoader());
        getCellCacheConnection().addListener(jmeCellCache);
        return jmeCellCache;
    }
}
