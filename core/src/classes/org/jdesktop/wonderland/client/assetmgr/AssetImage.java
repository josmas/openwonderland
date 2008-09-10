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
package org.jdesktop.wonderland.client.assetmgr;

import java.io.File;
import org.jdesktop.wonderland.common.AssetType;

/**
 *
 * @author paulby
 */
public class AssetImage extends Asset<File> {

    /**
     * @{inherit-javadoc}
     */
    public AssetImage(AssetID assetID) {
        super(assetID);
        type = AssetType.IMAGE;
    } 
    
    @Override
    public File getAsset() {
        return getLocalCacheFile();
    }

    @Override
    void postProcess() {
        // Don't need to process a File
    }

    @Override
    boolean loadLocal() {
        if (getLocalCacheFile()==null) {
            return false;
        }
        return getLocalCacheFile().canRead();
    }

    @Override
    void unloaded() {
        // Nothing to do for File
    }

}
