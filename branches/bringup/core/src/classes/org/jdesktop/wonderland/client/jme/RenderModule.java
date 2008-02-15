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
public abstract class RenderModule {

    private boolean active;
    
    /**
     * Initialise the module
     * @param info
     */
    public abstract void init(RenderInfo info);
    
    public void setActive(boolean active, RenderInfo info) {
        if (active==this.active)
            return;
        
        setActiveImpl(active, info);
        this.active = active;
    }
    
    public boolean isActive() {
        return active;
    }
    
    public abstract void setActiveImpl(boolean active, RenderInfo info);
    
    public abstract void update(RenderInfo info, float interpolation);
    
    public abstract void render(RenderInfo info, float interpolation);
}
