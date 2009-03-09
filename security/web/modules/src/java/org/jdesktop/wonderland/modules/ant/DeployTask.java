/**
 * Project Wonderland
 *
 * Copyright (c) 2004-2009, Sun Microsystems, Inc., All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * Sun designates this particular file as subject to the "Classpath" 
 * exception as provided by Sun in the License file that accompanied 
 * this code.
 */
package org.jdesktop.wonderland.modules.ant;

import org.jdesktop.wonderland.common.modules.ModuleUploader;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

/**
 * @author jkaplan
 */
public class DeployTask extends Task {
    private File module;
    private URL serverUrl;
    private boolean restart = true;
    
    // url for restarting the server
    private static final String RESTART_SERVER =
            "wonderland-web-runner/services/all/restart?wait=true";
    
    public void setModule(File module) {
        this.module = module;
    }
    
    public void setServerUrl(URL serverUrl) {
        this.serverUrl = serverUrl;
    }
    
    public void setRestart(boolean restart) {
        this.restart = restart;
    }
    
    @Override
    public void execute() throws BuildException {
        if (module == null) {
            throw new BuildException("module required");
        }
        if (serverUrl == null) {
            throw new BuildException("serverUrl required");
        }
        
        try {
            // upload module
            ModuleUploader mu = new ModuleUploader(serverUrl);
            mu.upload(module);
        } catch (IOException ioe) {
            throw new BuildException("Error uploading to " + serverUrl, ioe);
        }
    } 
}
