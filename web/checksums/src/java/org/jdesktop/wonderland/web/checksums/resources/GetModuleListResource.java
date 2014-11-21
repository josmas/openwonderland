/**
 * Project Wonderland
 *
 * Copyright (c) 2004-2010, Sun Microsystems, Inc., All Rights Reserved
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
package org.jdesktop.wonderland.web.checksums.resources;

import java.io.File;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import org.jdesktop.wonderland.common.checksums.ChecksumList;
import org.jdesktop.wonderland.common.modules.ModuleInfo;
import org.jdesktop.wonderland.common.modules.ModuleList;
import org.jdesktop.wonderland.common.modules.ModuleRepository;
import org.jdesktop.wonderland.modules.Module;
import org.jdesktop.wonderland.modules.service.DeployManager;
import org.jdesktop.wonderland.modules.service.ModuleManager;
import org.jdesktop.wonderland.utils.Constants;
import org.jdesktop.wonderland.web.asset.deployer.AssetDeployer;
import org.jdesktop.wonderland.web.asset.deployer.AssetDeployer.DeployedAsset;
import org.jdesktop.wonderland.web.checksums.ChecksumFactory;
import org.jdesktop.wonderland.web.checksums.ChecksumFactory.ChecksumAction;
import org.jdesktop.wonderland.web.checksums.ChecksumManager;
import org.jdesktop.wonderland.web.checksums.modules.ModuleAssetDescriptor;

/**
 * The GetModuleListResource class is a Jersey RESTful service that returns the
 * ModuleInfo objects (contained within module.xml) of all modules in a given
 * state.
 * <p>
 * The state can either be installed, pending, or uninstall
 *
 * @author Jordan Slott <jslott@dev.java.net>
 */
@Path("/list/get/{state}")
public class GetModuleListResource {
    
    public static ChecksumList getChecksumList(String moduleName, String modulePart) {
        // Fetch all of the module parts given the module name. This case
        // handles if the module part is null (in which case all module parts)
        ChecksumManager checksumManager = ChecksumManager.getChecksumManager();
        Map<DeployedAsset, File> partMap = AssetDeployer.getFileMap(moduleName, modulePart);

        // Create a checksum list from all of the individual module parts and
        // put them into a single map.
        ChecksumList checksumList = new ChecksumList();
        for (DeployedAsset deployedAsset : partMap.keySet()) {
            // Create an proper AssetDeployer using the module name and module
            // part. Add to the master list.
            ModuleAssetDescriptor mad = new ModuleAssetDescriptor(
                    deployedAsset.moduleName, deployedAsset.assetType, null);
            ChecksumFactory factory = checksumManager.getChecksumFactory(mad);
            ChecksumList partList = factory.getChecksumList(mad, ChecksumAction.DO_NOT_GENERATE);
            if (partList != null) {

                checksumList.putChecksums(partList.getChecksumMap());
            }
        }
        return checksumList;
    }
    
    private static ModuleRepository getModuleRepository(Module module) {
        /* Fetch the error logger for use in this method */
        Logger logger = ModuleManager.getLogger();
        String moduleName = module.getName();
        
        // For the base URL of assets within a module, use the server URL and
        // point it to the webdav repository
        String hostname = System.getProperty(Constants.WEBSERVER_URL_PROP) + "webdav/content/modules/installed/" + moduleName;

        /* Fetch the module repository, return an error if it does not exist */
        ModuleRepository mr = module.getRepository();
        if (mr == null || mr.getResources() == null || mr.getResources().length == 0) {
            /*
             * If the repository doesn't exist (perhaps from a missing repository.xml
             * file, then create a fallback response with this server as the
             * master.
             */

            ModuleRepository newRepository = new ModuleRepository();
            ModuleRepository.Repository rep = new ModuleRepository.Repository();
            rep.url = hostname;
            rep.isServer = true;
            newRepository.setMaster(rep);
            return newRepository;
            
        }
        
        /* Since we potentially edit fields below, make a copy of the repository */
        ModuleRepository newRepository = new ModuleRepository(mr);
        
        /* Replace the master if its string is the special %WL_SERVER% */
        if (newRepository.getMaster() != null && newRepository.getMaster().url.compareTo(ModuleRepository.WL_SERVER) == 0) {
            ModuleRepository.Repository rep = new ModuleRepository.Repository();
            rep.url = hostname;
            rep.isServer = true;
            newRepository.setMaster(rep);
        }
        
        /* Replace the mirrors if its string is the special %WL_SERVER% */
        ModuleRepository.Repository mirrors[] = newRepository.getMirrors();
        if (mirrors != null) {
            for (int i = 0; i < mirrors.length; i++) {
                if (mirrors[i] != null && mirrors[i].url.compareTo(ModuleRepository.WL_SERVER) == 0) {
                    ModuleRepository.Repository rep = new ModuleRepository.Repository();
                    rep.url = hostname;
                    rep.isServer = true;
                    mirrors[i] = rep;
                }
            }
            newRepository.setMirrors(mirrors);
        }
        
        return newRepository;
    }    
    /**
     * Returns a list of modules in a given state.
     * <p>
+     * /module/list/get/http://localhost:8080/wonderland-web-modules/modules/list/get/installed{state}
     * <p>
     * where {state} is the state of the module, either pending, installed, or
     * uninstall.
     * <p>
     * All spaces in the module name must be encoded to %20. Returns BAD_REQUEST
     * to the HTTP connection if the module name is invalid or if there was an
     * error encoding the module's information.
     * 
     * @param state The desired state of the module
     * @return An XML encoding of the module's basic information
     */
    @GET
    @Produces({"application/xml", "application/json"})
    public Response getModuleList(@PathParam("state") String state) {
        ModuleManager manager = ModuleManager.getModuleManager();
        ModuleList moduleList = new ModuleList();
        
        /*
         * Check the state given, and fetch the modules. If the module state is
         * invalid, return a BAD_REQUEST error. Otherwise fetch the module list
         * according to the state and return a ModuleList object.
         */
        if (state == null) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        else if (state.equals("installed") == true) {
            Map<String, Module> modules = manager.getInstalledModules();

            // sort in dependecy order
            List<String> ordered = DeployManager.getDeploymentOrder(modules);

            // create the list of infos in the correct order
            Collection<ModuleInfo> list = new LinkedList();
            for (String moduleName : ordered) {
                Module module = modules.get(moduleName);
                ModuleInfo info = module.getInfo();
                info.setChecksumList(
                        getChecksumList(moduleName, null));
                
                info.setRepository(getModuleRepository(module));
                list.add(info);
            }

            moduleList.setModuleInfos(list.toArray(new ModuleInfo[] {}));
            return Response.ok(moduleList).build();
        }
        else if (state.equals("pending") == true) {
            Map<String, Module> modules = manager.getPendingModules();
            Collection<ModuleInfo> list = new LinkedList();
            Iterator<Map.Entry<String, Module>> it = modules.entrySet().iterator();
            while (it.hasNext() == true) {
                Map.Entry<String, Module> entry = it.next();
                list.add(entry.getValue().getInfo());
            }
            moduleList.setModuleInfos(list.toArray(new ModuleInfo[] {}));
            return Response.ok(moduleList).build();
        }
        else if (state.equals("uninstall") == true) {
            Map<String, ModuleInfo> modules = manager.getUninstallModuleInfos();
            Collection<ModuleInfo> list = new LinkedList();
            Iterator<Map.Entry<String, ModuleInfo>> it = modules.entrySet().iterator();
            while (it.hasNext() == true) {
                Map.Entry<String, ModuleInfo> entry = it.next();
                list.add(entry.getValue());
            }
            moduleList.setModuleInfos(list.toArray(new ModuleInfo[] {}));
            return Response.ok(moduleList).build();
        }
        else {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
    }
}
