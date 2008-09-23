package org.jdesktop.wonderland.modules.servlets;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */


import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.fileupload.DiskFileUpload;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.jdesktop.wonderland.modules.service.AddedModule;
import org.jdesktop.wonderland.modules.service.ModuleManager;
import org.jdesktop.wonderland.modules.service.ModuleManager.State;

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
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        /*
         * Create a factory for disk-base file items to handle the request. Also
         * place the file in add/.
         */
        System.out.println("IN MODULE UPLOAD SERVLET");
        PrintWriter writer = response.getWriter();
        ModuleManager manager = ModuleManager.getModuleManager();
        Logger logger = ModuleManager.getLogger();
        
        /* Create the servlet handler and parse the request */
        DiskFileUpload upload = new DiskFileUpload();
        List items = null;
        try {
            items = upload.parseRequest(request);
        } catch (FileUploadException excp) {
            /* Log an error to the log and write an error message back */
            logger.warning("[MODULE] UPLOAD Failed to parse request " + excp.toString());
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
             * Write the file to the add/ directory using the name of the file.
             */
            File file = null;
            try {
                file = new File(manager.getModuleStateDirectory(State.ADD), moduleJar);
                item.write(file);
                logger.info("[MODULE] UPLOAD Wrote added module to " + file.getAbsolutePath());
            } catch (java.lang.Exception excp) {
                /* Log an error to the log and write an error message back */
                logger.warning("[MODULE] UPLOAD Failed to save file to " + file.getAbsolutePath());
                logger.warning("[MODULE] UPLOAD " + excp.toString());
                writer.println("Unable to save the file to the module directory. Press the ");
                writer.println("Back button on your browser and try again.<br><br>");
                writer.println(excp.toString());
                return;
            }
            
            /*
             * Open the module and attempt to add it
             */
            AddedModule am = (AddedModule) manager.getModule(moduleName, State.ADD);
            if (am == null) {
                /* Log an error to the log and write an error message back */
                logger.warning("[MODULE] UPLOAD Failed to find added module for some reason " + moduleName);
                writer.println("Cannot find the module " + moduleName + " just added. Press the ");
                writer.println("Back button on your browser and try again.<br><br>");
                return;
            }
            
            Collection<AddedModule> modules = new LinkedList<AddedModule>();
            modules.add(am);
            Collection<String> result = manager.addAll(modules, true);
            if (result.contains(moduleName) == false) {
                /* Log an error to the log and write an error message back */
                logger.warning("[MODULE] UPLOAD Failed to install module " + moduleName);
                writer.println("Unable to install module for some reason. Press the ");
                writer.println("Back button on your browser and try again.<br><br>");
                return;
            }
        }
        
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
