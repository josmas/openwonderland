/**
 * Project Wonderland
 *
 * Copyright (c) 2004-2009, Sun Microsystems, Inc., All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * Sun designates this particular file as subject to the "Classpath" 
 * exception as provided by Sun in the License file that accompanied 
 * this code.
 */
package org.jdesktop.wonderland.web.asset.resources;

import java.io.StringWriter;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import org.jdesktop.wonderland.common.modules.ModuleChecksums;
import org.jdesktop.wonderland.web.asset.deployer.AssetDeployer;
import org.jdesktop.wonderland.web.asset.deployer.AssetDeployer.DeployedAsset;

/**
 * The ModuleChecksumsResource class is a Jersey RESTful service that returns the
 * checksum information about all resources within a module given its name
 * encoded into a request URI. The getModuleChecksums() method handles the HTTP
 * GET request.
 * <p>
 * @author Jordan Slott <jslott@dev.java.net>
 */
@Path("/{modulename}/checksums/get")
public class ModuleChecksumsResource {
    
    @GET
    @Produces("text/plain")
    @Path("/{assettype}")
    public Response getAssetChecksums(@PathParam("modulename") String moduleName,
            @PathParam("assettype") String assetType) {

        /*
         * Get a map of all of the Checksum objects for each art asset. We see
         * if the module name matches each entry and collect its checksum
         * entries into a single map.
         */
        ModuleChecksums cks = new ModuleChecksums();
        Map<DeployedAsset, ModuleChecksums> checksumMap = AssetDeployer.getChecksumMap();
        Iterator<DeployedAsset> it = checksumMap.keySet().iterator();
        while (it.hasNext() == true) {
            DeployedAsset asset = it.next();
            if (asset.moduleName.equals(moduleName) == true) {
                if (assetType == null || asset.assetType.equals(assetType) == true) {
                    ModuleChecksums checksums = checksumMap.get(asset);
                    cks.putChecksums(checksums.getChecksums());
                }
            }
        }
        
        /* Write the XML encoding to a writer and return it */
        StringWriter sw = new StringWriter();
        try {
            cks.encode(sw);
            ResponseBuilder rb = Response.ok(sw.toString());
            return rb.build();
        } catch (javax.xml.bind.JAXBException excp) {
            /* Log an error and return an error response */
            Logger logger = Logger.getLogger(ModuleChecksumsResource.class.getName());
            logger.log(Level.WARNING, "[ASSET] Unable to encode checksums", excp);
            ResponseBuilder rb = Response.status(Response.Status.BAD_REQUEST);
            return rb.build();
        }
    }
    
    /**
     * Returns the checksums information about a module's resources, given its
     * module name encoded into the URI. The format of the URI is:
     * <p>
     * /module/{modulename}/checksums
     * <p>
     * where {modulename} is the name of the module. All spaces in the module
     * name must be encoded to %20. Returns BAD_REQUEST to the HTTP connection if
     * the module name is invalid or if there was an error encoding the module's
     * information.
     * 
     * @param moduleName The unique name of the module
     * @return An XML encoding of the module's basic information
     */
    @GET
    @Produces("text/plain")
    public Response getModuleChecksums(@PathParam("modulename") String moduleName) {
        return getAssetChecksums(moduleName, null);
    }
}
