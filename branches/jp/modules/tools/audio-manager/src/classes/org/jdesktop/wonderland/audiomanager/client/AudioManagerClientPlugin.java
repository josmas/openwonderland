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
package org.jdesktop.wonderland.audiomanager.client;

import java.util.logging.Logger;
import org.jdesktop.wonderland.client.ClientPlugin;
//import org.jdesktop.wonderland.client.WonderlandContext;
//import org.jdesktop.wonderland.client.comms.CommsManager;

/**
 * Pluging to support the audio manager
 * @author paulby
 */
public class AudioManagerClientPlugin implements ClientPlugin {
    private static final Logger logger =
            Logger.getLogger(AudioManagerClientPlugin.class.getName());
    
    public void initialize() {
        //CommsManager cm = WonderlandContext.getCommsManager();
        //cm.registerClientHandler(new AudioManagerConnectionHandler());
    }
    
}
