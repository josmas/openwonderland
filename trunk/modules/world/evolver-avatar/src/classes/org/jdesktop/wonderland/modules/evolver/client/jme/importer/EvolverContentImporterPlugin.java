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
package org.jdesktop.wonderland.modules.evolver.client.jme.importer;

import org.jdesktop.wonderland.client.BaseClientPlugin;
import org.jdesktop.wonderland.client.content.ContentImportManager;
import org.jdesktop.wonderland.client.login.ServerSessionManager;
import org.jdesktop.wonderland.common.annotation.Plugin;

/**
 * Registers the Evolver avatar importer
 *
 * @author Jordan Slott <jslott@dev.java.net>
 */
@Plugin
public class EvolverContentImporterPlugin extends BaseClientPlugin {

    private EvolverContentImporter importer = null;

    @Override
    public void initialize(ServerSessionManager loginInfo) {
        importer = new EvolverContentImporter(loginInfo);
        super.initialize(loginInfo);
    }

    @Override
    public void activate() {
        ContentImportManager cim = ContentImportManager.getContentImportManager();
        cim.registerContentImporter(importer);
    } 
    
    @Override
    public void deactivate() {
        ContentImportManager cim = ContentImportManager.getContentImportManager();
        cim.unregisterContentImporter(importer);
    }
}
