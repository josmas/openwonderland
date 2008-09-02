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

import java.util.Arrays;
import java.util.List;
import org.jdesktop.wonderland.modules.Module;
import org.jdesktop.wonderland.modules.ModuleInfo;

/**
 * The ModuleDependencyCheck class checks whether the dependencies for a module
 * has been met. A valid dependency is defined as matching the module name and
 * having a version greater than or equal to the required version. For a module,
 * its dependencies have been checked if all of its required modules are either
 * installed and not about to be removed, or waiting to be installed.
 * 
 * @author Jordan Slott <jslott@dev.java.net>
 */
public class ModuleDependencyCheck {
    /* The module we are current checking the dependencies for */
    private Module module = null;
    
    /* A list of module requirements, removed as the dependencies are met. */
    private List<ModuleInfo> requirements = null;
    
    /** Constructor that takes module to check */
    public ModuleDependencyCheck(Module module) {
        this.module = module;
        this.requirements = Arrays.asList(module.getModuleRequires().getRequires());
    }
    
    /**
     * Checks the module's dependencies. This method is implemented in the most
     * simple way possible: iterate over the entire requirement set, and remove
     * a requirement when it has been met. If we iterate over the entire list
     * without having satisified an additional requirement, then we know the
     * requirements have not been met.
     * 
     * @return True if the module has all of its dependencies satisfied
     */
    public boolean checkDependencies() {
        String uniqueName = this.module.getModuleInfo().getName();
        ModuleManager mm = ModuleManager.getModuleManager();
        ModuleInfo[] requires = this.requirements.toArray(new ModuleInfo[] {});
        
        /*
         * Loop through and ask if the module is present. If so, then see if the
         * version is acceptable.
         */
        for (ModuleInfo moduleRequired : requires) {
            Module candidate = mm.isModulePresent(uniqueName);
            if (candidate != null) {
                /* Check to see if the version number has been satisfied */
                if (this.isSatisfied(candidate.getModuleInfo(), moduleRequired) == true) {
                    this.requirements.remove(moduleRequired);
                }
            }
        }

        
        /* Make a check to see if the set is empty, which determines success */
        return this.requirements.isEmpty();
    }

    /**
     * Takes two ModuleInfo class and checks whether the first (provider) is
     * satisfied as a requirement of the second (requirer). This method assumes
     * they already share the same unique module name and simply checks the
     * version. Returns true if the requirement is satisfied, false if not.
     * 
     * @param provider The module info that could satisfy the requirement
     * @param requirer The module info that specifies the requirement
     * @return True is the requirement is satisfied, false if not
     */
    private boolean isSatisfied(ModuleInfo provider, ModuleInfo requirer) {
        /*
         * First check if the requirer needs a version. If major is -1, then
         * there is no requirement and all satisfy it.
         */
        if (requirer.getMajor() == ModuleInfo.VERSION_UNSET) {
            return true;
        }
        
        /*
         * Next check to see if the required major version number is greater
         * than the provided one. If so, then return false
         */
        if (requirer.getMajor() > provider.getMajor()) {
            return false;
        }
        
        /*
         * Next, check to see if the required major version number is less
         * than the provided one. If so return true.
         */
        if (provider.getMajor() > requirer.getMajor()) {
            return true;
        }
        
        /*
         * At this point we know that there is a required version number and
         * that the two major version numbers are equal. Now check the minor
         * number. If the required minor number is unset, then any will do.
         */
        if (requirer.getMinor() == ModuleInfo.VERSION_UNSET) {
            return true;
        }
        
        /*
         * If the required minor number of less than or equal to what is
         * provided, then we are good, otherwise, not.
         */
        if (requirer.getMinor() <= provider.getMinor()) {
            return true;
        }
        return false;
    }
}
