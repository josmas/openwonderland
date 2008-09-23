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
package org.jdesktop.wonderland.client.jme.artimport;

import com.jme.scene.Node;
import java.io.File;

/**
 *
 * @author paulby
 */
public interface ModelLoader {

    /**
     * @return the file extensions supported by the loader
     */
    public String getFileExtension();
    
    /**
     * Load the specified model
     * 
     * @param file
     * @return
     */
    public Node load(File file);
    
    /**
     * 
     * @return true if this loader is enabled
     */
    public boolean isEnabled();

    public void setEnabled(boolean b);
}
