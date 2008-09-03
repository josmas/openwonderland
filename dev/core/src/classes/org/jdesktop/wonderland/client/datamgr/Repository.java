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
package org.jdesktop.wonderland.client.datamgr;

import java.net.URL;
import org.jdesktop.wonderland.common.ExperimentalAPI;

/**
 * Class representing the Repository(s) that serve an asset
 * @author paulby
 */
@ExperimentalAPI
public class Repository {
    
    private URL originalRepository;
    
    public Repository(URL originalRepository) {
        this.originalRepository = originalRepository;
    }

    /**
     * Return original site for asset
     * 
     * @return
     */
    public URL getOriginalRepository() {
        return originalRepository;
    }

    /**
     * Return all known repositories that host this content
     * @return
     */
    //        public URL[] getAllRepositories() {
    //            throw new RuntimeException("Not Implemented");            
    //        }

    /**
     * Get the repository we prefer to download from
     * 
     * TODO - implement round robin, intranet etc features so
     * system admins can control how the prefered server is defined
     * @return
     */
    public URL getPreferedRepository() {
        return originalRepository;          
    }
}
