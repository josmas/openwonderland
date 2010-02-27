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
package org.jdesktop.wonderland.client.login;

import org.jdesktop.wonderland.client.comms.WonderlandSession;
import org.jdesktop.wonderland.common.ExperimentalAPI;

/**
 * Listen for new Wonderland sessions being created.
 * @author kaplanj
 */
@ExperimentalAPI
public interface SessionLifecycleListener {
    /**
     * Called when a new WonderlandSession is created.  When this method is
     * called, the session has been initialized and logged in, so is in
     * the CONNECTED state.
     * @param session the session that was created
     */
    public void sessionCreated(WonderlandSession session);

    /**
     * Called when a primary session is set.
     * @param session the session that was declared the primary session
     */
    public void primarySession(WonderlandSession session);
}
