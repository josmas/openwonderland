package org.jdesktop.wonderland.modules.servlets;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */


import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.FileUtils;
import org.jdesktop.wonderland.modules.Module;
import org.jdesktop.wonderland.modules.service.ModuleManager;
import org.jdesktop.wonderland.utils.RunUtil;

/**
 *
 * @author jordanslott
 */
public class ModuleUploadServlet extends HttpServlet {

    /** 
    * Handles the HTTP <code>GET</code> method.
    * @param request servlet request
    * @param response servlet response
    */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        throw new ServletException("Upload servlet only handles post");
    } 

    /** 
    * Handles the HTTP <code>POST</code> method.
    * @param request servlet request
    * @param response servlet response
    */
   // @Override
    protected void doPostOld(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        /*
         * Create a factory for disk-base file items to handle the request. Also
         * place the file in add/.
         */
        PrintWriter writer = response.getWriter();
        ModuleManager manager = ModuleManager.getModuleManager();
        Logger logger = ModuleManager.getLogger();
        
        /* Check that we have a file upload request */
        boolean isMultipart = ServletFileUpload.isMultipartContent(request);
        if (isMultipart == false) {
            logger.warning("[MODULE] UPLOAD Bad request");
            writer.println("Unable to recognize upload request. Press the ");
            writer.println("Back button on your browser and try again.<br><br>");
            return;
        }
        
        /* Create a factory for disk-based file items, set parameters */
        DiskFileItemFactory factory = new DiskFileItemFactory();
        factory.setSizeThreshold((int)(500 * FileUtils.ONE_MB));

        /*Create a new upload handler, set maximum file size */
        ServletFileUpload upload = new ServletFileUpload(factory);
        upload.setFileSizeMax(500 * FileUtils.ONE_MB);

        /* Parse the requests */
        List items = null;
        try {
            items = upload.parseRequest(request);
        } catch (FileUploadException excp) {
            /* Log an error to the log and write an error message back */
            logger.log(Level.WARNING, "[MODULE] UPLOAD Failed to parse request", excp);
            writer.println("Unable to deal with the upload request. Press the ");
            writer.println("Back button on your browser and try again.<br><br>");
            writer.println(excp.toString());
            return;
        }
        
        /* Loop through the items and deal with them */
        Iterator it = items.listIterator();
        while (it.hasNext() == true) {
            FileItem item = (FileItem)it.next();
            
            /*
             * The name given should have a .jar extension. Check this here. If
             * not, return an error. If so, parse out just the module name.
             */
            String moduleJar = item.getName();
            if (moduleJar.endsWith(".jar") == false) {
                /* Log an error to the log and write an error message back */
                logger.warning("[MODULE] UPLOAD File is not a jar file " + moduleJar);
                writer.println("The file " + moduleJar + " needs to be a jar file. Press the ");
                writer.println("Back button on your browser and try again.<br><br>");
                return;
            }
            String moduleName = moduleJar.substring(0, moduleJar.length() - 4);
            
            logger.info("[MODULE] UPLOAD Install module " + moduleName + " with file name " + moduleJar);
            
            /*
             * Write the file a temporary file
             */
            File tmpFile = null;
            try {
                tmpFile = File.createTempFile(moduleName, ".jar");
                tmpFile.deleteOnExit();
                item.write(tmpFile);
                logger.info("[MODULE] UPLOAD Wrote added module to " + tmpFile.getAbsolutePath());
            } catch (java.lang.Exception excp) {
                /* Log an error to the log and write an error message back */
                logger.log(Level.WARNING, "[MODULE] UPLOAD Failed to save file", excp);
                writer.println("Unable to save the file to the module directory. Press the ");
                writer.println("Back button on your browser and try again.<br><br>");
                writer.println(excp.toString());
                return;
            }
            
            /* Add the new module */
            Collection<File> moduleFiles = new LinkedList<File>();
            moduleFiles.add(tmpFile);
            Collection<Module> result = manager.addToInstall(moduleFiles);
            if (result.isEmpty() == true) {
                /* Log an error to the log and write an error message back */
                logger.warning("[MODULE] UPLOAD Failed to install module " + moduleName);
                writer.println("Unable to install module for some reason. Press the ");
                writer.println("Back button on your browser and try again.<br><br>");
                return;
            }
        }

        /* Install all of the modules that are possible */
        manager.installAll();
        
        /* If we have reached here, then post a simple message */
        logger.info("[MODULE] UPLOAD Added module successfuly");
        writer.print("Module added successfully. Please hit Back on your browswer ");
        writer.print("and refresh the page to see the updates. (Better UI coming ");
        writer.print("here soon!)");
    }

    /** 
    * Handles the HTTP <code>POST</code> method.
    * @param request servlet request
    * @param response servlet response
    */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        /*
         * Create a factory for disk-base file items to handle the request. Also
         * place the file in add/.
         */
        PrintWriter writer = response.getWriter();
        ModuleManager manager = ModuleManager.getModuleManager();
        Logger logger = ModuleManager.getLogger();
        
        /* Check that we have a file upload request */
        boolean isMultipart = ServletFileUpload.isMultipartContent(request);
        if (isMultipart == false) {
            logger.warning("[MODULE] UPLOAD Bad request");
            writer.println("Unable to recognize upload request. Press the ");
            writer.println("Back button on your browser and try again.<br><br>");
            return;
        }
 
        // Create a new file upload handler
        ServletFileUpload upload = new ServletFileUpload();

        // Parse the request
        try {
            FileItemIterator iter = upload.getItemIterator(request);
            while (iter.hasNext() == true) {
                FileItemStream item = iter.next();
                String name = item.getFieldName();
                InputStream stream = item.openStream();
                if (item.isFormField() == false) {
                    /*
                     * The name given should have a .jar extension. Check this here. If
                     * not, return an error. If so, parse out just the module name.
                     */
                    String moduleJar = item.getName();
                    if (moduleJar.endsWith(".jar") == false) {
                        /* Log an error to the log and write an error message back */
                        logger.warning("[MODULE] UPLOAD File is not a jar file " + moduleJar);
                        writer.println("The file " + moduleJar + " needs to be a jar file. Press the ");
                        writer.println("Back button on your browser and try again.<br><br>");
                        return;
                    }
                    String moduleName = moduleJar.substring(0, moduleJar.length() - 4);

                    logger.info("[MODULE] UPLOAD Install module " + moduleName + " with file name " + moduleJar);
                    
                    /*
                     * Write the file a temporary file
                     */
                    File tmpFile = null;
                    try {
                        tmpFile = File.createTempFile(moduleName, ".jar");
                        tmpFile.deleteOnExit();
                        RunUtil.writeToFile(stream, tmpFile);
                        logger.info("[MODULE] UPLOAD Wrote added module to " + tmpFile.getAbsolutePath());
                    } catch (java.lang.Exception excp) {
                        /* Log an error to the log and write an error message back */
                        logger.log(Level.WARNING, "[MODULE] UPLOAD Failed to save file", excp);
                        writer.println("Unable to save the file to the module directory. Press the ");
                        writer.println("Back button on your browser and try again.<br><br>");
                        writer.println(excp.toString());
                        return;
                    }

                    /* Add the new module */
                    Collection<File> moduleFiles = new LinkedList<File>();
                    moduleFiles.add(tmpFile);
                    Collection<Module> result = manager.addToInstall(moduleFiles);
                    if (result.isEmpty() == true) {
                        /* Log an error to the log and write an error message back */
                        logger.warning("[MODULE] UPLOAD Failed to install module " + moduleName);
                        writer.println("Unable to install module for some reason. Press the ");
                        writer.println("Back button on your browser and try again.<br><br>");
                        return;
                    }
                }
            }
        } catch (FileUploadException excp) {
            /* Log an error to the log and write an error message back */
            logger.log(Level.WARNING, "[MODULE] UPLOAD Failed", excp);
            writer.println("Unable to install module for some reason. Press the ");
            writer.println("Back button on your browser and try again.<br><br>");
            return;
        }
 
        /* Install all of the modules that are possible */
        manager.installAll();
        
        /* If we have reached here, then post a simple message */
        logger.info("[MODULE] UPLOAD Added module successfuly");
        writer.print("Module added successfully. Please hit Back on your browswer ");
        writer.print("and refresh the page to see the updates. (Better UI coming ");
        writer.print("here soon!)");
    }
    
    /** 
    * Returns a short description of the servlet.
    */
    @Override
    public String getServletInfo() {
        return "Module Upload Servlet";
    }
}
