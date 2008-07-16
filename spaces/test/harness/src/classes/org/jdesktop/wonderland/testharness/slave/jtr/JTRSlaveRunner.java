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
package org.jdesktop.wonderland.testharness.slave.jtr;

import jtr.runners.AbstractRunner;
import jtr.test.IOutcome;
import org.jdesktop.wonderland.testharness.slave.SlaveMain;

/**
 * THe JTRSlaveRunner class is a "runner" in the context of the JTR distributed
 * Java test harness. This class is serlialized to each test slave, that
 * essentially just spawns a new slave client (SlaveMain).
 * <p>
 * See the documentation on JTR for more details:
 * http://jtrunner.sourceforge.net/JTR/The%20JTR%20Project.html
 * <p>
 * @author Jordan Slott <jslott@dev.java.net>
 */
public class JTRSlaveRunner extends AbstractRunner {
        
    /** Creates a new instance of SgsRunner */
    public JTRSlaveRunner() {
    }
 
    /**
     * The main testing method to launch the Wonderland client. This method
     * expects two parameters: the hostname and port of the server. If they
     * are not present, an exception is throw.
     * <p>
     * @throws java.lang.Throwable If the required parameters are not given
     */
    public void test() throws Throwable {
        /*
         * Attempt to fetch the hostname and port parameters. If either is not
         * present, this will most likely result in a NullPointerException, which
         * is just passed upwards.
         */
        String hostname = super.getParameters().getParameter("hostname").getValue();
        String port = super.getParameters().getParameter("port").getValue();
        
        /*
         * Start of a new Wonderland client
         */
        new SlaveMain(new String[] { hostname, port });
    }
 
    public void receiveFailureNotification(Throwable t, String msg) {}
    public void cleanupResources() {}
    public void enrichOutcome(IOutcome outcome) {}
}
