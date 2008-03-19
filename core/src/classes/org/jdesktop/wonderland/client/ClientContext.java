/**
 * Project Wonderland
 *
 * $Id$
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
  */
package org.jdesktop.wonderland.client;

/**
 *
 * @author paulby
 */
public class ClientContext {

    private static ThreadGroup threadGroup = new ThreadGroup("Wonderland");
    
    /**
     * Returns a ThreadGroup for the client so all wonderland threads can
     * be associated together.
     * @return
     */
    public static ThreadGroup getThreadGroup() {
        return threadGroup;
    }
}
