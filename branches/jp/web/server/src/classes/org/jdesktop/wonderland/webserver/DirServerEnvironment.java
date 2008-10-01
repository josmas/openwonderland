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
package org.jdesktop.wonderland.webserver;

import org.jdesktop.wonderland.utils.RunUtil;
import java.io.File;
import java.util.logging.Logger;
import org.glassfish.server.ServerEnvironmentImpl;

/**
 * Workaround to put server files in directory of our choice
 * @author jkaplan
 */
public class DirServerEnvironment extends ServerEnvironmentImpl {
    private static final Logger logger =
            Logger.getLogger(DirServerEnvironment.class.getName());
    
    private static File dir;
    
    public DirServerEnvironment() {
        super (RunUtil.createTempDir("wlweb", ".tmp"));
        
        logger.warning("Created DirServerEnvironment");
    }
}
