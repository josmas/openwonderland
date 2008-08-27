/**
 * Project Wonderland
 *
 * $RCSfile: AssetDB.java,v $
 *
 * Copyright (c) 2004-2007, Sun Microsystems, Inc., All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * $Revision: 1.15 $
 * $Date: 2007/08/07 17:01:12 $
 * $State: Exp $
 */
package org.jdesktop.wonderland.client.repository;

import org.jdesktop.wonderland.common.InternalAPI;
import org.jdesktop.wonderland.common.config.WonderlandConfig;

/**
 * The SystemDefaultRepository class represents content that comes from an asset
 * server defined by a system-wide property.
 *
 * @author Jordan Slott <jslott@dev.java.net>
 */
@InternalAPI
public class SystemDefaultRepositoryList implements RepositoryList {
    
    /* The system-wide repository  */
    private Repository repository = null;
    
    /** Default constructor */
    public SystemDefaultRepositoryList() {
        /* Fetch the system-wide URL and remove any trailing '/' */
        this.repository = new Repository(WonderlandConfig.getBaseURL());
    }
    
    /**
     * Factory method to return a new instance of SystemDefaultRepository
     * 
     * @return A new SystemDefaultRepository class
     */
    public static final SystemDefaultRepositoryList getSystemDefaultRepository() {
        return new SystemDefaultRepositoryList();
    }

    /**
     * Returns the repository we prefer to download from.
     * 
     * @return The repository that is preferred to download from
     */
    public Repository getPreferedRepository() {
        return this.repository;
    }
    
    /**
     * Returns an array of all known repositories that host the content.
     * 
     * @return An array of repositories
     */
     public Repository[] getAllRepositories() {
         return new Repository[] {
             this.repository
         };
     }
}
