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

package org.jdesktop.wonderland.web.asset.resources;

import java.io.File;
import java.util.Collection;
import java.util.LinkedList;
import java.util.logging.Logger;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import org.jdesktop.wonderland.common.modules.ModuleArt;
import org.jdesktop.wonderland.common.modules.ModuleArtList;
import org.jdesktop.wonderland.web.asset.deployer.AssetDeployer;

/**
 * The GetModuleAssetListResource class is a Jersey RESTful service that returns
 * a list of art that is contained within the module system.
 * 
 * @author Jordan Slott <jslott@dev.java.net>
 */
@Path(value="/{modulename}/art/get")
public class GetModuleAssetListResource {
    
    /**
     * TBD
     * 
     * @param moduleName The unique name of the module
     * @return An XML encoding of the module's basic information
     */
    @GET
    @Produces({"text/plain", "application/xml", "application/json"})
    public Response getModuleAsset(@PathParam("modulename") String moduleName) {
        Logger logger = Logger.getLogger(GetModuleAssetListResource.class.getName());

        /* Try to find the File root for the "art" type and module name */
        File root = AssetDeployer.getFile("art", moduleName);
        if (root == null) {
            return Response.ok(new ModuleArtList()).build();
        }
        
        /* List all of the artwork recursively */
        Collection<ModuleArt> moduleArt = this.listArt(root, root);
        ModuleArtList artList = new ModuleArtList(moduleArt.toArray(new ModuleArt[] {}));
        return Response.ok(artList).build();
    }
    
    /**
     * Returns a collection of artwork contained within the directory
     */
    private Collection<ModuleArt> listArt(File root, File dir) {
        Collection<ModuleArt> list = new LinkedList<ModuleArt>();
        
        /*
         * Loop through all of the files. If it is a directory, then recursively
         * descend into subdirectories. Before computing the checksum make sure
         * the file name satisfies the includes and excludes list.
         */
        File[] files = dir.listFiles();
        if (files == null) {
            /* No files exist, just return an empty map */
            return list;
        }
        for (File file : files) {
            /* If a directory, then recursively descend and append */
            if (file.isDirectory() == true && file.isHidden() == false) {
                Collection<ModuleArt> rList = this.listArt(root, file);
                list.addAll(rList);
            }
            else if (file.isFile() == true && file.isHidden() == false) {
                /*
                 * The relative path name is the absolute path name of the
                 * file, stripping off the absolute path name of the root.
                 * Ignore the checksums.xml file
                 */
                String name = file.getAbsolutePath().substring((int)(root.getAbsolutePath().length() + 1));
                if (name.equals("checksums.xml") == false) {
                    list.add(new ModuleArt(name));
                }
            }
        }
        return list;
    }
}
