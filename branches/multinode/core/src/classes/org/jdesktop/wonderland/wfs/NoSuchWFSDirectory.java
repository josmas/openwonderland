/**
 * Project Looking Glass
 *
 * $RCSfile: NoSuchWFSDirectory.java,v $
 *
 * Copyright (c) 2004-2007, Sun Microsystems, Inc., All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * $Revision: 1.2.8.2 $
 * $Date: 2008/04/08 10:44:30 $
 * $State: Exp $
 */

package org.jdesktop.wonderland.wfs;

/**
 * The NoSuchWFSDirectory is thrown if a directory cannot be found in the WFS
 * hierarchy. XXXX
 * <p>
 * @author jslott
 */
public class NoSuchWFSDirectory extends java.lang.Exception {
    
    /**
     * Creates a new instance of <code>NoSuchWFSDirectory</code> without detail message.
     */
    public NoSuchWFSDirectory() {
    }
    
    /**
     * Constructs an instance of <code>NoSuchWFSDirectory</code> with the specified detail message.
     * @param msg the detail message.
     */
    public NoSuchWFSDirectory(String msg) {
        super(msg);
    }
}
