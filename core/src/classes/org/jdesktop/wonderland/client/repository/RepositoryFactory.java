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
 * $Revision: 1.15 $
 * $Date: 2007/08/07 17:01:12 $
 * $State: Exp $
 */
package org.jdesktop.wonderland.client.repository;

import org.jdesktop.wonderland.common.AssetURI;
import org.jdesktop.wonderland.common.ExperimentalAPI;

/**
 * The Repository factory class generates the proper repository given the type
 * of asset uri given to it. For example, if the asset uri is a relative url,
 * then the repository is the default repository set for the Wonderland server.
 * If the asset uri refers to an asset contained within a module, then the
 * repository generated represents a round-robin of asset servers to try.
 * <p>
 * If the information about the repository is contained within the definition
 * of a module, then this class interfaces with the client-side module system
 * 
 * @author Jordan Slott <jslott@dev.java.net>
 */
@ExperimentalAPI
public class RepositoryFactory {
    /**
     * Returns an appropriate repository given a unique uri for the asset.
     * 
     * @param assetURI The unique uri of the asset
     * @return The repository to find the asset
     */
    public static final Repository getRepository(AssetURI assetURI) {
        /*
         * If the asset uri is relative, then return a repository that reflects
         * assets from the default repository set for the entire server.
         */
        if (assetURI.isRelative() == true) {
            return SystemDefaultRepository.getSystemDefaultRepository();
        }
        else if (assetURI.isDefinite() == true) {
            /*
             * If the asset uri is definite, meaning it is a well-formed URL
             * with a 'http', 'file', or 'ftp' scheme, then return a repository
             * that uses the hostname encoded in the URI.
             */
            return DefiniteRepository.getDefiniteRepository();
        }
        else if (assetURI.isModule() == true) {
            /*
             * If the asset uri refers to a module, first find the name of the
             * module encoded in the uri, see if the module exists, and construct
             * a repository that includes all possible servers for the assets.
             */
            String moduleName = assetURI.getModuleName();
            if (moduleName == null) {
                // XXX log an error
                return null;
            }
            
            // XXX Fetch module and get master and mirrors
            String master = "http://foo.net";
            String mirrors[] = { "http://192.18.37.42/compiled_models" };
            return new MasterMirrorRepository(master, mirrors);
        }
        
        // Log an error XXX
        return null;
    }
}
