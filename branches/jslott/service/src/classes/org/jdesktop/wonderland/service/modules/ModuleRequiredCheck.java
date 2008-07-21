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

/**
 * TBD
 * 
 * @author Jordan Slott <jslott@dev.java.net>
 */
public class ModuleRequiredCheck {
    /* The module name we are current checking the dependencies for */
    private RemovedModule module = null;
    
    
    /** Constructor that takes module to check */
    public ModuleRequiredCheck(RemovedModule module) {
        this.module = module;
    }
    
    /**
     * Checks whether the module is required in the system. This method is
     * implemented in the most simple way possible: iterate over all of the
     * installed or pending for installation modules and see if they require
     * the given module.
     * 
     * @return True if the module is still required by the system
     */
    public boolean checkRequired() {
        String uniqueName = this.module.getModuleInfo().getName();
        ModuleManager mm = ModuleManager.getModuleManager();
        
        /* See if this module is required */
        if (mm.isModuleRequired(uniqueName) != null) {
            // print something to the log XXX
            return true;
        }
        return false;
    }
}
