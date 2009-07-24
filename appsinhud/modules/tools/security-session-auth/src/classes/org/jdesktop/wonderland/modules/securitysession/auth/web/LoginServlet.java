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
import java.util.logging.Logger;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.jdesktop.wonderland.modules.security.weblib.serverauthmodule.SessionResolver;
import org.jdesktop.wonderland.modules.securitysession.auth.weblib.AuthSessionManagerImpl;
import org.jdesktop.wonderland.modules.securitysession.weblib.SessionLoginException;
import org.jdesktop.wonderland.modules.securitysession.weblib.UserRecord;

/**
 *
 * @author jkaplan
 */
public class LoginServlet extends HttpServlet {
    private static final Logger logger =
            Logger.getLogger(LoginServlet.class.getName());

    /** 
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException
    {
        String action = request.getParameter("action");
        if (action == null) {
            action = "view";
        }

        if (action.equalsIgnoreCase("login")) {
            doLogin(request, response);
        } else if (action.equalsIgnoreCase("logout")) {
            doLogout(request, response);
        } else {
            doView(request, response);
        }
    } 

    protected void doView(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException
    {
        RequestDispatcher rd = getServletContext().getRequestDispatcher("/login.jsp");
        rd.forward(request, response);
    }

    protected void doLogout(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException
    {
        String userId = request.getUserPrincipal().getName();
        if (userId != null) {
            // create a session manager impl.  This is OK to do, because the
            // real work is done by a singleton under the covers, so we are sure
            // to get the right data
            AuthSessionManagerImpl impl = new AuthSessionManagerImpl();
            UserRecord rec = impl.get(userId);

            if (rec != null) {
                impl.logout(rec.getToken());
            }
        }

        redirectTo(response, null);
    }

    protected void doLogin(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException
    {
        String username = request.getParameter("username");
        String password = request.getParameter("password");

        if (username == null || username.length() == 0 ||
            password == null || password.length() == 0)
        {
            request.setAttribute("error", "No username or password specified.");
            doView(request, response);
            return;
        }

        // create a session manager impl.  This is OK to do, because the
        // real work is done by a singleton under the covers, so we are sure
        // to get the right data
        AuthSessionManagerImpl impl = new AuthSessionManagerImpl();
        UserRecord rec;
        try {
            rec = impl.login(username, password);
            logger.fine("Record for " + username + " is " + rec);
        } catch (SessionLoginException sle) {
            throw new ServletException(sle);
        }

        // see if the login succeeded
        if (rec == null) {
            request.setAttribute("error", "Invalid username or password.");
            doView(request, response);
            return;
        }

        // everything worked! See if there is a page to forward to
        String forwardTo = request.getParameter("forwardPage");
        if (forwardTo == null || forwardTo.trim().length() == 0) {
            forwardTo = "/wonderland-web-front/admin";
        }

        // create a cookie to identify this session
        Cookie session = new Cookie(SessionResolver.COOKIE_NAME,
                URLEncoder.encode(rec.getToken(), "UTF-8"));
        session.setPath("/");
        response.addCookie(session);

        logger.fine("Adding cookie for " + rec.getToken() + " for " +
                    username);

        // use a redirect to go to the new page, now that we are authenticated
        redirectTo(response, forwardTo);
    }

    protected void redirectTo(HttpServletResponse response, String page)
            throws IOException
    {
        String url = "/wonderland-web-front/admin";
        if (page != null && page.length() > 0) {
            url += "?pageURL=" + URLEncoder.encode(page, "utf-8");
        }

        response.getWriter().println("<script>");
        response.getWriter().println("parent.location.replace('" + url + "');");
        response.getWriter().println("</script>");
        response.getWriter().close();
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
