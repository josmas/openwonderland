/**
 * Project Wonderland
 *
 * $RCSfile:$
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
 * $Revision:$
 * $Date:$
 * $State:$
 */
package org.jdesktop.wonderland.common.comms;

/**
 * The client type used internally by WonderlandSession to send
 * messages
 * @author jkaplan
 */
public class SessionInternalHandlerType extends HandlerType {
    public static final HandlerType SESSION_INTERNAL_CLIENT_TYPE =
            new SessionInternalHandlerType();

    /** the id used by the session internal client */
    public static final short SESSION_INTERNAL_CLIENT_ID = -1;
    
    public SessionInternalHandlerType() {
        super ("__WonderlandSessionInternalClient");
    }
}
