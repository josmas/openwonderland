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
package org.jdesktop.wonderland.client.jme;

/**
 *
 * @author paulby
 */
public interface PluginAccessor {

    public void addModule(RenderModule module);

    /**
     * Set the desired fps rate
     */
    public void setFPS(int fps);
    
    /**
     * Returns the module manager
     * @return
     */
    public ModuleManager getModuleManager();
}
