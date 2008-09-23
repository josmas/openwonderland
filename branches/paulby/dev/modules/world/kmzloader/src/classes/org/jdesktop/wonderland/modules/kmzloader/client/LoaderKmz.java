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
package org.jdesktop.wonderland.modules.kmzloader.client;

import com.jme.scene.Node;
import java.io.File;
import java.lang.ref.SoftReference;
import org.jdesktop.wonderland.client.jme.artimport.ModelLoader;

/**
 *
 * @author paulby
 */
public class LoaderKmz implements ModelLoader {

    private SoftReference<LoaderKmzImpl> loaderRef = null;
    private boolean enabled = true;
    
    public String getFileExtension() {
        return "kmz";
    }

    public Node load(File file) {
        if (loaderRef==null || loaderRef.get()==null) {
            loaderRef = new SoftReference(new LoaderKmzImpl());
        }
        
        return loaderRef.get().load(file);        
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

}
