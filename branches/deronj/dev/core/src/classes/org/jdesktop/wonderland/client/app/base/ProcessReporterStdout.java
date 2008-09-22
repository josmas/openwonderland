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
package org.jdesktop.wonderland.client.app.base;

import java.util.HashMap;
import org.jdesktop.wonderland.common.ExperimentalAPI;

/**
 * An process reporter which reports to the Wonderland client stdout using the App logger.
 *
 * @author deronj
 */

@ExperimentalAPI
public class ProcessReporterStdout extends ProcessReporter {
    
    /** 
     * Create a new instance of ProcessReporter.
     *
     * @param processName The name of the process on which to report.
     */
    ProcessReporterStdout (String processName) {
	super(processName);
    }

    /** 
     * {@inheritDoc}
     */
    public void output (String str) {
	App.logger.info("Output from app " + processName + ": " + str);
    }

    /** 
     * {@inheritDoc}
     */
    public void exitValue (int value) {
	App.logger.info("Process " + processName + "exitted.");
	App.logger.info("exitValue = " + value);
    }
}
