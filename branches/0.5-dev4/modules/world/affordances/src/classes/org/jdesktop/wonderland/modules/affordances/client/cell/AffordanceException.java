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

package org.jdesktop.wonderland.modules.affordances.client.cell;

/**
 * An exception indicating there was an error adding an affordance
 *
 * @author Jordan Slott <jslott@dev.java.net>
 */
public class AffordanceException extends Exception {

    /**
     * Creates a new instance of <code>AffordanceException</code> without detail message.
     */
    public AffordanceException() {
    }
    
    /**
     * Constructs an instance of <code>AffordanceException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public AffordanceException(String msg) {
        super(msg);
    }
}
