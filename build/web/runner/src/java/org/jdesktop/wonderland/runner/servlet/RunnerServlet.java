/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.runner.servlet;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Collection;
import java.util.Iterator;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.jdesktop.wonderland.runner.DarkstarRunner;
import org.jdesktop.wonderland.runner.RunManager;
import org.jdesktop.wonderland.runner.Runner;
import org.jdesktop.wonderland.runner.RunnerException;
import org.jdesktop.wonderland.runner.RunnerFactory;

/**
 *
 * @author jkaplan
 */
public class RunnerServlet extends HttpServlet {
    /** a logger */
    private static final Logger logger =
            Logger.getLogger(RunnerServlet.class.getName());
    
    @Override
    public void init(ServletConfig config) throws ServletException {
        // Add a new Darkstar runner and start it.
        // TODO this should read an XML file describing all the components
        // to deploy, and then execute that deployment plan by creating
        // and optionally starting components.
        try {
            Runner r = RunnerFactory.create(DarkstarRunner.class.getName(), 
                                        new Properties());
            RunManager.getInstance().add(r);
        } catch (Exception ex) {
            throw new ServletException(ex);
        }
    }
   
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
              
        try {
            if (action.equalsIgnoreCase("start")) {
                doStart(request, response, runner);
            } else if (action.equalsIgnoreCase("stop")) {
                doStop(request, response, runner);
            } else if (action.equalsIgnoreCase("log")) {
                doLog(request, response, runner);
            } else {
                // default case -- show the view page
                doView(request, response);
            }
        } catch (RunnerException re) {
            throw new ServletException(re);
        }
    }
    
    private void doStart(HttpServletRequest request,
                         HttpServletResponse response,
                         Runner runner)
        throws ServletException, IOException, RunnerException
    {
        runner.start(new Properties());
        
        // redirect to the main page
        response.sendRedirect("run");
    }
    
    private void doStop(HttpServletRequest request,
                        HttpServletResponse response,
                        Runner runner)
        throws ServletException, IOException
    {
        runner.stop();
        
        // redirect to the main page
        response.sendRedirect("run");
    }
    
    private void doView(HttpServletRequest request,
                        HttpServletResponse response)
        throws ServletException, IOException
    {
        // fill in the list of all runners
        Collection<Runner> runners = RunManager.getInstance().getAll();
        request.setAttribute("runnerList", runners);
        
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
