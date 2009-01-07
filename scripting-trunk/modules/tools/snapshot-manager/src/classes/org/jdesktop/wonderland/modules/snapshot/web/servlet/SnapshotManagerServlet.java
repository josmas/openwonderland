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
package org.jdesktop.wonderland.modules.snapshot.web.servlet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.jdesktop.wonderland.front.admin.AdminRegistration;
import org.jdesktop.wonderland.web.wfs.WFSManager;
import org.jdesktop.wonderland.web.wfs.WFSSnapshot;

/**
 *
 * @author jkaplan
 */
public class SnapshotManagerServlet extends HttpServlet
    implements ServletContextListener
{
    // our registration with the webadmin system
    private AdminRegistration reg;

    /** 
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request,
                                  HttpServletResponse response)
        throws ServletException, IOException
    {
        WFSManager m = WFSManager.getWFSManager();

        String action = request.getParameter("action");
        if (action == null) {
            action = "view";
        }

        String snapshotName = request.getParameter("snapshot");
        WFSSnapshot snapshot = null;
        if (snapshotName != null) {
            snapshot = m.getWFSSnapshot(snapshotName);
        }

        String error = null;
        if (action.equalsIgnoreCase("update")) {
            error = doUpdate(request, response, snapshot);
            if (error != null) {
                // return to the edit page
                request.setAttribute("error", error);
                action = "edit";
            }
        }
        
        if (action.equalsIgnoreCase("edit")) {
            error = doEdit(request, response, snapshot);
            if (error == null) {
                return;
            }
            request.setAttribute("error", error);
        }

        if (action.equalsIgnoreCase("remove")) {
            error = doRemove(request, response, snapshot);
        }

        // if we get here, it means we are going to display the main page
        request.setAttribute("error", error);

        // store the wfs roots in a variable
        String[] wfsRoots = m.getWFSRoots();
        request.setAttribute("roots", wfsRoots);

        // store the wfs snapshots in a variable.  Sort the snapshots by date
        List<WFSSnapshot> snapshots = new ArrayList<WFSSnapshot>(m.getWFSSnapshots());
        Collections.sort(snapshots, new Comparator<WFSSnapshot>() {
            public int compare(WFSSnapshot o1, WFSSnapshot o2) {
                if (o1.getTimestamp() == null) {
                    return (o2.getTimestamp() == null)?0:1;
                } else if (o2.getTimestamp() == null) {
                    return -1;
                }
                
                return -1 * o1.getTimestamp().compareTo(o2.getTimestamp());
            }            
        });
        request.setAttribute("snapshots", snapshots);

        RequestDispatcher rd =
                getServletContext().getRequestDispatcher("/snapshots.jsp");
        rd.forward(request, response);
    } 

    protected String doEdit(HttpServletRequest request,
                          HttpServletResponse response,
                          WFSSnapshot snapshot)
        throws ServletException, IOException
    {
        if (snapshot == null) {
            return "No such snapshot " + request.getParameter("snapshot");
        }
        request.setAttribute("snapshot", snapshot);

        RequestDispatcher rd =
                getServletContext().getRequestDispatcher("/edit.jsp");
        rd.forward(request, response);
        return null;
    }

    protected String doUpdate(HttpServletRequest request,
                               HttpServletResponse response,
                               WFSSnapshot snapshot)
        throws ServletException, IOException
    {
        if (snapshot == null) {
            return "No such snapshot " + request.getParameter("snapshot");
        }

        String name = request.getParameter("name");
        if (name == null) {
            name = "";
        }

        String description = request.getParameter("description");
        if (description == null) {
            description = "";
        }

        if (!name.equals(snapshot.getName())) {
            // change the name
            String error = validName(name, snapshot);

            System.out.println("Setting name to: " + name + " error: " + error);

            if (error != null) {
                return error;
            }

            
            snapshot.setName(name);
        }

        if (!description.equals(snapshot.getDescription())) {
            snapshot.setDescription(description);
        }

        return null;
    }

    protected String doRemove(HttpServletRequest request,
                              HttpServletResponse response,
                              WFSSnapshot snapshot)
        throws ServletException, IOException
    {
        if (snapshot == null) {
            return "No such snapshot " + request.getParameter("snapshot");
        }

        WFSManager.getWFSManager().removeWFSSnapshot(snapshot.getName());

        return null;
    }

    /**
     * Return any errors changing the given snapshot to the given name,
     * or null if there are no errors.
     */
    protected String validName(String name, WFSSnapshot snapshot) {
        if (name == null || name.trim().length() == 0) {
            return "Blank name";
        } else if (name.contains(" ") || name.contains("\t")) {
            return "Spaces not allowed in name";
        } else if (WFSManager.getWFSManager().getWFSSnapshot(name) != null) {
            return "Duplicate snapshot " + name;
        } else {
            return null;
        }
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /** 
     * Handles the HTTP <code>GET</code> method.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        processRequest(request, response);
    } 

    /** 
     * Handles the HTTP <code>POST</code> method.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        processRequest(request, response);
    }

    /** 
     * Returns a short description of the servlet.
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

    public void contextInitialized(ServletContextEvent sce) {
        // register with web admin
        reg = new AdminRegistration("Manage Snapshots",
                                    "/snapshot-manager/snapshot/SnapshotManager");
        AdminRegistration.register(reg, sce.getServletContext());
    }

    public void contextDestroyed(ServletContextEvent sce) {
        // unregister
        AdminRegistration.unregister(reg, sce.getServletContext());
    }
}
