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
package org.jdesktop.wonderland.modules.audiomanager.common;

import org.jdesktop.wonderland.common.comms.ConnectionType;
import org.jdesktop.wonderland.common.messages.Message;

/**
 * The client type for audio managers
 * @author jkaplan
 */
public class AudioManagerConnectionType extends ConnectionType {
    /** The audio manager client type */
    public static final ConnectionType CONNECTION_TYPE =
            new AudioManagerConnectionType();
    
    /** Use the static CLIENT_TYPE, not this constructor */
    public AudioManagerConnectionType() {
        super ("__AudioManagerConnection");
    }

}
