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
package org.jdesktop.wonderland.runner.servlet;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.jdesktop.wonderland.runner.RunManager;
import org.jdesktop.wonderland.utils.FileListUtil;
import org.jdesktop.wonderland.utils.RunUtil;

/**
 *
 * @author kaplanj
 */
public class RunnerUploadServlet extends HttpServlet {
    private static final Logger logger =
            Logger.getLogger(RunnerUploadServlet.class.getName());

    /** 
     * Handles the HTTP <code>GET</code> method.
     * @param request servlet request
     * @param response servlet response
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException
    {
        throw new ServletException("Upload servlet only handles post");
    }

    /** 
     * Handles the HTTP <code>POST</code> method.
     * @param request servlet request
     * @param response servlet response
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException
    {
        /*
         * Create a factory for disk-base file items to handle the request. Also
         * place the file in add/.
         */
        PrintWriter writer = response.getWriter();
        
        /* Check that we have a file upload request */
        boolean isMultipart = ServletFileUpload.isMultipartContent(request);
        if (isMultipart == false) {
            logger.warning("[Runner] UPLOAD Bad request");
            String message = "Unable to recognize upload request. Please " +
                             "try again.";
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, message);
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
                    File tmp = File.createTempFile("runnerupload", "tmp");
                    RunUtil.writeToFile(stream, tmp);
                    RunManager.getInstance().addRunnerZip(name, tmp);
                    tmp.delete();
                }
            }
        } catch (FileUploadException excp) {
            /* Log an error to the log and write an error message back */
            logger.log(Level.WARNING, "[Runner] UPLOAD Failed", excp);
            String message = "Unable to upload runner for some reason.";
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                               message);
            return;
        } catch (IOException excp) {
            /* Log an error to the log and write an error message back */
            logger.log(Level.WARNING, "[Runner] UPLOAD Failed", excp);
            String message = "Unable to upload runner for some reason.";
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                               message);
            return;
        }
 
        /* If we have reached here, then post a simple message */
        logger.info("[Runner] UPLOAD Added runner files successfuly");
        writer.print("Runner file added successfully.");
    }

    /** 
     * Returns a short description of the servlet.
     */
    @Override
    public String getServletInfo() {
        return "Runner Upload Servlet";
    }
}
