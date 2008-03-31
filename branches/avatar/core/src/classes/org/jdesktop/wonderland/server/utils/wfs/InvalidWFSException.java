/**
 * Project Looking Glass
 *
 * $RCSfile: InvalidWFSException.java,v $
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
 * $Date: 2007/10/17 17:11:07 $
 * $State: Exp $
 */

package org.jdesktop.wonderland.server.utils.wfs;

/**
 * The InvalidWFSException is thrown if a Wonderland File System is invalid, for
 * example is certain structural conventions are not followed.
 * <p>
 * @author jslott
 */
public class InvalidWFSException extends java.lang.Exception {
    
    /**
     * Creates a new instance of <code>InvalidWFSException</code> without detail message.
     */
    public InvalidWFSException() {
    }
    
    /**
     * Constructs an instance of <code>InvalidWFSException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public InvalidWFSException(String msg) {
        super(msg);
    }
}
