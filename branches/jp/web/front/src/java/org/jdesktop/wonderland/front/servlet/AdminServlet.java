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

package org.jdesktop.wonderland.front.servlet;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author jkaplan
 */
public class AdminServlet extends HttpServlet {
   private static final String DEFAULT_PAGE_URL = "/wonderland-web-front/admin_default.jsp";
    
   // hardcode for now
   private static Map<String, AdminEntry> adminPages = 
           new LinkedHashMap<String, AdminEntry>();
   static {
       adminPages.put("runner", new AdminEntry("Start/Stop Server", "/wonderland-web-runner"));
       adminPages.put("modules", new AdminEntry("Manage Modules", "/wonderland-web-modules"));
   }
   
   /** 
    * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
    * @param request servlet request
    * @param response servlet response
    */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException 
    {
        String pageURL = request.getParameter("pageURL");
        if (pageURL == null) {
            String page = request.getParameter("page");
            AdminEntry ae = mapPage(page);
            if (ae != null) {
                pageURL = ae.getUrl();
            }
        }
        
        if (pageURL == null) {
            pageURL = DEFAULT_PAGE_URL;
        }           
        
        request.setAttribute("pageURL", pageURL);
        request.setAttribute("adminPages", adminPages.values());
        
        RequestDispatcher rd = request.getRequestDispatcher("/admin.jsp");
        rd.forward(request, response);
    } 

    private AdminEntry mapPage(String page) {
        return adminPages.get(page);
    }
        
    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /** 
    * Handles the HTTP <code>GET</code> method.
    * @param request servlet request
    * @param response servlet response
    */
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        processRequest(request, response);
    } 

    /** 
    * Handles the HTTP <code>POST</code> method.
    * @param request servlet request
    * @param response servlet response
    */
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        processRequest(request, response);
    }

    /** 
    * Returns a short description of the servlet.
    */
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}
