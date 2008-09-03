package org.jdesktop.wonderland.webserver;

import org.jdesktop.wonderland.webserver.launcher.WebUtil;
import com.sun.enterprise.v3.server.ServerEnvironmentImpl;
import java.io.File;
import java.util.logging.Logger;

/**
 * Workaround to put server files in directory of our choice
 * @author jkaplan
 */
public class DirServerEnvironment extends ServerEnvironmentImpl {
    private static final Logger logger =
            Logger.getLogger(DirServerEnvironment.class.getName());
    
    private static File dir;
    
    public DirServerEnvironment() {
        super (WebUtil.createTempDir("wlweb", ".tmp"));
        
        logger.warning("Created DirServerEnvironment");
    }
}
