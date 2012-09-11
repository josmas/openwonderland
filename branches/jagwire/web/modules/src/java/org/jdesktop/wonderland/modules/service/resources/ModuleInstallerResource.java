/**
 * Open Wonderland
 *
 * Copyright (c) 2012, Open Wonderland Foundation, All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above copyright and
 * this condition.
 *
 * The contents of this file are subject to the GNU General Public License,
 * Version 2 (the "License"); you may not use this file except in compliance
 * with the License. A copy of the License is available at
 * http://www.opensource.org/licenses/gpl-license.php.
 *
 * The Open Wonderland Foundation designates this particular file as subject to
 * the "Classpath" exception as provided by the Open Wonderland Foundation in
 * the License file that accompanied this code.
 */
package org.jdesktop.wonderland.modules.service.resources;

import com.google.common.collect.Lists;
import com.sun.istack.logging.Logger;
import com.sun.jersey.api.client.ClientResponse.Status;
import com.sun.jersey.core.header.FormDataContentDisposition;
import java.io.File;
import java.io.FileInputStream;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import org.jdesktop.wonderland.modules.Module;
import org.jdesktop.wonderland.modules.service.ModuleManager;
import org.jdesktop.wonderland.utils.RunUtil;

/**
 *
 * @author JagWire
 */
@Path("/install")
public class ModuleInstallerResource {
    private static final Logger logger = Logger.getLogger(ModuleInstallerResource.class);
    /**
     * Heavily adapted from ModuleUploadServlet written by Jordan Slott
     * 
     * @param uploadedInputStream
     * @param fileDetail
     * @return 
     */
    @POST
    @Path("/module")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response installNewModule(
            @FormParam("files[]") File file,
            @FormParam("files[]")FormDataContentDisposition info
            ) 
    {
         
        ModuleManager manager = ModuleManager.getModuleManager();
        
        String name = info.getFileName();
        System.out.println("file: "+name);
        if(name.endsWith(".jar")) {
            //success
            //parse out the '.jar'
            name = info.getFileName().substring(0,info.getFileName().length() - 4);
        } else {
            //fail
            logger.warning("FILE NOT A MODULE!");
            return Response.status(Status.INTERNAL_SERVER_ERROR).cacheControl(new CacheControl()).build();
        }
        
        File tmpFile = null;
        try {
            tmpFile = File.createTempFile(name+"_tmp", ".jar");
            tmpFile.deleteOnExit();
            RunUtil.writeToFile(new FileInputStream(file), tmpFile);
        } catch(Exception e) {
            logger.warning("ERROR WRITING TO FILE!");
            return Response.status(Status.INTERNAL_SERVER_ERROR).cacheControl(new CacheControl()).build();
        }
        
        
        Collection<File> moduleFiles = new LinkedList<File>();
        moduleFiles.add(tmpFile);
        Collection<Module> result = manager.addToInstall(moduleFiles);
        if(result.isEmpty()) {
            logger.warning("NOTHING IN MODULE!");
            return Response.status(Status.INTERNAL_SERVER_ERROR).cacheControl(new CacheControl()).build();
        }
        
        //We won't use this data on the client side, so pass in dummy data for
        //file size and URL.        
        FileMeta metaData = new FileMeta(tmpFile.getName(), 0, "");
        List<FileMeta> metas = Lists.newArrayList(metaData);
        GenericEntity<List<FileMeta>> entity = new GenericEntity<List<FileMeta>>(metas){};
        return Response.ok(entity).cacheControl(new CacheControl()).build();
    }
    
    @Path("/all")
    @GET
    public Response tryInstallAll() {
        ModuleManager manager = ModuleManager.getModuleManager();
        manager.installAll();
        
        return Response.ok().cacheControl(new CacheControl()).build();
    }
    
    @XmlRootElement
    @XmlAccessorType(XmlAccessType.FIELD)
    static class FileMeta {

        private String name;
        private long size;
        private String url;
        private String delete_url;

        public String getDelete_type() {
            return delete_type;
        }

        public void setDelete_type(String delete_type) {
            this.delete_type = delete_type;
        }

        public String getDelete_url() {
            return delete_url;
        }

        public void setDelete_url(String delete_url) {
            this.delete_url = delete_url;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public long getSize() {
            return size;
        }

        public void setSize(long size) {
            this.size = size;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }
        private String delete_type;

        public FileMeta(String filename, long size, String url) {
            this.name = filename;
            this.size = size;
            this.url = url;
            this.delete_url = url;
            this.delete_type = "DELETE";
        }

        public FileMeta() {
        }
    }

    
}
