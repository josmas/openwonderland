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
package org.jdesktop.wonderland.modules.xremwin.client;

import org.jdesktop.wonderland.client.utils.SmallIntegerAllocator;
import org.jdesktop.wonderland.common.ExperimentalAPI;
import org.jdesktop.wonderland.modules.appbase.client.MonitoredProcess;
import org.jdesktop.wonderland.modules.appbase.client.ProcessReporter;
import org.jdesktop.wonderland.modules.appbase.client.ProcessReporterFactory;
import org.jdesktop.wonderland.modules.xremwin.client.wm.X11WindowManager;
import org.jdesktop.wonderland.modules.xremwin.client.wm.X11IntegrationModule;

/**
 * The Window System for an X11 app. This consists of two processes:
 * 
 * 1. The Xremwin server (an external process).
 * 
 * 2. The Wonderland X11 window manager (a Java thread).
 *
 * @author deronj
 */

@ExperimentalAPI
class WindowSystemXrw 
    implements X11WindowManager.ExitListener 
{
    /**
     * Provides a way for the window system to notify other
     * Wonderland software components that it has exitted.
     */
    public interface ExitListener {

	/** The window system has exitted */
	public void windowSystemExitted();
    }

    /**
     * Preallocate the first two display numbers. The first for the user display and
     * the second for the LG3D display (no longer necessary but used for safety).
     */
    private static SmallIntegerAllocator displayNumAllocator = new SmallIntegerAllocator(2);

    // TODO: change this name
    public static final String XREMWIN_WEBSTART_DIR_PROP = "appshare.xremwinWebStartDir";

    /** The name of the app instance */
    private String appInstanceName;

    /** The X display number of the X server started. This number is valid when it is non-zero */
    private int displayNum;

    /** The name of the X display. This is the display number prefixed with ":". */
    private String displayName;
       
    /** The reporter to use for the server */
    private ProcessReporter xServerReporter;

    /* The Xremwin server process */
    private MonitoredProcess xServerProcess;

    /* The X11 window manager */
    private X11WindowManager wm;

    /* A component who wants to listen for window title changes from the window manager */
    private X11WindowManager.WindowTitleListener wtl;

    /** An exit listener */
    private ExitListener exitListener;

    /**
     * Create an instance of WindowSystemXrw. This launches the
     * exernal Xremwin server process and then starts the 
     * window manager thread. This method blocks until the 
     * window manager connects to the server.
     *
     * @param appInstanceName The unique name of the app instance
     * @param wtl A listener whose setWindowTitle method is called whenever the Xremwin server notifies us
     * that the title of the window has changed.
     */
    public static WindowSystemXrw create (String appInstanceName, X11WindowManager.WindowTitleListener wtl) {
	WindowSystemXrw winSys = null;
	try {
	    winSys = new WindowSystemXrw(appInstanceName, wtl);
	} catch (Exception ex) {
	    ex.printStackTrace();
	}
	return winSys;
    }

    /**
     * Create an instance of WindowSystemXrw. This launches the
     * exernal Xremwin server process and then starts the 
     * window manager thread. This method blocks until the 
     * window manager connects to the server.
     *
     * @param appInstanceName The unique name of the app instance
     * @param wtl A listener whose setWindowTitle method is called whenever the Xremwin server notifies us
     * that the title of the window has changed.
     */
    private WindowSystemXrw (String appInstanceName, X11WindowManager.WindowTitleListener wtl) 
    {
	this.appInstanceName = appInstanceName;
	startXServer();
	startWindowManager(wtl);
    }

    /** Attach an exit listener */
    void setExitListener (ExitListener listener) {
	exitListener = listener;
    }
	
    /** Start the Xremwin server */
    private void startXServer () {
	displayNum = allocDisplayNum();
	displayName = ":" + displayNum;

	String topDirPath = determineXremwinTopDirPath();

	String[] cmdAndArgs = new String[3];
	cmdAndArgs[0] = determineRunxremwinExecutable(topDirPath);
	cmdAndArgs[1] = displayName;
	cmdAndArgs[2] = determineXvfbDirectory(topDirPath);

	String processName = "Xremwin server for " + appInstanceName;
	xServerReporter = ProcessReporterFactory.getFactory().create(processName);
	if (xServerReporter == null) {
	    cleanup();
	    throw new RuntimeException("Cannot create error reporter for " +
				       processName);
	}
	
	xServerProcess = new MonitoredProcess(appInstanceName, cmdAndArgs, xServerReporter);
	if (!xServerProcess.start()) {
	    cleanup();
	    xServerReporter.output("Cannot start Xremwin server");
	    xServerReporter.exitValue(-1);
	    throw new RuntimeException("Cannot start Xremwin server");
	}
    }

    /** 
     * Start the window manager
     *
     * @param wtl A listener whose setWindowTitle method is called whenever the Xremwin server notifies us
     * that the title of the window has changed.
     */
    private void startWindowManager (X11WindowManager.WindowTitleListener wtl) 
    {
	X11IntegrationModule nativeWinIntegration = 
	    new X11IntegrationModule(displayName);
	nativeWinIntegration.initialize(wtl);
	wm = nativeWinIntegration.getWindowManager();
	wm.addExitListener(this);
    }

    /** For internal use only */
    public void windowManagerExitted () {
	AppXrw.logger.info("Window manager exitted for " + appInstanceName);
	wm.removeExitListener(this);
	cleanup();
    }

    /** 
     * Clean up resources. This kill the server process and stops the window manager thread.
     */
    public void cleanup () {
	if (displayNum != 0) {
	    deallocDisplayNum(displayNum);
	}

	if (xServerReporter != null) {
	    xServerReporter.cleanup();
	    xServerReporter = null;
	}

	if (xServerProcess != null) {
	    xServerProcess.cleanup();
	    xServerProcess = null;
	}


	// In the case of an error, the WindowManager may still be running.
        // Calling disconnect multiple times won't break anything, so double
        // check that it is disconnected.
	if (wm != null) {
	    wm.disconnect();
	    wm = null;
	}

	wtl = null;

	if (exitListener != null) {
	    exitListener.windowSystemExitted();
	}
	exitListener = null;
    }

    /**
     * The display name used to start the X server.
     */
    public String getDisplayName () {
	return displayName;
    }

    /**
     * The display number used to start the X server.
     */
    public int getDisplayNum () {
	return displayNum;
    }

    /** 
     * Allocate a new unique X11 display number.
     *
     * @return The display number allocated.
     */
    private static int allocDisplayNum () {
	return displayNumAllocator.allocate();
    }

    /**
     * Return the given X11 display number to the pool.
     *
     * @param displayNum The display number to deallocated.
     */
    private static void deallocDisplayNum (int displayNum) {
	displayNumAllocator.free(displayNum);
    }

    /**
     * Returns true if the Wonderland client is being run via Java webstart.
     */
    private static boolean usingWebStart () {
	try {
            Class clazz = Class.forName("javax.jnlp.BasicService");
	    return true;
	} catch (Exception ex) {
	    return false;
	}
    }

    /**
     * Find the directory in which the files for running the Xremwin
     * server can be found. For webstarted clients, the necessary
     * files will be in a system property. For all other situations, 
     * "." (meaning the current working directory) is returned.
     *
     * Note: for now we can assume that WL is always run in the 
     * top level wonderland directory.
     */
    private static String determineXremwinTopDirPath () {
	String xremwinWebStartDirPath = System.getProperty(XREMWIN_WEBSTART_DIR_PROP);
	System.err.println("xremwinWebStartDirPath = " + xremwinWebStartDirPath);
	if (xremwinWebStartDirPath == null) {
	    // Note: Assumes that the wonderland client is always run 
	    // with current working directory = lg3d-wonderland top-level.
	    return ".";
	} 
	
	return xremwinWebStartDirPath;
    }

    private static String determineRunxremwinExecutable (String topDirPath) {
	String xremwinWebStartDirPath = System.getProperty(XREMWIN_WEBSTART_DIR_PROP);
	if (xremwinWebStartDirPath == null) {
	    // Non-webstart case (release or workspace)
	    String scriptDir = System.getProperty("wonderland.scripts.dir");
	    return  scriptDir + "/runxremwin";
	} else {
	    return topDirPath + "/runxremwin";
	}
    }

    private static String determineXvfbDirectory (String topDirPath) {
	String xremwinWebStartDirPath = System.getProperty(XREMWIN_WEBSTART_DIR_PROP);
	if (xremwinWebStartDirPath == null) {

	    // Non-webstart case (release or workspace)
	    String xvfbDir = System.getProperty("wonderland.xvfb.dir");
	    return xvfbDir;

	} else {

	    // Webstart case
	    if ("SunOS".equals(System.getProperty("os.name"))) {
		// Solaris
		return topDirPath + "/solaris/bin";
	    } else {
		// Linux
		return topDirPath + "/linux/bin";
	    }
	}
    }

    /**
     * Tell the window system that this window has been closed.
     */
    public void deleteWindow (int wid) {
	wm.deleteWindow(wid);
    }
}
