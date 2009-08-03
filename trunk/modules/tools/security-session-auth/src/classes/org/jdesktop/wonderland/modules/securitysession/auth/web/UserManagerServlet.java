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
package org.jdesktop.wonderland.modules.securitysession.auth.web;

import java.io.IOException;
import java.net.URLEncoder;
import java.security.Principal;
import java.util.List;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.jdesktop.wonderland.modules.securitysession.auth.weblib.db.UserDAO;
import org.jdesktop.wonderland.modules.securitysession.auth.weblib.db.UserEntity;

/**
 *
 * @author jkaplan
 */
public class UserManagerServlet extends HttpServlet {
    @PersistenceUnit(unitName="WonderlandUserPU")
    private EntityManagerFactory emf;
   
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
        UserDAO users = new UserDAO(emf);

        // determine if this is an administrator
        boolean admin = request.isUserInRole("admin");

        // read the action from the request
        String action = request.getParameter("action");
        if (action == null) {
            if (admin) {
                // default action for administrators is view
                action = "view";
            } else {
                // default action for non-admins is edit their own details
                action = "edit";
            }
        }

        // try to resolve a user (if any) from the request
        String userId = request.getParameter("id");
        UserEntity user = null;

        // if this is not an administrator, default to editing the user's
        // own account
        if (userId == null && !admin && request.getUserPrincipal() != null) {
            userId = request.getUserPrincipal().getName();
        }

        // if a userId was found, resolve it into a user object
        if (userId != null) {
            user =  users.getUser(userId);
        }

        // now process the action
        if (action.equalsIgnoreCase("edit")) {
            doEdit(request, response, user, false);
        } else if (action.equalsIgnoreCase("create")) {
            doEdit(request, response, null, true);
        } else if (action.equalsIgnoreCase("remove")) {
            doRemove(request, response, user, users);
        } else if (action.equalsIgnoreCase("save")) {
            doSave(request, response, user, users);
        } else {
            doView(request, response, users);
        }
    }

    protected void doView(HttpServletRequest request,
                          HttpServletResponse response, UserDAO users)
        throws ServletException, IOException
    {
        // requires admin
        if (!checkAdmin(request, response)) {
            return;
        }

        List<UserEntity> userList = users.getUsers();
        request.setAttribute("users", userList);

        RequestDispatcher rd = getServletContext().getRequestDispatcher("/view.jsp");
        rd.forward(request, response);
    }

    protected void doEdit(HttpServletRequest request,
                          HttpServletResponse response,
                          UserEntity user, boolean create)
        throws ServletException, IOException
    {
        if (create) {
            // only an admin may create
            if (!checkAdmin(request, response)) {
                return;
            }

            request.setAttribute("create", "true");
        }

        if (user == null) {
            user = new UserEntity();
        }

        request.setAttribute("user", user);

        RequestDispatcher rd = getServletContext().getRequestDispatcher("/edit.jsp");
        rd.forward(request, response);
    }

    protected void doSave(HttpServletRequest request,
                          HttpServletResponse response, UserEntity existing,
                          UserDAO users)
        throws ServletException, IOException
    {
        boolean create = request.getParameter("create").equalsIgnoreCase("true");

        UserEntity user;
        try {
            user = getUser(request);
        } catch (IllegalArgumentException iae) {
            request.setAttribute("error", iae.getMessage());
            doEdit(request, response, existing, create);
            return;
        }

        // only an owner or administrator can save a user
        if (!checkOwner(request, response, user.getId())) {
            return;
        }

        // if we are creating, make sure it isn't a duplicate
        if (create && existing != null) {
            request.setAttribute("error", "Duplicate user id: " + user.getId());
            doEdit(request, response, user, true);
            return;
        }

        // update
        users.updateUser(user);
        redirectToView(response);
    }

    protected void doRemove(HttpServletRequest request,
                            HttpServletResponse response, UserEntity user,
                            UserDAO users)
        throws ServletException, IOException
    {
        // make sure we have a user to remove
        if (user == null) {
            request.setAttribute("error", "Unknown user " +
                                 request.getParameter("id"));
            doView(request, response, users);
            return;
        }

        // only an owner can remove a user
        if (!checkOwner(request, response, user.getId())) {
            return;
        }

        // go ahead and remove the user
        users.removeUser(user.getId());

        // now redisplay the view
        redirectToView(response);
    }

    protected UserEntity getUser(HttpServletRequest req)
        throws IllegalArgumentException
    {
        UserEntity ue = new UserEntity();
        ue.setId(req.getParameter("id"));
        ue.setFullname(req.getParameter("fullname"));
        ue.setEmail(req.getParameter("email"));

        // if password and confirm are both set, then try to change
        // the password
        String password = req.getParameter("password");
        String confirm  = req.getParameter("confirmPassword");
        if (confirm != null && confirm.length() > 0) {
            if (!confirm.equals(password)) {
                throw new IllegalArgumentException("Passwords don't match.");
            }

            ue.setPassword(password);
        }

        return ue;
    }

    protected void redirectToView(HttpServletResponse response)
        throws IOException
    {
        redirectTo(response, "/security-session-auth/security-session-auth/users");
    }

    protected void redirectTo(HttpServletResponse response, String page)
            throws IOException
    {
        String url = "/wonderland-web-front/admin?pageURL=" +
                URLEncoder.encode(page, "utf-8");

        response.getWriter().println("<script>");
        response.getWriter().println("parent.location.replace('" + url + "');");
        response.getWriter().println("</script>");
        response.getWriter().close();
    }

    protected boolean checkAdmin(HttpServletRequest request,
                                 HttpServletResponse response)
        throws ServletException, IOException
    {
        if (!request.isUserInRole("admin")) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED,
                               "Admin access is required.");
            return false;
        }

        return true;
    }

    protected boolean checkOwner(HttpServletRequest request,
                                 HttpServletResponse response,
                                 String user)
        throws ServletException, IOException
    {
        // admin always has permissions
        if (request.isUserInRole("admin")) {
            return true;
        }

        Principal p = request.getUserPrincipal();
        if (p == null || !p.getName().equals(user)) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED,
                               "Only the owner is allowed to edit user " + user);
            return false;
        }

        return true;
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

}
