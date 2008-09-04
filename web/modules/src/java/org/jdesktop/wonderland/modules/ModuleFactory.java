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

package org.jdesktop.wonderland.modules;

import java.io.IOException;
import org.jdesktop.wonderland.modules.memory.MemoryModule;

/**
 * The ModuleFactory class creates instances of Module classes. Methods in this
 * class are the only way to create these instances.
 * <p>
 * All methods on this class are static.
 * <p>
 * @author Jordan Slott <jslott@dev.java.net>
 */
public class ModuleFactory {

    
    /**
     * Create a new module in memory.
     */
    public static final Module create() {
        return new MemoryModule();
    }
    
    public static final void main(String args[]) throws IOException {
//        ZipFile zipFile = new ZipFile("/Users/jordanslott/module/module-wlm.jar");
//        ModuleFactory.open(zipFile);
//        
//        File file = new File("/Users/jordanslott/module");
//        ModuleFactory.open(file);
    }
}
