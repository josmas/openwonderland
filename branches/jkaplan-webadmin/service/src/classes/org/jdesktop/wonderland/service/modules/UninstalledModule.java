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

package org.jdesktop.wonderland.service.modules;

import java.io.File;
import org.jdesktop.wonderland.modules.ModuleInfo;

/**
 * TBD
 * 
 * @author Jordan Slott <jslott@dev.java.net>
 */
public class UninstalledModule {

    private File moduleXml = null;
    private ModuleInfo moduleInfo = null;
    
    public UninstalledModule(File moduleXml, ModuleInfo moduleInfo) {
        this.moduleXml = moduleXml;
        this.moduleInfo = moduleInfo;
    }

    /**
     * TBD
     */
    public File getModuleXML() {
        return this.moduleXml;
    }
    
    /**
     * TBD
     */
    public ModuleInfo getModuleInfo() {
        return this.moduleInfo;
    }
}
