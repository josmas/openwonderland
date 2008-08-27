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

/**
 * The RepositorySet interface represents content repository(s) that server an
 * asset. A repository consists of a 'base' URL from which the asset is stored.
 * This list specifies a collection of individual repositories from which to
 * load an asset.
 * 
 * @author paulby
 * @author Jordan Slott <jslott@dev.java.net>
 */
@InternalAPI
public interface RepositoryList {
    /**
     * Returns the repository we prefer to download from.
     * 
     * @return The repository that is preferred to download from
     */
    public Repository getPreferedRepository();
    
    /**
     * Returns an array of all known repositories that host the content.
     * 
     * @return An array of repositories
     */
     public Repository[] getAllRepositories();
}
