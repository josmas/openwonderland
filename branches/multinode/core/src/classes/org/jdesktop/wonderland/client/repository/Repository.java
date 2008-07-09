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

import org.jdesktop.wonderland.common.AssetURI;
import org.jdesktop.wonderland.common.InternalAPI;

/**
 * The Repository interface represents content repository(s) that server an asset.
 * A repository consists of one or more 'base' URLs from which the asset is
 * stored.
 *
 * @author paulby
 * @author Jordan Slott <jslott@dev.java.net>
 */
@InternalAPI
public interface Repository {
    
    /**
     * Returns the base URL of the repository we prefer to download from.
     * 
     * @return The repository that is preferred to download from
     */
    public String getPreferedBaseURL();
    
    /**
     * Returns the full URL used to download the asset from the repository
     * that we prefer to download from.
     * 
     * @param assetURI The uri of the asset to download
     * @return The full URL of the asset download
     */
    public String getPreferedURL(AssetURI assetURI);
    
    /**
     * Returns an array of base URLs of all known servers that host the content.
     * 
     * @return An array of base repository URLs
     */
     public String[] getAllBaseURLs();
     
     /**
      * Returns an array of full URLs used to download the asset from the
      * repository that host the content for the given asset uri. The order of
      * the list is determined by some policy.
      * 
      * @param assetURI The uri of the asset to download
      * @return The full URLs of the asset download
      */
     public String[] getAllURLs(AssetURI assetURI);
}
