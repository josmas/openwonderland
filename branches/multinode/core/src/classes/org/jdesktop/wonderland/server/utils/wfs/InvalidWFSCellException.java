/**
 * Project Looking Glass
 *
 * $RCSfile: InvalidWFSCellException.java,v $
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
 * $Revision: 1.2 $
 * $Date: 2007/10/17 17:11:06 $
 * $State: Exp $
 */

package org.jdesktop.wonderland.server.utils.wfs;

/**
 * The InvalidCellGLOSetupException is thrown if an invalid set of JavaBeans
 * properties is loaded.
 * <p>
 * @author jslott
 */
public class InvalidWFSCellException extends Exception {
    
    /**
     * Creates a new instance of <code>InvalidWFSCellException</code> without
     * detail message.
     */
    public InvalidWFSCellException() {
    }
    
    /**
     * Constructs an instance of <code>InvalidWFSCellException</code> with the 
     * specified detail message.
     * @param msg the detail message.
     */
    public InvalidWFSCellException(String msg) {
        super(msg);
    }

    /**
     * Constructs an instance of <code>InvalidWFSCellException</code> with the 
     * specified cause.
     * @param cause the cause.
     */
    public InvalidWFSCellException(Throwable cause) {
        super(cause);
    }

    /**
     * Constructs an instance of <code>InvalidWFSCellException</code> with the 
     * specified detail message and cause.
     & @param msg the detail message.
     * @param cause the cause.
     */
    public InvalidWFSCellException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
