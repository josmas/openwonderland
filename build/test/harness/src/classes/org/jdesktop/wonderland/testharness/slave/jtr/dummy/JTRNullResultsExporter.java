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
package org.jdesktop.wonderland.testharness.slave.jtr.dummy;

import java.io.File;
import jtr.test.TestOutcomeTable;
import jtr.remote.test.NodeInfo;
import jtr.test.results.exporters.ITestResultsExporter;

/**
 * The JTRNullResultsExporter is a dummy placeholder instead of the Excel
 * results exporter.
 * <p>
 * See the documentation on JTR for more details:
 * http://jtrunner.sourceforge.net/JTR/The%20JTR%20Project.html
 * <p>
 * @author Jordan Slott <jslott@dev.java.net>
 */
public class JTRNullResultsExporter implements ITestResultsExporter {
        
    /** Default constructor */
    public JTRNullResultsExporter() {
    }
 
    public void export(NodeInfo node, TestOutcomeTable results) {}
    public void flush(File file) {}
}
