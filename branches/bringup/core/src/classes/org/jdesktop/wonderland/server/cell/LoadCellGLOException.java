/**
 * Project Looking Glass
 * 
 * $RCSfile: LoadCellGLOException.java,v $
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
 * $Revision: 1.1 $
 * $Date: 2007/07/20 21:32:06 $
 * $State: Exp $ 
 */
package org.jdesktop.wonderland.server.cell;

/**
 * An exception when loading a cell glo using a CellGLOProvider
 * @author jkaplan
 */
public class LoadCellGLOException extends RuntimeException {

    /**
     * Creates a new instance of <code>LoadCellGLOException</code> without 
     * detail message.
     */
    public LoadCellGLOException() {
    }

    /**
     * Constructs an instance of <code>LoadCellGLOException</code> with the 
     * specified detail message.
     * @param msg the detail message.
     */
    public LoadCellGLOException(String msg) {
        super (msg);
    }
    
    /**
     * Constructs an instance of <code>LoadCellGLOException</code> with the 
     * specified cause.
     * @param cause the cause.
     */
    public LoadCellGLOException(Throwable cause) {
        super (cause);
    }
    
    /**
     * Constructs an instance of <code>LoadCellGLOException</code> with the 
     * specified detail message and cause.
     * @param msg the detail message.
     * @param cause the cause
     */
    public LoadCellGLOException(String msg, Throwable cause) {
        super (msg, cause);
    }
}
