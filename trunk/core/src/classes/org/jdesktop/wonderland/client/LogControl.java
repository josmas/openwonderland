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
package org.jdesktop.wonderland.client;

import java.util.logging.LogManager;
import org.jdesktop.wonderland.common.InternalAPI;
import java.io.InputStream;
import java.io.IOException;

/**
 * Read the log configuration from the jar
 * 
 * This class is used via the command line logging property
 *
 * @author paulby
 */
@InternalAPI
public class LogControl {
    
    /** Creates a new instance of LogControl */
    public LogControl() {
        LogManager logManager = LogManager.getLogManager();
        InputStream in = getClass().getResourceAsStream("resources/logging.properties");
        try {
            logManager.readConfiguration(in);
        } catch(IOException e) {
            e.printStackTrace();
        }
    }
    
}
