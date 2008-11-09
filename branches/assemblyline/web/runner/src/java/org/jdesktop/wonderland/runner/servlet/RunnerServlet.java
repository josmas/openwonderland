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

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.jdesktop.wonderland.runner.DeploymentEntry;
import org.jdesktop.wonderland.runner.DeploymentManager;
import org.jdesktop.wonderland.runner.DeploymentPlan;
import org.jdesktop.wonderland.runner.RunManager;
import org.jdesktop.wonderland.runner.Runner;

/**
 *
 * @author jkaplan
 */
public class RunnerServlet extends HttpServlet {
    /** a logger */
    private static final Logger logger =
            Logger.getLogger(RunnerServlet.class.getName());
    
    /** 
    * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
    * @param request servlet request
    * @param response servlet response
    */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException
    {
        String action = request.getParameter("action");
        String runnerName = request.getParameter("name");
        Runner runner = null;
        
        if (action == null) {
            action = "view";
        }
        if (runnerName != null) {
            runner = RunManager.getInstance().get(runnerName);
        }
              
        if (action.equalsIgnoreCase("log")) {
            doLog(request, response, runner);
        } else if (action.equalsIgnoreCase("edit")) {
            doEdit(request, response, runner, null);
        } else if (action.equalsIgnoreCase("editForm")) {
            doEditForm(request, response, runner);
        } else {
            // default case -- show the view page
            doView(request, response);
        }
    }
    
    private void doView(HttpServletRequest request,
                        HttpServletResponse response)
        throws ServletException, IOException
    {
        // forward to the view page
        RequestDispatcher rd = request.getRequestDispatcher("/view.jsp");
        rd.forward(request, response);
    }
    
    protected void doLog(HttpServletRequest request, 
                         HttpServletResponse response,
                         Runner runner)
        throws ServletException, IOException
    {
        FileReader fr = new FileReader(runner.getLogFile());
        request.setAttribute("log", new LogReader(fr));
        
        RequestDispatcher rd = request.getRequestDispatcher("/log.jsp");
        rd.forward(request, response);
    }

    protected void doEdit(HttpServletRequest request, 
                         HttpServletResponse response,
                         Runner runner, DeploymentEntry de)
        throws ServletException, IOException
    {
        if (de == null) {
            de = DeploymentManager.getInstance().getEntry(runner.getName());
        }
        
        // if the deployment entry has no properties, use the defaults
        if (de.getRunProps().isEmpty()) {
            de.setRunProps(runner.getDefaultProperties());
        }
        
        request.setAttribute("entry", de);
       
        RequestDispatcher rd = request.getRequestDispatcher("/edit.jsp");
        rd.forward(request, response);
    }
    
    protected void doEditForm(HttpServletRequest request,
                              HttpServletResponse response,
                              Runner runner)
        throws ServletException, IOException
    {
        DeploymentEntry de = getEntry(request);
        
        String button = request.getParameter("button");
        if (button.equalsIgnoreCase("Save")) {
            doEditSave(request, response, runner, de);
        } else if (button.equalsIgnoreCase("Cancel")) {
            redirectToRun(response);
        } else if (button.equalsIgnoreCase("Restore Defaults")) {
            de.setRunProps(runner.getDefaultProperties());
            doEdit(request, response, runner, de);
        } else {
            doEdit(request, response, runner, de);
        }
    }
    
    protected void doEditSave(HttpServletRequest request, 
                              HttpServletResponse response,
                              Runner runner,
                              DeploymentEntry de)
        throws ServletException, IOException
    {
        DeploymentManager dm = DeploymentManager.getInstance();
        DeploymentPlan dp = dm.getPlan();
        
        // if the properties are the same as the default property set,
        // remove all properties so we preserve the fact that these
        // are defaults
        if (de.getRunProps().equals(runner.getDefaultProperties())) {
            de.getRunProps().clear();
        }
        
        // replace the existing entry with the new one
        dp.removeEntry(de);
        dp.addEntry(de);
        dm.savePlan();
        
        redirectToRun(response);
    }
    
    protected void redirectToRun(HttpServletResponse response) 
        throws IOException
    {
        String page = "/wonderland-web-runner/run";
        String url = "/wonderland-web-front/admin?pageURL=" +
                URLEncoder.encode(page, "utf-8");
        
        response.getWriter().println("<script>");
        response.getWriter().println("parent.location.replace('" + url + "');");
        response.getWriter().println("</script>");
        response.getWriter().close();
    }
    
    protected DeploymentEntry getEntry(HttpServletRequest request) 
        throws ServletException
    {
        // read name and class
        String name = request.getParameter("name");
        String clazz = request.getParameter("class");
        
        // read properties
        Properties props = new Properties();
        int c = 1;
        String key;
        String value;
        while (true) {
            key = request.getParameter("key-" + c);
            value = request.getParameter("value-" + c);
            
            if (key == null) {
                break;
            }
            
            if (key.trim().length() > 0) {
                props.put(key, value);
            }
            c++;
        } 
        
        // read new property
        key = request.getParameter("key-new");
        value = request.getParameter("value-new");
        if (key != null && key.trim().length() > 0) {
            props.put(key, value);
        }
        
        DeploymentEntry de = new DeploymentEntry(name, clazz);
        de.setRunProps(props);
        
        return de;
    }
    
    static class LogReader implements Iterator<String> {
        private BufferedReader log;
        private String line;
        
        public LogReader(Reader log) {
            this.log = new BufferedReader(log);
            readNextLine();
        }
        
        public void readNextLine() {
            try {
                line = log.readLine();
            } catch (IOException ioe) {
                logger.log(Level.WARNING, "Error reading log file", ioe);
                line = null;
            }
        }

        public boolean hasNext() {
            return (line != null);
        }

        public String next() {
            String ret = line;
            readNextLine();
            return ret;
        }

        public void remove() {
            throw new UnsupportedOperationException("Not supported yet.");
        }
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
