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

package org.jdesktop.wonderland.runner;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipInputStream;
import org.jdesktop.wonderland.utils.RunUtil;
import org.jdesktop.wonderland.utils.SystemPropertyUtil;

/**
 * A base implementation of <code>Runner</code>.  This implements all the 
 * methods of <code>Runner</code>, so subclasses are only necessary to 
 * differentiate what files should be deployed by the <code>RunManager</code>.
 * @author jkaplan
 */
public abstract class BaseRunner implements Runner {
    /** a logger */
    private static final Logger logger =
            Logger.getLogger(BaseRunner.class.getName());
    
    /** the name of this runner */
    private String name = "unknown";
    
    /** the directory to run in */
    private File runDir;
    
    /** the process we started */
    private Process proc;
    
    /** the log file */
    private File logDir;
    private File logFile;
    private PrintWriter logWriter;
    
    /** the current status */
    private Status status = Status.NOT_RUNNING;
    
    /** status listeners */
    private Set<RunnerStatusListener> listeners = 
            new CopyOnWriteArraySet<RunnerStatusListener>();
   
    /** status updater thread */
    private Thread statusUpdater;
    
    /**
     * No-arg constructor for factory
     */
    protected BaseRunner() {
    }
    
    /**
     * Get the name of this runner.  Only valid after <code>cofigure()</code>
     * is called.
     * @return the name of the runner.
     */
    public String getName() {
        return name;
    }

    /**
     * Set the name of this runner
     * @param name the name of this runner
     */
    protected void setName(String name) {
        this.name = name;
    }
    
    /**
     * Configure the runner.  Curently parses the following properties:
     * <ul><li><code>runner.name</code> - the name to return in 
     *         <code>getName()</code>
     * </ul>
     * @param props the properties to configure with
     * @throws RunnerConfigurationException if there is an error
     */
    public void configure(Properties props) throws RunnerConfigurationException {
        if (props.containsKey("runner.name")) {
            setName(props.getProperty("runner.name"));
        }
    }

    /**
     * Get the directory to install files in.
     * @return the run directory
     */
    protected synchronized File getRunDir() {
        if (runDir == null) {
            runDir = RunUtil.createTempDir("server", "run");
        }
        
        return runDir;
    }
        
    /**
     * Deploy files.  This just unzips the files into the run
     * directory.
     * @param in the input file
     * @throws IOException if there is an error reading the file
     */
    public void deploy(InputStream in) throws IOException {
        RunUtil.extractZip(new ZipInputStream(in), getRunDir());
    }

    /**
     * Start the app.  This assumes the default Wonderland packaging, with
     * the ant libraries in "lib/ant" at the top level.  This will create
     * an external ant process to run the top-level build.xml script.
     * @param props the properties to start with
     * @throws RunnerException if there is a problem starting up
     */
    public synchronized void start(Properties props) throws RunnerException {
        // make sure we are in the correct state
        if (getStatus() != Status.NOT_RUNNING) {
            throw new IllegalStateException("Can't start runner in " + 
                                            getStatus() + " state");
        }
        
        // setup the logger.  First make sure that a new log will be
        // created.
        resetLogFile();
        try {
            logWriter = new PrintWriter(new FileWriter(getLogFile(true)));
        } catch (IOException ioe) {
            // no log file, abort
            logger.log(Level.WARNING, "Error creating log file " +
                       getLogFile() + " in runner " + getName());
            throw new RunnerException("Error creating log file", ioe);
        }
        
        try {
            String javaHome = System.getProperty("java.home");
            String fileSep = System.getProperty("file.separator");
            String runHome = getRunDir().getCanonicalPath();
            String antHome = runHome + fileSep + "lib" + fileSep + "ant";
        
            // create the command to execute as a list of strings.  We will
            // convert this to a string array later on in order to execute it.
            // Command will be of approximately the form:
            //
            // $java -cp ${antdir}/ant-launcher.jar -Dant.home=${antdir} \
            //       org.apache.tools.ant.launch.Launcher -Dxxx=yyy \
            //       -f ${rundir}/run.xml
            //
            List<String> cmd = new ArrayList<String>();
            cmd.add(javaHome + fileSep + "bin" + fileSep + "java");
            cmd.add("-cp");
            cmd.add(antHome + fileSep + "ant-launcher.jar");
            cmd.add("-Dant.home=" + antHome);
            cmd.add("org.apache.tools.ant.launch.Launcher");
            
            // add in the properties we were given
            for (Object propName : props.keySet()) {
                cmd.add("-D" + propName + "=" + 
                        props.getProperty((String) propName));
            }
            
            cmd.add("-f");
            cmd.add(runHome + fileSep + "run.xml");
               
            // log the command
            logWriter.println("Executing: " + cmd.toString());
        
            // update status
            setStatus(Status.STARTING_UP);
        
            // execute
            ProcessBuilder pb = new ProcessBuilder(cmd);
            pb.redirectErrorStream(true);
            pb.directory(getRunDir());
            
            proc = pb.start();
            Thread outReader = new Thread(
                    new ProcessOutputReader(proc.getInputStream(), logWriter));
            outReader.setName(getName() + " output reader");
            outReader.start();
            
            // start a thread to wait for the process to die
            Thread waiter = new Thread(
                    new ProcessWaiter(proc, outReader, logWriter));
            waiter.setName(getName() + " process waiter");
            waiter.start();
        } catch (IOException ioe) {
            ioe.printStackTrace(logWriter);
            setStatus(Status.ERROR);
            throw new RunnerException(ioe);
        }    
        
        // everything started up OK, so set the status to RUNNING.  Note
        // that we hold the lock, so even if the process finishes before
        // this is called, the ProcessWaiter won't be able to the the status
        // until we are done, guaranteeing the correct ordering of 
        // RUNNING and STOPPED.
        setStatus(Status.RUNNING);
    }

    public synchronized void stop() {
        // make sure we are in the correct state
        if (getStatus() != Status.RUNNING) {
            throw new IllegalStateException("Can't stop runner in " + 
                                            getStatus() + " state");
        }
        
        setStatus(Status.SHUTTING_DOWN);
        proc.destroy();
    }

    public synchronized Status getStatus() {
        return status;
    }

    /**
     * Set the status and notify listeners.  Notifying listeners is done
     * in a separate thread, so we don't have to worry about listeners
     * creating deadlocks.
     * @param status the new status
     */
    protected synchronized void setStatus(final Status status) {
        this.status = status;
        
        // make sure any notification that is in progress completes.  Otherwise
        // we could get out-of-order notifcations
        while (statusUpdater != null && statusUpdater.isAlive()) {
            try {
                statusUpdater.join();
            } catch (InterruptedException ie) {
                // ignore
            }
        }
        
        // start a new thread to do the notifications
        statusUpdater = new Thread(new Runnable() {
            public void run() {
                for (RunnerStatusListener l : listeners) {
                    l.statusChanged(BaseRunner.this, status);
                }
            }
        });
        statusUpdater.setName(getName() + " status update notifier");
        statusUpdater.start();
    }
    
    /**
     * Get the process log file.  Don't create it if it is null.
     * @return the log file, or null if it doesn't exist
     */
    public File getLogFile() {
        return getLogFile(false);
    }
    
    /**
     * Get the directory to keep log files in.  This is typically represented
     * by the <code>wonderland.log.dir</code> property.  If this property is not
     * specified, the "log" subdirectory of the run directory will be used.
     * 
     * @return the log directory
     */
    protected synchronized File getLogFile(boolean create) {
        if (create && logDir == null) {
            
            // check a property
            String logDirProp = SystemPropertyUtil.getProperty("wonderland.log.dir");
            if (logDirProp != null) {
                logDir = new File(logDirProp);
            } else {
                logDir = new File(RunUtil.getRunDir(), "log");
            }
            
            logDir.mkdirs();
        }
        
        if (create && logFile == null) {
            // create a new log file
            do {
                String fileName = getName() + ((int) (Math.random() * 65536)) + 
                                  ".log"; 
                logFile = new File(logDir, fileName);
            } while (logFile.exists());
        }
        
        return logFile;
    }
   
    /**
     * Reset the log file
     */
    protected synchronized void resetLogFile() {
        logFile = null;
        
        if (logWriter != null) {
            logWriter.close();
            logWriter = null;
        }
    }


    public void addStatusListener(RunnerStatusListener listener) {
        listeners.add(listener);
    }

    public void removeStatusListener(RunnerStatusListener listener) {
        listeners.remove(listener);
    }

    /**
     * Runners are identified by name.  Equals method is based on the name.
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final BaseRunner other = (BaseRunner) obj;
        if (this.name != other.name && (this.name == null || !this.name.equals(other.name))) {
            return false;
        }
        return true;
    }

    /**
     * Runners are identified by name.  Hashcode is based on the name.
     */
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 29 * hash + (this.name != null ? this.name.hashCode() : 0);
        return hash;
    }

    
    
    
    /** read a stream from a process and write it to the given log file */
    static class ProcessOutputReader implements Runnable {
        private BufferedReader in;
        private PrintWriter out;
        
        public ProcessOutputReader(InputStream is, PrintWriter out) {
            this.in = new BufferedReader(new InputStreamReader(is));
            this.out = out;
        }

        public void run() {
            String line;
            
            try {
                while ((line = in.readLine()) != null) {
                    out.println(line);
                    out.flush();
                }
            } catch (IOException ioe) {
                // oh well
                logger.log(Level.WARNING, "Exception in process output reader",
                           ioe);
            }
        }        
    } 
    
    /** wait for a process to end */
    class ProcessWaiter implements Runnable {
        private Process proc;
        private Thread outReader;
        private PrintWriter logWriter;
        
        public ProcessWaiter(Process proc, Thread outReader, 
                             PrintWriter logWriter) 
        {
            this.proc = proc;
            this.outReader = outReader;
            this.logWriter = logWriter;
        }

        public void run() {
            int exitValue = -1; 
            
            // first wait for the process to end
            boolean running = true;
            while (running) {
                try {
                    exitValue = proc.waitFor();
                    running = false;
                } catch (InterruptedException ie) {
                    // ignore -- just start waiting again
                }
            }
            
            // now wait for the output
            try {
                outReader.join();
            } catch (InterruptedException ie) {
                // ignore
            }    
            
            // wrte the exit value to the log and then close the log
            logWriter.println("Process exitted, return value: " + exitValue);
            logWriter.close();
            
            // everything has terminated -- update the status
            setStatus(Status.NOT_RUNNING);
        } 
    }
}
