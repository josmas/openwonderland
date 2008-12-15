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
package org.jdesktop.wonderland.runner;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.wonderland.modules.Module;
import org.jdesktop.wonderland.modules.ModulePart;
import org.jdesktop.wonderland.modules.spi.ModuleDeployerSPI;

/**
 * Deploys runner .zips
 * @author jkaplan
 */
public class RunnerDeployer implements ModuleDeployerSPI {
    private static final Logger logger =
            Logger.getLogger(RunnerDeployer.class.getName());

    public String getName() {
        return "runner";
    }

    public String[] getTypes() {
        return new String[] { "runner" };
    }

    public boolean isDeployable(String type, Module module, ModulePart part) {
        // always deployable
        return true;
    }

    public boolean isUndeployable(String type, Module module, ModulePart part) {
        // always undeployable
        return true;
    }

    public void deploy(String type, Module module, ModulePart part) {
        File[] files = part.getFile().listFiles();
        if (files == null) {
            return;
        }

        for (File file : files) {
            try {
                RunManager.getInstance().addRunnerZip(file.getName(), file);
            } catch (IOException ioe) {
                logger.log(Level.WARNING, "Unable to deploy " + file, ioe);
            }
        }
    }

    public void undeploy(String type, Module module, ModulePart part) {
        File[] files = part.getFile().listFiles();
        if (files == null) {
            return;
        }

        for (File file : files) {
            try {
                RunManager.getInstance().removeRunnerZip(file.getName());
            } catch (IOException ioe) {
                logger.log(Level.WARNING, "Unable to undeploy " + file, ioe);
            }
        }
    }
}
