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
 * Protocol version used by the Wonderland client and server
 * @author jkaplan
 */
public class WonderlandProtocolVersion extends DefaultProtocolVersion {
    public static final String PROTOCOL_NAME = "wonderland_client";
    public static final WonderlandProtocolVersion VERSION = new WonderlandProtocolVersion();
    
    private WonderlandProtocolVersion() {
        super (0, 1, 0);
    }
}
