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

package org.jdesktop.wonderland.webserver;

import java.io.File;
import java.io.IOException;
import org.jdesktop.wonderland.utils.RunUtil;

/**
 * An extension of the default app server startup to handle the remote
 * runners.  This web server only runs the minimal remote runner
 * webapp in addition to the basic runner code.
 * @author jkaplan
 */
public class RunRemoteRunner extends RunAppServer {
    public RunRemoteRunner() throws IOException {
        super();
    }

    @Override
    protected void setupProperties() {
        super.setupProperties();

        // disable web deployment
        System.setProperty(WebDeployer.WEBDEPLOY_DISABLE_PROP, "true");
    }

    @Override
    protected void deployWebApps() throws IOException {
        // ignore
    }

    @Override
    protected void writeDocumentRoot() throws IOException {
        File docDir = new File(RunUtil.getRunDir(), "docRoot");
        docDir.mkdirs();

        // no files
    }

    @Override
    protected void writeWebApps() throws IOException {
        // ignore
    }
}
